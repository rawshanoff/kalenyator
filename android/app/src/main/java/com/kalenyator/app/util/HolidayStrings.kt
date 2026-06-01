package com.kalenyator.app.util

import android.content.Context
import com.kalenyator.app.R
import com.kalenyator.app.data.holidays.HolidayKind

fun holidayName(context: Context, kind: HolidayKind): String = when (kind) {
    HolidayKind.NEW_YEAR -> context.getString(R.string.holiday_new_year)
    HolidayKind.WOMEN -> context.getString(R.string.holiday_women)
    HolidayKind.NAVRUZ -> context.getString(R.string.holiday_navruz)
    HolidayKind.MEMORY -> context.getString(R.string.holiday_memory)
    HolidayKind.INDEPENDENCE_UZ -> context.getString(R.string.holiday_independence_uz)
    HolidayKind.TEACHERS -> context.getString(R.string.holiday_teachers)
    HolidayKind.CONSTITUTION_UZ -> context.getString(R.string.holiday_constitution_uz)
    HolidayKind.DEFENDER -> context.getString(R.string.holiday_defender)
    HolidayKind.LABOR -> context.getString(R.string.holiday_labor)
    HolidayKind.VICTORY -> context.getString(R.string.holiday_victory)
    HolidayKind.RUSSIA_DAY -> context.getString(R.string.holiday_russia_day)
    HolidayKind.UNITY -> context.getString(R.string.holiday_unity)
    HolidayKind.CONSTITUTION_RU -> context.getString(R.string.holiday_constitution_ru)
}

fun monthName(context: Context, month: Int): String = when (month) {
    1 -> context.getString(R.string.month_1)
    2 -> context.getString(R.string.month_2)
    3 -> context.getString(R.string.month_3)
    4 -> context.getString(R.string.month_4)
    5 -> context.getString(R.string.month_5)
    6 -> context.getString(R.string.month_6)
    7 -> context.getString(R.string.month_7)
    8 -> context.getString(R.string.month_8)
    9 -> context.getString(R.string.month_9)
    10 -> context.getString(R.string.month_10)
    11 -> context.getString(R.string.month_11)
    12 -> context.getString(R.string.month_12)
    else -> ""
}

fun weatherEmoji(code: Int): String = when (code) {
    0 -> "☀️"
    1, 2, 3 -> "⛅"
    45, 48 -> "🌫️"
    51, 53, 55, 56, 57 -> "🌦️"
    61, 63, 65, 66, 67, 80, 81, 82 -> "🌧️"
    71, 73, 75, 77, 85, 86 -> "❄️"
    95, 96, 99 -> "⛈️"
    else -> "🌤️"
}
