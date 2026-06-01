package com.kalenyator.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kalenyator.app.AppContainer
import com.kalenyator.app.ui.calculator.CalculatorViewModel
import com.kalenyator.app.ui.calendar.CalendarViewModel
import com.kalenyator.app.ui.family.FamilyViewModel
import com.kalenyator.app.ui.settings.SettingsViewModel
import com.kalenyator.app.ui.weather.WeatherViewModel

class AppViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(CalculatorViewModel::class.java) ->
            CalculatorViewModel(container.settingsRepository) as T
        modelClass.isAssignableFrom(CalendarViewModel::class.java) ->
            CalendarViewModel(container.familyRepository, container.settingsRepository) as T
        modelClass.isAssignableFrom(WeatherViewModel::class.java) ->
            WeatherViewModel(container.weatherRepository, container.settingsRepository) as T
        modelClass.isAssignableFrom(FamilyViewModel::class.java) ->
            FamilyViewModel(
                container.familyRepository,
                container.reminderRepository,
                container.reminderScheduler,
                container.app
            ) as T
        modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
            SettingsViewModel(
                container.settingsRepository,
                container.syncRepository,
                container.reminderScheduler
            ) as T
        else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
