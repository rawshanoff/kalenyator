package com.kalenyator.app.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kalenyator.app.data.model.AppLanguage
import com.kalenyator.app.data.model.AppThemeMode
import com.kalenyator.app.data.model.AppVisualTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kalenyator.app.data.model.HolidayRegion
import com.kalenyator.app.data.weather.WeatherCity
import com.kalenyator.app.data.weather.WeatherInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kalenyator_settings")

class SettingsRepository(private val context: Context) {
    private val dataStore = context.dataStore
    private val gson = Gson()
    private val weatherCityListType = object : TypeToken<List<WeatherCity>>() {}.type
    private val weatherInfoListType = object : TypeToken<List<WeatherInfo>>() {}.type
    private val stringListType = object : TypeToken<List<String>>() {}.type

    val language: Flow<AppLanguage> = dataStore.data.map { prefs ->
        when (prefs[LANGUAGE]) {
            AppLanguage.UZ.code -> AppLanguage.UZ
            else -> AppLanguage.RU
        }
    }

    val holidayRegion: Flow<HolidayRegion> = dataStore.data.map { prefs ->
        when (prefs[REGION]) {
            "RU" -> HolidayRegion.RUSSIA
            "BOTH" -> HolidayRegion.BOTH
            else -> HolidayRegion.UZBEKISTAN
        }
    }

    val themeMode: Flow<AppThemeMode> = dataStore.data.map { prefs ->
        when (prefs[THEME]) {
            "LIGHT" -> AppThemeMode.LIGHT
            "DARK" -> AppThemeMode.DARK
            else -> AppThemeMode.SYSTEM
        }
    }

    val visualTheme: Flow<AppVisualTheme> = dataStore.data.map { prefs ->
        when (prefs[VISUAL_THEME]) {
            "SPACE" -> AppVisualTheme.SPACE
            "MINIMAL" -> AppVisualTheme.MINIMAL
            "NAVRUZ" -> AppVisualTheme.NAVRUZ
            "NEW_YEAR" -> AppVisualTheme.NEW_YEAR
            else -> AppVisualTheme.TENTAKO
        }
    }

    val reminderSettings: Flow<ReminderSettings> = dataStore.data.map { prefs ->
        ReminderSettings(
            enabled = prefs[REMINDER_ENABLED] ?: true,
            daysBefore = prefs[REMINDER_DAYS_BEFORE] ?: 3,
            remindOnDay = prefs[REMINDER_ON_DAY] ?: true,
            reminderHour = prefs[REMINDER_HOUR] ?: 9
        )
    }

    val weatherCities: Flow<List<WeatherCity>> = dataStore.data.map { prefs ->
        parseWeatherCities(prefs[WEATHER_CITIES])
    }

    val calculatorHistory: Flow<List<String>> = dataStore.data.map { prefs ->
        parseStringList(prefs[CALC_HISTORY])
    }

    val lastExportTime: Flow<Long> = dataStore.data.map { prefs ->
        prefs[LAST_EXPORT_TIME] ?: 0L
    }

    suspend fun setLanguage(language: AppLanguage) {
        dataStore.edit { it[LANGUAGE] = language.code }
    }

    suspend fun setHolidayRegion(region: HolidayRegion) {
        dataStore.edit {
            it[REGION] = when (region) {
                HolidayRegion.UZBEKISTAN -> "UZ"
                HolidayRegion.RUSSIA -> "RU"
                HolidayRegion.BOTH -> "BOTH"
            }
        }
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        dataStore.edit {
            it[THEME] = when (mode) {
                AppThemeMode.LIGHT -> "LIGHT"
                AppThemeMode.DARK -> "DARK"
                AppThemeMode.SYSTEM -> "SYSTEM"
            }
        }
    }

    suspend fun setVisualTheme(theme: AppVisualTheme) {
        dataStore.edit {
            it[VISUAL_THEME] = when (theme) {
                AppVisualTheme.SPACE -> "SPACE"
                AppVisualTheme.MINIMAL -> "MINIMAL"
                AppVisualTheme.NAVRUZ -> "NAVRUZ"
                AppVisualTheme.NEW_YEAR -> "NEW_YEAR"
                AppVisualTheme.TENTAKO -> "TENTAKO"
            }
        }
    }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        dataStore.edit { it[REMINDER_ENABLED] = enabled }
    }

    suspend fun setReminderDaysBefore(days: Int) {
        dataStore.edit { it[REMINDER_DAYS_BEFORE] = days.coerceIn(1, 30) }
    }

    suspend fun setRemindOnDay(enabled: Boolean) {
        dataStore.edit { it[REMINDER_ON_DAY] = enabled }
    }

    suspend fun setReminderHour(hour: Int) {
        dataStore.edit { it[REMINDER_HOUR] = if (hour == 18) 18 else 9 }
    }

    suspend fun ensureDefaultWeatherCities(defaults: List<WeatherCity>) {
        val current = dataStore.data.first()
        if (current[WEATHER_CITIES].isNullOrBlank()) {
            setWeatherCities(defaults)
        }
    }

    suspend fun setWeatherCities(cities: List<WeatherCity>) {
        dataStore.edit { it[WEATHER_CITIES] = gson.toJson(cities.distinctBy { "${it.name}|${it.latitude}|${it.longitude}" }) }
    }

    suspend fun addCalculatorHistoryEntry(entry: String) {
        dataStore.edit { prefs ->
            val updated = (listOf(entry) + parseStringList(prefs[CALC_HISTORY])).take(50)
            prefs[CALC_HISTORY] = gson.toJson(updated)
        }
    }

    suspend fun clearCalculatorHistory() {
        dataStore.edit { it.remove(CALC_HISTORY) }
    }

    suspend fun markExportCompleted() {
        dataStore.edit { it[LAST_EXPORT_TIME] = System.currentTimeMillis() }
    }

    suspend fun getWeatherCache(): List<WeatherInfo> {
        val json = dataStore.data.first()[WEATHER_CACHE]
        return parseWeatherCache(json)
    }

    suspend fun setWeatherCache(list: List<WeatherInfo>) {
        dataStore.edit { it[WEATHER_CACHE] = gson.toJson(list) }
    }

    suspend fun setHomeCity(target: WeatherCity) {
        val current = weatherCities.first()
        setWeatherCities(
            current.map { c ->
                c.copy(isHome = sameCity(c, target))
            }
        )
    }

    private fun sameCity(a: WeatherCity, b: WeatherCity): Boolean =
        kotlin.math.abs(a.latitude - b.latitude) < 0.01 &&
            kotlin.math.abs(a.longitude - b.longitude) < 0.01

    private fun parseWeatherCities(json: String?): List<WeatherCity> =
        if (json.isNullOrBlank()) emptyList()
        else runCatching { gson.fromJson<List<WeatherCity>>(json, weatherCityListType) }.getOrDefault(emptyList())

    private fun parseStringList(json: String?): List<String> =
        if (json.isNullOrBlank()) emptyList()
        else runCatching { gson.fromJson<List<String>>(json, stringListType) }.getOrDefault(emptyList())

    private fun parseWeatherCache(json: String?): List<WeatherInfo> =
        if (json.isNullOrBlank()) emptyList()
        else runCatching { gson.fromJson<List<WeatherInfo>>(json, weatherInfoListType) }.getOrDefault(emptyList())

    companion object {
        private val LANGUAGE = stringPreferencesKey("language")
        private val REGION = stringPreferencesKey("region")
        private val THEME = stringPreferencesKey("theme")
        private val VISUAL_THEME = stringPreferencesKey("visual_theme")
        private val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        private val REMINDER_DAYS_BEFORE = intPreferencesKey("reminder_days_before")
        private val REMINDER_ON_DAY = booleanPreferencesKey("reminder_on_day")
        private val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        private val WEATHER_CITIES = stringPreferencesKey("weather_cities")
        private val CALC_HISTORY = stringPreferencesKey("calc_history")
        private val WEATHER_CACHE = stringPreferencesKey("weather_cache")
        private val LAST_EXPORT_TIME = longPreferencesKey("last_export_time")

        val REMINDER_DAY_OPTIONS = listOf(1, 3, 7, 14)
        val REMINDER_HOUR_OPTIONS = listOf(9, 18)
    }
}
