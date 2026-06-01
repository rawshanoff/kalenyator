package com.kalenyator.app.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kalenyator.app.data.holidays.HolidayProvider
import com.kalenyator.app.data.holidays.PublicHoliday
import com.kalenyator.app.data.local.FamilyEventEntity
import com.kalenyator.app.data.model.FamilyEventType
import com.kalenyator.app.data.model.HolidayRegion
import com.kalenyator.app.data.repository.FamilyRepository
import com.kalenyator.app.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.YearMonth

data class CalendarDay(
    val day: Int?,
    val isToday: Boolean = false,
    val holidays: List<PublicHoliday> = emptyList(),
    val familyCount: Int = 0,
    val familyTypes: List<FamilyEventType> = emptyList(),
    val isSelected: Boolean = false
)

data class CalendarUiState(
    val yearMonth: YearMonth = YearMonth.now(),
    val region: HolidayRegion = HolidayRegion.UZBEKISTAN,
    val days: List<CalendarDay> = emptyList(),
    val selectedDate: LocalDate? = LocalDate.now(),
    val selectedHolidays: List<PublicHoliday> = emptyList(),
    val selectedFamily: List<FamilyEventEntity> = emptyList()
)

class CalendarViewModel(
    private val familyRepository: FamilyRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {
    private val yearMonth = MutableStateFlow(YearMonth.now())
    private val selected = MutableStateFlow<LocalDate?>(LocalDate.now())
    private val allFamily = familyRepository.observeAll()

    val uiState: StateFlow<CalendarUiState> = combine(
        yearMonth,
        selected,
        settingsRepository.holidayRegion,
        allFamily
    ) { ym, sel, region, family ->
        buildState(ym, sel, region, family)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CalendarUiState())

    fun previousMonth() {
        yearMonth.update { it.minusMonths(1) }
    }

    fun nextMonth() {
        yearMonth.update { it.plusMonths(1) }
    }

    fun goToToday() {
        val today = LocalDate.now()
        yearMonth.value = YearMonth.from(today)
        selected.value = today
    }

    fun selectDay(day: Int) {
        val ym = yearMonth.value
        selected.value = LocalDate.of(ym.year, ym.monthValue, day)
    }

    fun birthdayAgeOnDate(event: FamilyEventEntity, date: LocalDate): Int? =
        familyRepository.birthdayAgeOnDate(event, date)

    private fun buildState(
        ym: YearMonth,
        sel: LocalDate?,
        region: HolidayRegion,
        family: List<FamilyEventEntity>
    ): CalendarUiState {
        val today = LocalDate.now()
        val first = ym.atDay(1)
        val startOffset = (first.dayOfWeek.value + 6) % 7
        val daysInMonth = ym.lengthOfMonth()
        val days = mutableListOf<CalendarDay>()
        repeat(startOffset) { days.add(CalendarDay(day = null)) }
        for (d in 1..daysInMonth) {
            val date = LocalDate.of(ym.year, ym.monthValue, d)
            val holidays = HolidayProvider.holidaysOn(ym.monthValue, d, region)
            val familyOnDay = family.filter { it.month == ym.monthValue && it.day == d }
            days.add(
                CalendarDay(
                    day = d,
                    isToday = date == today,
                    holidays = holidays,
                    familyCount = familyOnDay.size,
                    familyTypes = familyOnDay.map { it.type },
                    isSelected = sel?.let { it.year == ym.year && it.monthValue == ym.monthValue && it.dayOfMonth == d } == true
                )
            )
        }
        val selectedHolidays = sel?.let {
            HolidayProvider.holidaysOn(it.monthValue, it.dayOfMonth, region)
        } ?: emptyList()
        val selectedFamily = sel?.let { date ->
            family.filter { it.month == date.monthValue && it.day == date.dayOfMonth }
        } ?: emptyList()
        return CalendarUiState(
            yearMonth = ym,
            region = region,
            days = days,
            selectedDate = sel,
            selectedHolidays = selectedHolidays,
            selectedFamily = selectedFamily
        )
    }
}
