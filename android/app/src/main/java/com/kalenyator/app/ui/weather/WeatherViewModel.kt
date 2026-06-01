package com.kalenyator.app.ui.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kalenyator.app.data.settings.SettingsRepository
import com.kalenyator.app.data.weather.GeocodingResult
import com.kalenyator.app.data.weather.WeatherCity
import com.kalenyator.app.data.weather.WeatherInfo
import com.kalenyator.app.data.weather.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class WeatherUiState(
    val loading: Boolean = true,
    val cities: List<WeatherInfo> = emptyList(),
    val error: String? = null,
    val offline: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<GeocodingResult> = emptyList(),
    val searching: Boolean = false
)

class WeatherViewModel(
    private val weatherRepository: WeatherRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val refreshTrigger = MutableStateFlow(0)
    private val _state = MutableStateFlow(WeatherUiState())
    val state: StateFlow<WeatherUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val region = settingsRepository.holidayRegion.first()
            settingsRepository.ensureDefaultWeatherCities(
                weatherRepository.defaultCitiesForRegion(region)
            )
        }
        viewModelScope.launch {
            combine(settingsRepository.weatherCities, refreshTrigger) { cities, _ -> cities }
                .collect { cities -> load(cities) }
        }
    }

    fun refresh() {
        refreshTrigger.value = refreshTrigger.value + 1
    }

    fun updateSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        if (query.length < 2) {
            _state.value = _state.value.copy(searchResults = emptyList(), searching = false)
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(searching = true)
            weatherRepository.searchCities(query)
                .onSuccess { results ->
                    _state.value = _state.value.copy(searchResults = results, searching = false)
                }
                .onFailure {
                    _state.value = _state.value.copy(searchResults = emptyList(), searching = false)
                }
        }
    }

    fun addCity(result: GeocodingResult) {
        viewModelScope.launch {
            val current = settingsRepository.weatherCities.first()
            val city = WeatherCity(result.displayName(), result.latitude, result.longitude)
            val exists = current.any {
                kotlin.math.abs(it.latitude - city.latitude) < 0.01 &&
                    kotlin.math.abs(it.longitude - city.longitude) < 0.01
            }
            if (!exists) settingsRepository.setWeatherCities(current + city)
            _state.value = _state.value.copy(searchQuery = "", searchResults = emptyList())
        }
    }

    fun removeCity(info: WeatherInfo) {
        viewModelScope.launch {
            val current = settingsRepository.weatherCities.first()
            settingsRepository.setWeatherCities(
                current.filterNot {
                    kotlin.math.abs(it.latitude - info.latitude) < 0.01 &&
                        kotlin.math.abs(it.longitude - info.longitude) < 0.01
                }
            )
        }
    }

    fun setHomeCity(info: WeatherInfo) {
        viewModelScope.launch {
            settingsRepository.setHomeCity(
                WeatherCity(info.cityName, info.latitude, info.longitude, isHome = true)
            )
            refresh()
        }
    }

    private suspend fun load(cities: List<WeatherCity>) {
        if (cities.isEmpty()) {
            _state.value = WeatherUiState(loading = false, cities = emptyList())
            return
        }
        val sorted = weatherRepository.sortCities(cities)
        val cache = settingsRepository.getWeatherCache()
        if (cache.isNotEmpty()) {
            _state.value = _state.value.copy(loading = false, cities = cache, offline = false)
        } else {
            _state.value = _state.value.copy(loading = true, error = null)
        }
        weatherRepository.fetchCities(sorted)
            .onSuccess { list ->
                settingsRepository.setWeatherCache(list)
                _state.value = WeatherUiState(loading = false, cities = list, offline = false)
            }
            .onFailure {
                val cached = settingsRepository.getWeatherCache()
                if (cached.isNotEmpty()) {
                    _state.value = WeatherUiState(
                        loading = false,
                        cities = cached.map { it.copy(isOffline = true) },
                        offline = true
                    )
                } else {
                    _state.value = _state.value.copy(loading = false, error = it.message ?: "error")
                }
            }
    }
}
