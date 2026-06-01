package com.kalenyator.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kalenyator.app.ui.theme.TentakoPink
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.Month

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DayMonthWheelPicker(
    day: Int,
    month: Int,
    onDayChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    monthLabel: (Int) -> String,
    dayLabel: String,
    monthTitle: String,
    modifier: Modifier = Modifier
) {
    val maxDay = Month.of(month.coerceIn(1, 12)).maxLength()
    val safeDay = day.coerceIn(1, maxDay)

  LaunchedEffect(month) {
        if (day > maxDay) onDayChange(maxDay)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IntWheel(
            title = dayLabel,
            values = (1..maxDay).toList(),
            selected = safeDay,
            onSelected = onDayChange,
            format = { it.toString() },
            modifier = Modifier.weight(1f)
        )
        IntWheel(
            title = monthTitle,
            values = (1..12).toList(),
            selected = month.coerceIn(1, 12),
            onSelected = onMonthChange,
            format = monthLabel,
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun IntWheel(
    title: String,
    values: List<Int>,
    selected: Int,
    onSelected: (Int) -> Unit,
    format: (Int) -> String,
    modifier: Modifier = Modifier
) {
    val startIndex = values.indexOf(selected).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)
    val fling = rememberSnapFlingBehavior(listState)

    LaunchedEffect(selected, values) {
        val idx = values.indexOf(selected)
        if (idx >= 0 && idx != listState.firstVisibleItemIndex) {
            listState.scrollToItem(idx)
        }
    }

    LaunchedEffect(listState, values) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { idx ->
                values.getOrNull(idx)?.let { onSelected(it) }
            }
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.labelMedium, color = TentakoPink)
        Box(
            modifier = Modifier
                .height(140.dp)
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            LazyColumn(
                state = listState,
                flingBehavior = fling,
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(vertical = 52.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(values) { _, value ->
                    val isSelected = value == selected
                    Text(
                        text = format(value),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelected(value)
                                listState.requestScrollToItem(values.indexOf(value))
                            }
                            .padding(vertical = 8.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        style = if (isSelected) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                        color = if (isSelected) TentakoPink else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 8.dp)
            )
        }
    }
}

fun maxDayInMonth(month: Int): Int = Month.of(month.coerceIn(1, 12)).maxLength()
