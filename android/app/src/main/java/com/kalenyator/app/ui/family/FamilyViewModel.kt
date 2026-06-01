package com.kalenyator.app.ui.family

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kalenyator.app.data.local.FamilyEventEntity
import com.kalenyator.app.data.model.FamilyEventType
import com.kalenyator.app.data.reminder.ReminderRepository
import com.kalenyator.app.data.reminder.ReminderScheduler
import com.kalenyator.app.data.repository.FamilyRepository
import com.kalenyator.app.util.PhotoStorage
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class FamilyViewModel(
    private val repository: FamilyRepository,
    private val reminderRepository: ReminderRepository,
    private val reminderScheduler: ReminderScheduler,
    private val appContext: Context
) : ViewModel() {
    val events = repository.observeAll()
        .map { list ->
            val today = LocalDate.now()
            list.sortedWith(compareBy({ repository.daysUntil(it, today) }, { it.month }, { it.day }, { it.title }))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun save(
        id: Long?,
        title: String,
        type: FamilyEventType,
        month: Int,
        day: Int,
        year: Int?,
        note: String?,
        existingPhotoPath: String?,
        newPhotoUri: Uri?,
        removePhoto: Boolean
    ) {
        viewModelScope.launch {
            var photoPath = existingPhotoPath
            if (removePhoto) {
                PhotoStorage.delete(appContext, photoPath)
                photoPath = null
            }

            val base = FamilyEventEntity(
                id = id ?: 0,
                title = title.trim(),
                type = type,
                month = month,
                day = day,
                year = year,
                note = note?.trim()?.takeIf { it.isNotEmpty() },
                photoPath = photoPath
            )

            val savedId = if (id == null || id == 0L) {
                repository.insert(base.copy(id = 0))
            } else {
                repository.update(base)
                reminderRepository.clearNotifiedKeysForEvent(id)
                id
            }

            if (newPhotoUri != null) {
                PhotoStorage.delete(appContext, photoPath)
                photoPath = PhotoStorage.saveFromUri(appContext, savedId, newPhotoUri)
                repository.update(base.copy(id = savedId, photoPath = photoPath))
            }

            if (id == null || id == 0L) {
                reminderRepository.clearNotifiedKeysForEvent(savedId)
            }
            reminderRepository.refreshWidget()
            reminderScheduler.reschedule()
        }
    }

    fun delete(event: FamilyEventEntity) {
        viewModelScope.launch {
            PhotoStorage.delete(appContext, event.photoPath)
            repository.delete(event)
            reminderRepository.clearNotifiedKeysForEvent(event.id)
            reminderRepository.refreshWidget()
            reminderScheduler.reschedule()
        }
    }

    fun daysUntil(event: FamilyEventEntity): Long = repository.daysUntil(event, LocalDate.now())

    fun turningAge(event: FamilyEventEntity): Int? = repository.turningAge(event, LocalDate.now())

    fun nearestCountdown(events: List<FamilyEventEntity>) =
        repository.nearestCountdown(events)

    /** Именинник недели — только если до ДР/события меньше 7 дней. */
    fun starOfWeekEvent(events: List<FamilyEventEntity>): FamilyEventEntity? {
        val today = LocalDate.now()
        return events
            .map { it to repository.daysUntil(it, today) }
            .filter { (_, days) -> days in 0 until 7 }
            .minByOrNull { (_, days) -> days }
            ?.first
    }

    fun formatCountdown(millis: Long) = repository.formatCountdown(millis)
}
