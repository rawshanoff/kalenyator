package com.kalenyator.app.data.repository

import com.kalenyator.app.data.local.FamilyEventDao
import com.kalenyator.app.data.local.FamilyEventEntity
import com.kalenyator.app.data.model.FamilyEventType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

data class FamilyCountdown(
    val event: FamilyEventEntity,
    val target: LocalDateTime,
    val millisLeft: Long
)

class FamilyRepository(private val dao: FamilyEventDao) {
    fun observeAll(): Flow<List<FamilyEventEntity>> = dao.observeAll()

    fun observeForDate(month: Int, day: Int): Flow<List<FamilyEventEntity>> =
        dao.observeForDate(month, day)

    suspend fun insert(event: FamilyEventEntity): Long = dao.insert(event)

    suspend fun update(event: FamilyEventEntity) = dao.update(event)

    suspend fun delete(event: FamilyEventEntity) = dao.delete(event)

    fun daysUntil(event: FamilyEventEntity, today: LocalDate = LocalDate.now()): Long {
        var next = LocalDate.of(today.year, event.month, event.day)
        if (next.isBefore(today)) next = next.plusYears(1)
        if (next.isEqual(today)) return 0
        return ChronoUnit.DAYS.between(today, next)
    }

    fun turningAge(event: FamilyEventEntity, today: LocalDate = LocalDate.now()): Int? {
        val birthYear = event.year ?: return null
        if (event.type != FamilyEventType.BIRTHDAY) return null
        var birthday = LocalDate.of(today.year, event.month, event.day)
        if (birthday.isBefore(today)) birthday = birthday.plusYears(1)
        return birthday.year - birthYear
    }

    /** Возраст именинника в конкретный день (если в этот день у него ДР). */
    fun birthdayAgeOnDate(event: FamilyEventEntity, date: LocalDate): Int? {
        val birthYear = event.year ?: return null
        if (event.type != FamilyEventType.BIRTHDAY) return null
        if (event.month != date.monthValue || event.day != date.dayOfMonth) return null
        return (date.year - birthYear).coerceAtLeast(0)
    }

    fun nextOccurrenceDateTime(event: FamilyEventEntity, now: LocalDateTime = LocalDateTime.now()): LocalDateTime {
        var date = LocalDate.of(now.year, event.month, event.day)
        if (!date.isAfter(now.toLocalDate())) date = date.plusYears(1)
        return date.atStartOfDay()
    }

    fun nearestCountdown(events: List<FamilyEventEntity>, now: LocalDateTime = LocalDateTime.now()): FamilyCountdown? {
        return events.minByOrNull { nextOccurrenceDateTime(it, now) }
            ?.let { event ->
                val target = nextOccurrenceDateTime(event, now)
                val millis = ChronoUnit.MILLIS.between(now, target).coerceAtLeast(0)
                FamilyCountdown(event, target, millis)
            }
    }

    fun formatCountdown(millis: Long): CountdownParts {
        val totalSec = millis / 1000
        val days = totalSec / 86400
        val hours = (totalSec % 86400) / 3600
        val minutes = (totalSec % 3600) / 60
        val seconds = totalSec % 60
        return CountdownParts(days, hours, minutes, seconds)
    }
}

data class CountdownParts(val days: Long, val hours: Long, val minutes: Long, val seconds: Long)
