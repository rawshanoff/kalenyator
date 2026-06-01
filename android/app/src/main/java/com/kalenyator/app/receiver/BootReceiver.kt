package com.kalenyator.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kalenyator.app.KalenyatorApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED &&
            intent?.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return

        val app = context.applicationContext as KalenyatorApplication
        val pending = goAsync()
        scope.launch {
            val settings = app.container.settingsRepository.reminderSettings.first()
            if (settings.enabled) {
                app.container.reminderScheduler.schedule()
            }
            pending.finish()
        }
    }
}
