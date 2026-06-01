package com.kalenyator.app.ui.family

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kalenyator.app.R
import com.kalenyator.app.data.local.FamilyEventEntity
import com.kalenyator.app.ui.theme.TentakoPink
import com.kalenyator.app.util.DateFormatUtil
import com.kalenyator.app.util.GreetingTemplates
import java.io.File

@Composable
fun FamilyWeeklyHeroCard(
    event: FamilyEventEntity,
    daysUntil: Long,
    age: Int?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showPicker by remember { mutableStateOf(false) }
    val templates = remember(event, daysUntil, age) {
        GreetingTemplates.all(context, event, daysUntil, age)
    }
    val countdown = when (daysUntil) {
        0L -> stringResource(R.string.days_until_today)
        else -> stringResource(R.string.days_until, daysUntil.toInt())
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.family_star_of_week),
                style = MaterialTheme.typography.labelLarge,
                color = TentakoPink,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                event.photoPath?.let { path ->
                    AsyncImage(
                        model = File(path),
                        contentDescription = null,
                        modifier = Modifier.size(72.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.size(12.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(event.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(DateFormatUtil.format(event.day, event.month, event.year), style = MaterialTheme.typography.bodySmall)
                    Text(countdown, style = MaterialTheme.typography.titleMedium, color = TentakoPink)
                    age?.let {
                        Text(
                            stringResource(R.string.age_turns, it),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            FilledTonalButton(
                onClick = { showPicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.greeting_share))
            }
        }
    }

    if (showPicker) {
        GreetingPickerDialog(
            templates = templates,
            onDismiss = { showPicker = false }
        )
    }
}
