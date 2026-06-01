package com.kalenyator.app.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kalenyator.app.R
import com.kalenyator.app.ui.LocalViewModelFactory
import com.kalenyator.app.data.holidays.HolidayKind
import com.kalenyator.app.data.model.FamilyEventType
import com.kalenyator.app.ui.theme.TentakoGold
import com.kalenyator.app.ui.theme.HolidayConstitution
import com.kalenyator.app.ui.theme.HolidayIndependence
import com.kalenyator.app.ui.theme.HolidayMemory
import com.kalenyator.app.ui.theme.HolidayNavruz
import com.kalenyator.app.ui.theme.HolidayNewYear
import com.kalenyator.app.ui.theme.HolidayTeachers
import com.kalenyator.app.ui.theme.HolidayWomen
import com.kalenyator.app.ui.theme.TentakoPink
import com.kalenyator.app.util.DateFormatUtil
import com.kalenyator.app.util.holidayName
import com.kalenyator.app.util.monthName
import java.time.LocalDate

@Composable
fun CalendarScreen(viewModel: CalendarViewModel = viewModel(factory = LocalViewModelFactory.current)) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val weekdays = listOf(
        R.string.weekday_mon, R.string.weekday_tue, R.string.weekday_wed,
        R.string.weekday_thu, R.string.weekday_fri, R.string.weekday_sat, R.string.weekday_sun
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("📅 ${stringResource(R.string.nav_calendar)}", style = MaterialTheme.typography.headlineSmall, color = TentakoPink)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            IconButton(onClick = viewModel::previousMonth) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = stringResource(R.string.cal_prev))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(state.yearMonth.year.toString(), style = MaterialTheme.typography.labelLarge)
                Text(monthName(context, state.yearMonth.monthValue), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = viewModel::nextMonth) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = stringResource(R.string.cal_next))
            }
        }
        FilledTonalButton(onClick = viewModel::goToToday, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(stringResource(R.string.cal_today, DateFormatUtil.format(LocalDate.now())))
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth()) {
            weekdays.forEach { res ->
                Text(
                    stringResource(res),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(280.dp),
            userScrollEnabled = false
        ) {
            items(state.days) { day ->
                CalendarDayCell(day, onClick = { d -> viewModel.selectDay(d) })
            }
        }
        Spacer(Modifier.height(16.dp))
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.padding(16.dp)) {
                val sel = state.selectedDate
                Text(
                    if (sel != null) DateFormatUtil.format(sel) else stringResource(R.string.cal_select_day),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                state.selectedHolidays.forEach { h ->
                    Text("🎉 ${holidayName(context, h.kind)}", style = MaterialTheme.typography.bodyMedium)
                }
                state.selectedFamily.forEach { e ->
                    val emoji = when (e.type) {
                        FamilyEventType.BIRTHDAY -> "🎂"
                        FamilyEventType.ANNIVERSARY -> "💍"
                        FamilyEventType.OTHER -> "⭐"
                    }
                    Text("$emoji ${e.title}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text(DateFormatUtil.format(e.day, e.month, e.year), style = MaterialTheme.typography.bodySmall)
                    if (sel != null) {
                        viewModel.birthdayAgeOnDate(e, sel)?.let { age ->
                            Text(
                                stringResource(R.string.cal_birthday_age, age),
                                style = MaterialTheme.typography.bodySmall,
                                color = TentakoPink
                            )
                        }
                    }
                    e.note?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                }
                if (state.selectedHolidays.isEmpty() && state.selectedFamily.isEmpty()) {
                    Text(stringResource(R.string.cal_no_events), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(day: CalendarDay, onClick: (Int) -> Unit) {
    val d = day.day
    if (d == null) {
        Box(Modifier.aspectRatio(1f))
        return
    }
    val holidayColor = day.holidays.firstOrNull()?.kind?.let { holidayColor(it) }
    val familyColor = day.familyTypes.firstOrNull()?.let { familyEventColor(it) }
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(
                when {
                    day.isSelected -> TentakoPink
                    day.isToday -> TentakoPink.copy(alpha = 0.25f)
                    familyColor != null -> familyColor.copy(alpha = 0.45f)
                    holidayColor != null -> holidayColor.copy(alpha = 0.35f)
                    else -> Color.Transparent
                }
            )
            .then(
                if (day.isToday && !day.isSelected) Modifier.border(2.dp, TentakoPink, CircleShape)
                else Modifier
            )
            .clickable { onClick(d) },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                d.toString(),
                color = if (day.isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
                style = MaterialTheme.typography.bodyMedium
            )
            if (day.familyCount > 0) {
                val dotColor = day.familyTypes.firstOrNull()?.let { familyEventColor(it) } ?: TentakoPink
                Text("•", color = dotColor, modifier = Modifier.size(8.dp))
            }
        }
    }
}

private fun familyEventColor(type: FamilyEventType): Color = when (type) {
    FamilyEventType.BIRTHDAY -> TentakoPink
    FamilyEventType.ANNIVERSARY -> TentakoGold
    FamilyEventType.OTHER -> Color(0xFF9B59B6)
}

private fun holidayColor(kind: HolidayKind): Color = when (kind) {
    HolidayKind.NEW_YEAR -> HolidayNewYear
    HolidayKind.WOMEN -> HolidayWomen
    HolidayKind.NAVRUZ -> HolidayNavruz
    HolidayKind.MEMORY, HolidayKind.VICTORY -> HolidayMemory
    HolidayKind.INDEPENDENCE_UZ -> HolidayIndependence
    HolidayKind.TEACHERS -> HolidayTeachers
    HolidayKind.CONSTITUTION_UZ, HolidayKind.CONSTITUTION_RU -> HolidayConstitution
    else -> TentakoPink
}
