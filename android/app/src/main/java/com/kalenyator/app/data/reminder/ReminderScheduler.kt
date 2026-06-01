package com.kalenyator.app.data.reminder

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ReminderScheduler(
    private val context: Context,
    private val reminderRepository: ReminderRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun schedule() {
        NotificationHelper.createChannel(context)
        val request = PeriodicWorkRequestBuilder<BirthdayReminderWorker>(1, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
        scope.launch {
            reminderRepository.runDailyCheck()
        }
    }

    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    fun reschedule() {
        schedule()
    }

    companion object {
        private const val WORK_NAME = "kalenyator_birthday_reminders"
    }
}
