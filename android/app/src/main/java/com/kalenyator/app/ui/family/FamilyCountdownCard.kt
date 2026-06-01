package com.kalenyator.app.ui.family

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kalenyator.app.R
import com.kalenyator.app.data.local.FamilyEventEntity
import com.kalenyator.app.data.repository.FamilyCountdown
import com.kalenyator.app.ui.theme.TentakoPink
import kotlinx.coroutines.delay
import java.time.LocalDateTime

@Composable
fun FamilyCountdownCard(
    events: List<FamilyEventEntity>,
    viewModel: FamilyViewModel,
    modifier: Modifier = Modifier
) {
    if (events.isEmpty()) return

    var tick by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            tick = System.currentTimeMillis()
        }
    }

    val countdown: FamilyCountdown? = remember(events, tick) {
        viewModel.nearestCountdown(events)
    }
    if (countdown == null) return

    val millisLeft = remember(countdown, tick) {
        java.time.temporal.ChronoUnit.MILLIS.between(LocalDateTime.now(), countdown.target).coerceAtLeast(0)
    }
    val parts = remember(millisLeft) { viewModel.formatCountdown(millisLeft) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.family_countdown_title, countdown.event.title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(
                    R.string.family_countdown_format,
                    parts.days, parts.hours, parts.minutes, parts.seconds
                ),
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp),
                color = TentakoPink,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
