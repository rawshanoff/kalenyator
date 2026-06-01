package com.kalenyator.app.data.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.kalenyator.app.MainActivity
import com.kalenyator.app.R
import com.kalenyator.app.data.local.FamilyEventEntity
import com.kalenyator.app.data.model.FamilyEventType

object NotificationHelper {
    const val CHANNEL_ID = "kalenyator_birthdays"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.reminder_channel_desc)
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    fun showAdvanceReminder(
        context: Context,
        event: FamilyEventEntity,
        daysBefore: Int,
        notificationId: Int
    ) {
        val emoji = emojiFor(event.type)
        val title = context.getString(R.string.reminder_notification_title, emoji, event.title)
        val text = when (event.type) {
            FamilyEventType.BIRTHDAY ->
                context.getString(R.string.reminder_notification_birthday, daysBefore)
            FamilyEventType.ANNIVERSARY ->
                context.getString(R.string.reminder_notification_anniversary, daysBefore)
            FamilyEventType.OTHER ->
                context.getString(R.string.reminder_notification_other, daysBefore)
        }
        notify(context, notificationId, title, text)
    }

    fun showTodayReminder(context: Context, event: FamilyEventEntity, notificationId: Int) {
        val emoji = emojiFor(event.type)
        val title = context.getString(R.string.reminder_notification_title, emoji, event.title)
        val text = context.getString(R.string.reminder_notification_today, event.title)
        notify(context, notificationId, title, text)
    }

    private fun emojiFor(type: FamilyEventType) = when (type) {
        FamilyEventType.BIRTHDAY -> "🎂"
        FamilyEventType.ANNIVERSARY -> "💍"
        FamilyEventType.OTHER -> "⭐"
    }

    private fun notify(context: Context, id: Int, title: String, text: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(id, notification)
    }
}
