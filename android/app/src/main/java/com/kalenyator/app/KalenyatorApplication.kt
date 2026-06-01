package com.kalenyator.app

import android.app.Application
import com.kalenyator.app.data.local.AppDatabase
import com.kalenyator.app.data.reminder.ReminderRepository
import com.kalenyator.app.data.reminder.ReminderScheduler
import com.kalenyator.app.data.repository.FamilyRepository
import com.kalenyator.app.data.settings.SettingsRepository
import com.kalenyator.app.data.sync.SyncRepository
import com.kalenyator.app.data.weather.WeatherRepository
import com.kalenyator.app.util.LocaleApplicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class KalenyatorApplication : Application() {
    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        runBlocking {
            LocaleApplicator.apply(container.settingsRepository.language.first())
        }
        container.reminderScheduler.schedule()
        appScope.launch {
            runCatching {
                val region = container.settingsRepository.holidayRegion.first()
                container.settingsRepository.ensureDefaultWeatherCities(
                    container.weatherRepository.defaultCitiesForRegion(region)
                )
                val cities = container.settingsRepository.weatherCities.first()
                if (cities.isNotEmpty()) {
                    container.weatherRepository.fetchCities(cities)
                }
                com.kalenyator.app.widget.WeatherWidgetUpdater.update(this@KalenyatorApplication)
            }
        }
    }
}

class AppContainer(val app: Application) {
    private val database = AppDatabase.get(app)
    val settingsRepository = SettingsRepository(app)
    val familyRepository = FamilyRepository(database.familyEventDao())
    val syncRepository = SyncRepository(app, database.familyEventDao())
    val weatherRepository = WeatherRepository()
    val reminderRepository = ReminderRepository(
        app,
        database.familyEventDao(),
        familyRepository,
        settingsRepository
    )
    val reminderScheduler = ReminderScheduler(app, reminderRepository)
}
