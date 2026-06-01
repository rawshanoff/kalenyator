package com.kalenyator.app.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.graphics.BitmapFactory
import android.view.View
import android.widget.RemoteViews
import com.kalenyator.app.R
import com.kalenyator.app.data.model.FamilyEventType
import com.kalenyator.app.data.reminder.NextFamilyEvent
import com.kalenyator.app.data.repository.CountdownParts
import com.kalenyator.app.util.DateFormatUtil
import java.io.File

object BirthdayWidgetUpdater {

    fun update(context: Context, next: NextFamilyEvent?) {
        val manager = AppWidgetManager.getInstance(context)
        val component = ComponentName(context, BirthdayAppWidget::class.java)
        val ids = manager.getAppWidgetIds(component)
        if (ids.isEmpty()) return

        val views = if (next == null) emptyViews(context) else buildViews(context, next)
        ids.forEach { id -> manager.updateAppWidget(id, views) }
    }

    private fun buildViews(context: Context, next: NextFamilyEvent): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_birthday)
        val emoji = when (next.type) {
            FamilyEventType.BIRTHDAY -> "🎂"
            FamilyEventType.ANNIVERSARY -> "💍"
            FamilyEventType.OTHER -> "⭐"
        }
        views.setTextViewText(R.id.widget_title, context.getString(R.string.widget_title))
        views.setTextViewText(R.id.widget_name, "$emoji ${next.title}")
        val countdown = when (next.daysUntil) {
            0L -> context.getString(R.string.days_until_today)
            else -> context.getString(R.string.days_until, next.daysUntil.toInt())
        }
        views.setTextViewText(
            R.id.widget_countdown,
            "$countdown · ${DateFormatUtil.format(next.day, next.month)}"
        )
        val parts = formatMillis(next.millisLeft)
        views.setTextViewText(
            R.id.widget_timer,
            context.getString(
                R.string.family_countdown_format,
                parts.days, parts.hours, parts.minutes, parts.seconds
            )
        )
        val photo = next.photoPath
        if (!photo.isNullOrBlank() && File(photo).exists()) {
            val bmp = BitmapFactory.decodeFile(photo)
            if (bmp != null) {
                views.setViewVisibility(R.id.widget_photo, View.VISIBLE)
                views.setImageViewBitmap(R.id.widget_photo, bmp)
            } else {
                views.setViewVisibility(R.id.widget_photo, View.GONE)
            }
        } else {
            views.setViewVisibility(R.id.widget_photo, View.GONE)
        }
        return views
    }

    private fun formatMillis(millis: Long): CountdownParts {
        val totalSec = millis / 1000
        return CountdownParts(
            days = totalSec / 86400,
            hours = (totalSec % 86400) / 3600,
            minutes = (totalSec % 3600) / 60,
            seconds = totalSec % 60
        )
    }

    private fun emptyViews(context: Context): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_birthday)
        views.setTextViewText(R.id.widget_title, context.getString(R.string.widget_title))
        views.setTextViewText(R.id.widget_name, context.getString(R.string.widget_empty))
        views.setTextViewText(R.id.widget_countdown, context.getString(R.string.family_add))
        views.setTextViewText(R.id.widget_timer, "")
        views.setViewVisibility(R.id.widget_photo, View.GONE)
        return views
    }
}
