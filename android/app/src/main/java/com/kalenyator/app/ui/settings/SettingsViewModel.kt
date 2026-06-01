package com.kalenyator.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kalenyator.app.data.model.AppLanguage
import com.kalenyator.app.data.model.AppThemeMode
import com.kalenyator.app.data.model.AppVisualTheme
import com.kalenyator.app.data.model.HolidayRegion
import com.kalenyator.app.data.reminder.ReminderScheduler
import com.kalenyator.app.data.settings.ReminderSettings
import com.kalenyator.app.data.settings.SettingsRepository
import com.kalenyator.app.data.sync.SyncRepository
import com.kalenyator.app.util.LocaleApplicator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val syncRepository: SyncRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {
    val language = settingsRepository.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppLanguage.RU)
    val region = settingsRepository.holidayRegion
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HolidayRegion.UZBEKISTAN)
    val themeMode = settingsRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppThemeMode.SYSTEM)
    val visualTheme = settingsRepository.visualTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppVisualTheme.TENTAKO)
    val reminderSettings = settingsRepository.reminderSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReminderSettings())

    suspend fun applyLanguage(lang: AppLanguage) {
        settingsRepository.setLanguage(lang)
        LocaleApplicator.apply(lang)
    }

    fun setRegion(region: HolidayRegion) = viewModelScope.launch {
        settingsRepository.setHolidayRegion(region)
    }

    fun setTheme(mode: AppThemeMode) = viewModelScope.launch {
        settingsRepository.setThemeMode(mode)
    }

    fun setVisualTheme(theme: AppVisualTheme) = viewModelScope.launch {
        settingsRepository.setVisualTheme(theme)
    }

    fun setRemindersEnabled(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.setRemindersEnabled(enabled)
        if (enabled) reminderScheduler.schedule() else reminderScheduler.cancel()
    }

    fun setReminderDaysBefore(days: Int) = viewModelScope.launch {
        settingsRepository.setReminderDaysBefore(days)
        reminderScheduler.reschedule()
    }

    fun setRemindOnDay(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.setRemindOnDay(enabled)
        reminderScheduler.reschedule()
    }

    fun setReminderHour(hour: Int) = viewModelScope.launch {
        settingsRepository.setReminderHour(hour)
        reminderScheduler.reschedule()
    }

    suspend fun exportJson() = syncRepository.exportToCache()
    suspend fun exportJsonString() = syncRepository.exportJsonString()
    suspend fun importJson(json: String) = syncRepository.importFromJson(json)
}
