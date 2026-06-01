package com.kalenyator.app.data.reminder

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kalenyator.app.data.local.FamilyEventDao
import com.kalenyator.app.data.local.FamilyEventEntity
import com.kalenyator.app.data.model.FamilyEventType
import com.kalenyator.app.data.repository.FamilyRepository
import com.kalenyator.app.data.settings.SettingsRepository
import com.kalenyator.app.widget.BirthdayWidgetUpdater
import com.kalenyator.app.widget.WeatherWidgetUpdater
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime

private val Context.reminderDataStore: DataStore<Preferences> by preferencesDataStore("kalenyator_reminders")

data class NextFamilyEvent(
    val title: String,
    val type: FamilyEventType,
    val daysUntil: Long,
    val day: Int,
    val month: Int,
    val photoPath: String? = null,
    val millisLeft: Long = 0
)

class ReminderRepository(
    private val context: Context,
    private val familyDao: FamilyEventDao,
    private val familyRepository: FamilyRepository,
    private val settingsRepository: SettingsRepository
) {
    private val notifiedStore = context.reminderDataStore

    suspend fun runDailyCheck() {
        val settings = settingsRepository.reminderSettings.first()
        val today = LocalDate.now()
        val nowHour = LocalTime.now().hour
        val events = familyDao.getAll()

        BirthdayWidgetUpdater.update(context, findNextEvent(events, today))
        WeatherWidgetUpdater.update(context)

        if (!settings.enabled) return
        if (nowHour != settings.reminderHour) return

        val sentKeys = notifiedStore.data.first()[NOTIFIED_KEYS] ?: emptySet()
        val newKeys = sentKeys.toMutableSet()

        events.forEach { event ->
            val daysUntil = familyRepository.daysUntil(event, today)
            when {
                daysUntil == settings.daysBefore.toLong() -> {
                    val key = notificationKey(event.id, today.year, "adv", settings.daysBefore)
                    if (key !in sentKeys) {
                        NotificationHelper.showAdvanceReminder(
                            context, event, settings.daysBefore.toInt(),
                            notificationId(event.id, settings.daysBefore)
                        )
                        newKeys.add(key)
                    }
                }
                daysUntil == 0L && settings.remindOnDay -> {
                    val key = notificationKey(event.id, today.year, "today", 0)
                    if (key !in sentKeys) {
                        NotificationHelper.showTodayReminder(
                            context, event,
                            notificationId(event.id, 0) + 1
                        )
                        newKeys.add(key)
                    }
                }
            }
        }

        if (newKeys != sentKeys) {
            notifiedStore.edit { it[NOTIFIED_KEYS] = newKeys }
        }
    }

    suspend fun refreshWidget() {
        val events = familyDao.getAll()
        BirthdayWidgetUpdater.update(context, findNextEvent(events, LocalDate.now()))
        WeatherWidgetUpdater.update(context)
    }

    fun findNextEvent(events: List<FamilyEventEntity>, today: LocalDate): NextFamilyEvent? {
        return events
            .map { event -> event to familyRepository.daysUntil(event, today) }
            .filter { (_, days) -> days >= 0 }
            .minByOrNull { (_, days) -> days }
            ?.let { (event, days) ->
                val target = familyRepository.nextOccurrenceDateTime(event)
                val millis = java.time.temporal.ChronoUnit.MILLIS.between(
                    java.time.LocalDateTime.now(),
                    target
                ).coerceAtLeast(0)
                NextFamilyEvent(
                    title = event.title,
                    type = event.type,
                    daysUntil = days,
                    day = event.day,
                    month = event.month,
                    photoPath = event.photoPath,
                    millisLeft = millis
                )
            }
    }

    suspend fun clearNotifiedKeysForEvent(eventId: Long) {
        notifiedStore.edit { prefs ->
            val filtered = (prefs[NOTIFIED_KEYS] ?: emptySet()).filterNot { it.startsWith("e${eventId}_") }
            prefs[NOTIFIED_KEYS] = filtered.toSet()
        }
    }

    private fun notificationKey(eventId: Long, year: Int, kind: String, extra: Int) =
        "e${eventId}_${year}_${kind}_$extra"

    private fun notificationId(eventId: Long, extra: Int): Int =
        (eventId * 10 + extra).toInt() and 0x7FFFFFFF

    companion object {
        private val NOTIFIED_KEYS = stringSetPreferencesKey("notified_keys")
    }
}
