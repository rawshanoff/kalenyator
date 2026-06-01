package com.kalenyator.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import com.kalenyator.app.KalenyatorApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BirthdayAppWidget : AppWidgetProvider() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val app = context.applicationContext as KalenyatorApplication
        scope.launch {
            app.container.reminderRepository.refreshWidget()
        }
    }

    override fun onEnabled(context: Context) {
        val app = context.applicationContext as KalenyatorApplication
        scope.launch {
            app.container.reminderRepository.refreshWidget()
        }
    }
}
