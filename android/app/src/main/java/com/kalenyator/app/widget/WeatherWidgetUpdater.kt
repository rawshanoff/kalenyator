package com.kalenyator.app.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import com.kalenyator.app.KalenyatorApplication
import com.kalenyator.app.R
import com.kalenyator.app.util.weatherEmoji

object WeatherWidgetUpdater {
    fun update(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val component = ComponentName(context, WeatherAppWidget::class.java)
        val ids = manager.getAppWidgetIds(component)
        if (ids.isEmpty()) return

        val weather = (context.applicationContext as KalenyatorApplication)
            .container.weatherRepository.cached

        ids.forEach { id ->
            val views = if (weather == null) {
                emptyViews(context)
            } else {
                RemoteViews(context.packageName, R.layout.widget_weather).apply {
                    setTextViewText(R.id.widget_weather_title, context.getString(R.string.weather_widget_title))
                    setTextViewText(R.id.widget_weather_city, weather.cityName)
                    setTextViewText(R.id.widget_weather_temp, "${weather.temperatureC.toInt()}°C")
                    setTextViewText(R.id.widget_weather_emoji, weatherEmoji(weather.weatherCode))
                }
            }
            manager.updateAppWidget(id, views)
        }
    }

    private fun emptyViews(context: Context): RemoteViews {
        return RemoteViews(context.packageName, R.layout.widget_weather).apply {
            setTextViewText(R.id.widget_weather_title, context.getString(R.string.weather_widget_title))
            setTextViewText(R.id.widget_weather_city, "—")
            setTextViewText(R.id.widget_weather_temp, context.getString(R.string.weather_loading))
            setTextViewText(R.id.widget_weather_emoji, "🌤️")
        }
    }
}
