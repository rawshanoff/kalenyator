package com.kalenyator.app.data.weather

import com.kalenyator.app.data.model.HolidayRegion
import com.kalenyator.app.util.DateFormatUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

data class WeatherInfo(
    val cityName: String,
    val latitude: Double,
    val longitude: Double,
    val temperatureC: Double,
    val humidity: Int,
    val windKmh: Double,
    val weatherCode: Int,
    val updatedAt: String,
    val dailyForecast: List<DailyForecast> = emptyList(),
    val isOffline: Boolean = false,
    val isHome: Boolean = false
)

class WeatherRepository {
    @Volatile
    var cached: WeatherInfo? = null
        private set

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .apply {
            if (com.kalenyator.app.BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                })
            }
        }
        .build()

    private val weatherApi: WeatherApi = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WeatherApi::class.java)

    private val geocodingApi: GeocodingApi = Retrofit.Builder()
        .baseUrl("https://geocoding-api.open-meteo.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GeocodingApi::class.java)

    fun defaultCitiesForRegion(region: HolidayRegion): List<WeatherCity> = when (region) {
        HolidayRegion.RUSSIA -> listOf(
            WeatherCity("Москва", 55.7558, 37.6173, isHome = true),
            WeatherCity("Санкт-Петербург", 59.9343, 30.3351)
        )
        else -> listOf(
            WeatherCity("Ташкент", 41.2995, 69.2401, isHome = true),
            WeatherCity("Самарканд", 39.6542, 66.9597)
        )
    }

    fun sortCities(cities: List<WeatherCity>): List<WeatherCity> =
        cities.sortedByDescending { it.isHome }

    suspend fun searchCities(query: String): Result<List<GeocodingResult>> = withContext(Dispatchers.IO) {
        if (query.length < 2) return@withContext Result.success(emptyList())
        runCatching { geocodingApi.search(name = query).results.orEmpty() }
    }

    suspend fun fetchCity(city: WeatherCity, offline: Boolean = false): Result<WeatherInfo> = withContext(Dispatchers.IO) {
        runCatching {
            val response = weatherApi.forecast(latitude = city.latitude, longitude = city.longitude)
            val current = response.current ?: error("No weather data")
            val daily = parseDaily(response.daily)
            WeatherInfo(
                cityName = city.name,
                latitude = city.latitude,
                longitude = city.longitude,
                temperatureC = current.temperature_2m ?: 0.0,
                humidity = current.relative_humidity_2m ?: 0,
                windKmh = current.wind_speed_10m ?: 0.0,
                weatherCode = current.weather_code ?: 0,
                updatedAt = current.time ?: "",
                dailyForecast = daily,
                isOffline = offline,
                isHome = city.isHome
            )
        }
    }

    suspend fun fetchCities(cities: List<WeatherCity>): Result<List<WeatherInfo>> = withContext(Dispatchers.IO) {
        if (cities.isEmpty()) return@withContext Result.success(emptyList())
        runCatching {
            coroutineScope {
                sortCities(cities).map { city ->
                    async { fetchCity(city).getOrThrow() }
                }.awaitAll()
            }.also { list ->
                cached = list.firstOrNull { c ->
                    cities.any { it.isHome && kotlin.math.abs(it.latitude - c.latitude) < 0.01 }
                } ?: list.firstOrNull()
            }
        }
    }

    suspend fun fetchForRegion(region: HolidayRegion): Result<WeatherInfo> =
        fetchCities(defaultCitiesForRegion(region)).map { it.first() }

    private fun parseDaily(daily: DailyWeather?): List<DailyForecast> {
        if (daily == null) return emptyList()
        val times = daily.time.orEmpty()
        val codes = daily.weather_code.orEmpty()
        val maxes = daily.temperature_2m_max.orEmpty()
        val mins = daily.temperature_2m_min.orEmpty()
        return times.indices.map { i ->
            val dateRaw = times.getOrNull(i) ?: ""
            val label = runCatching {
                DateFormatUtil.format(java.time.LocalDate.parse(dateRaw))
            }.getOrElse { dateRaw }
            DailyForecast(
                dateLabel = label,
                maxC = maxes.getOrNull(i)?.toInt() ?: 0,
                minC = mins.getOrNull(i)?.toInt() ?: 0,
                weatherCode = codes.getOrNull(i) ?: 0
            )
        }.take(7)
    }
}
