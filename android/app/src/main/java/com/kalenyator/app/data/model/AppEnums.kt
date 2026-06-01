package com.kalenyator.app.data.model

enum class AppLanguage(val code: String) {
    RU("ru"),
    UZ("uz")
}

enum class HolidayRegion {
    UZBEKISTAN,
    RUSSIA,
    BOTH
}

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class AppVisualTheme {
    TENTAKO,
    SPACE,
    MINIMAL,
    NAVRUZ,
    NEW_YEAR
}

enum class FamilyEventType {
    BIRTHDAY,
    ANNIVERSARY,
    OTHER
}
