package com.kalenyator.app.data.holidays

import com.kalenyator.app.data.model.HolidayRegion

enum class HolidayKind {
    NEW_YEAR,
    WOMEN,
    NAVRUZ,
    MEMORY,
    INDEPENDENCE_UZ,
    TEACHERS,
    CONSTITUTION_UZ,
    DEFENDER,
    LABOR,
    VICTORY,
    RUSSIA_DAY,
    UNITY,
    CONSTITUTION_RU
}

data class PublicHoliday(
    val month: Int,
    val day: Int,
    val kind: HolidayKind,
    val region: HolidayRegion
)

object HolidayProvider {
    private val uzHolidays = listOf(
        PublicHoliday(1, 1, HolidayKind.NEW_YEAR, HolidayRegion.UZBEKISTAN),
        PublicHoliday(3, 8, HolidayKind.WOMEN, HolidayRegion.UZBEKISTAN),
        PublicHoliday(3, 21, HolidayKind.NAVRUZ, HolidayRegion.UZBEKISTAN),
        PublicHoliday(5, 9, HolidayKind.MEMORY, HolidayRegion.UZBEKISTAN),
        PublicHoliday(9, 1, HolidayKind.INDEPENDENCE_UZ, HolidayRegion.UZBEKISTAN),
        PublicHoliday(10, 1, HolidayKind.TEACHERS, HolidayRegion.UZBEKISTAN),
        PublicHoliday(12, 8, HolidayKind.CONSTITUTION_UZ, HolidayRegion.UZBEKISTAN)
    )

    private val ruHolidays = listOf(
        PublicHoliday(1, 1, HolidayKind.NEW_YEAR, HolidayRegion.RUSSIA),
        PublicHoliday(2, 23, HolidayKind.DEFENDER, HolidayRegion.RUSSIA),
        PublicHoliday(3, 8, HolidayKind.WOMEN, HolidayRegion.RUSSIA),
        PublicHoliday(5, 1, HolidayKind.LABOR, HolidayRegion.RUSSIA),
        PublicHoliday(5, 9, HolidayKind.VICTORY, HolidayRegion.RUSSIA),
        PublicHoliday(6, 12, HolidayKind.RUSSIA_DAY, HolidayRegion.RUSSIA),
        PublicHoliday(11, 4, HolidayKind.UNITY, HolidayRegion.RUSSIA),
        PublicHoliday(12, 12, HolidayKind.CONSTITUTION_RU, HolidayRegion.RUSSIA)
    )

    fun holidaysFor(region: HolidayRegion): List<PublicHoliday> = when (region) {
        HolidayRegion.UZBEKISTAN -> uzHolidays
        HolidayRegion.RUSSIA -> ruHolidays
        HolidayRegion.BOTH -> (uzHolidays + ruHolidays).distinctBy { it.month to it.day to it.kind }
    }

    fun holidaysOn(month: Int, day: Int, region: HolidayRegion): List<PublicHoliday> =
        holidaysFor(region).filter { it.month == month && it.day == day }
}
