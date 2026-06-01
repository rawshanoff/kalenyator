package com.kalenyator.app.data.settings

data class ReminderSettings(
    val enabled: Boolean = true,
    val daysBefore: Int = 3,
    val remindOnDay: Boolean = true,
    val reminderHour: Int = 9
)
