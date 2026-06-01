package com.kalenyator.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import com.kalenyator.app.KalenyatorApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WeatherAppWidget : AppWidgetProvider() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val app = context.applicationContext as KalenyatorApplication
        scope.launch {
            val cities = app.container.settingsRepository.weatherCities.first()
            if (cities.isNotEmpty()) {
                app.container.weatherRepository.fetchCities(cities)
            }
            WeatherWidgetUpdater.update(context)
        }
    }

    override fun onEnabled(context: Context) {
        WeatherWidgetUpdater.update(context)
    }
}
