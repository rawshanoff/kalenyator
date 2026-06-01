package com.kalenyator.app.ui.family

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.kalenyator.app.R
import com.kalenyator.app.data.local.FamilyEventEntity
import com.kalenyator.app.data.model.FamilyEventType
import com.kalenyator.app.ui.LocalViewModelFactory
import com.kalenyator.app.ui.components.DayMonthWheelPicker
import com.kalenyator.app.ui.theme.TentakoPink
import com.kalenyator.app.util.DateFormatUtil
import com.kalenyator.app.util.monthName
import java.io.File

@Composable
fun FamilyScreen(viewModel: FamilyViewModel = viewModel(factory = LocalViewModelFactory.current)) {
    val events by viewModel.events.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<FamilyEventEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<FamilyEventEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editing = null; showDialog = true },
                containerColor = TentakoPink
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.family_add))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.family_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = TentakoPink
                )
            }
            if (events.isNotEmpty()) {
                val star = viewModel.starOfWeekEvent(events)
                if (star != null) {
                    item {
                        FamilyWeeklyHeroCard(
                            event = star,
                            daysUntil = viewModel.daysUntil(star),
                            age = viewModel.turningAge(star)
                        )
                    }
                } else {
                    item {
                        Text(
                            stringResource(R.string.family_star_empty),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                item { FamilyCountdownCard(events = events, viewModel = viewModel) }
            }
            if (events.isEmpty()) {
                item {
                    Text(stringResource(R.string.family_empty), style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                items(events, key = { it.id }) { event ->
                    FamilyEventCard(
                        event = event,
                        daysUntil = viewModel.daysUntil(event),
                        age = viewModel.turningAge(event),
                        onEdit = { editing = event; showDialog = true },
                        onDelete = { deleteTarget = event }
                    )
                }
            }
            item { Spacer(Modifier.height(72.dp)) }
        }
    }

    if (showDialog) {
        FamilyEventDialog(
            initial = editing,
            onDismiss = { showDialog = false },
            onSave = { id, title, type, month, day, year, note, photoPath, photoUri, removePhoto ->
                viewModel.save(id, title, type, month, day, year, note, photoPath, photoUri, removePhoto)
                showDialog = false
            }
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(stringResource(R.string.family_delete)) },
            text = { Text(stringResource(R.string.family_confirm_delete, target.title)) },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(target); deleteTarget = null }) {
                    Text(stringResource(R.string.family_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(stringResource(R.string.family_cancel))
                }
            }
        )
    }
}

@Composable
private fun FamilyEventCard(
    event: FamilyEventEntity,
    daysUntil: Long,
    age: Int?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val emoji = when (event.type) {
        FamilyEventType.BIRTHDAY -> "🎂"
        FamilyEventType.ANNIVERSARY -> "💍"
        FamilyEventType.OTHER -> "⭐"
    }
    val countdown = if (daysUntil == 0L) stringResource(R.string.days_until_today)
    else stringResource(R.string.days_until, daysUntil.toInt())

    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            event.photoPath?.let { path ->
                AsyncImage(
                    model = File(path),
                    contentDescription = null,
                    modifier = Modifier.size(52.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.size(12.dp))
            }
            Column(Modifier.weight(1f)) {
                Text("$emoji ${event.title}", fontWeight = FontWeight.SemiBold)
                Text(DateFormatUtil.format(event.day, event.month, event.year))
                Text(countdown, style = MaterialTheme.typography.labelMedium, color = TentakoPink)
                age?.let { Text(stringResource(R.string.age_turns, it), style = MaterialTheme.typography.bodySmall) }
                event.note?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.family_edit))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.family_delete))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FamilyEventDialog(
    initial: FamilyEventEntity?,
    onDismiss: () -> Unit,
    onSave: (Long?, String, FamilyEventType, Int, Int, Int?, String?, String?, Uri?, Boolean) -> Unit
) {
    var title by remember(initial) { mutableStateOf(initial?.title ?: "") }
    var note by remember(initial) { mutableStateOf(initial?.note ?: "") }
    var month by remember(initial) { mutableIntStateOf(initial?.month ?: 1) }
    var day by remember(initial) { mutableIntStateOf(initial?.day ?: 1) }
    var yearText by remember(initial) { mutableStateOf(initial?.year?.toString() ?: "") }
    var type by remember(initial) { mutableStateOf(initial?.type ?: FamilyEventType.BIRTHDAY) }
    var photoPath by remember(initial) { mutableStateOf(initial?.photoPath) }
    var newPhotoUri by remember(initial) { mutableStateOf<Uri?>(null) }
    var removePhoto by remember(initial) { mutableStateOf(false) }
    val context = LocalContext.current

    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            newPhotoUri = uri
            removePhoto = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) stringResource(R.string.family_add) else stringResource(R.string.family_edit)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val showPhoto = !removePhoto && (newPhotoUri != null || photoPath != null)
                if (showPhoto) {
                    val model: Any = newPhotoUri ?: File(photoPath!!)
                    AsyncImage(
                        model = model,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp).clip(CircleShape).align(Alignment.CenterHorizontally),
                        contentScale = ContentScale.Crop
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { pickPhoto.launch("image/*") }, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.family_photo_add))
                    }
                    if (showPhoto) {
                        OutlinedButton(onClick = { removePhoto = true; newPhotoUri = null }) {
                            Text(stringResource(R.string.family_photo_remove))
                        }
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.family_name)) },
                    singleLine = true
                )
                Text(stringResource(R.string.family_type), style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FamilyEventType.entries.forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(typeLabel(t)) }
                        )
                    }
                }
                DayMonthWheelPicker(
                    day = day,
                    month = month,
                    onDayChange = { day = it },
                    onMonthChange = { month = it },
                    monthLabel = { monthName(context, it) },
                    dayLabel = stringResource(R.string.family_day),
                    monthTitle = stringResource(R.string.family_month)
                )
                OutlinedTextField(
                    value = yearText,
                    onValueChange = { yearText = it.filter { c -> c.isDigit() }.take(4) },
                    label = { Text(stringResource(R.string.family_year_optional)) },
                    singleLine = true
                )
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text(stringResource(R.string.family_note)) })
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            initial?.id, title, type, month, day,
                            yearText.toIntOrNull(), note.ifBlank { null },
                            photoPath, newPhotoUri, removePhoto
                        )
                    }
                }
            ) { Text(stringResource(R.string.family_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.family_cancel)) } }
    )
}

@Composable
private fun typeLabel(type: FamilyEventType): String = when (type) {
    FamilyEventType.BIRTHDAY -> stringResource(R.string.family_type_birthday)
    FamilyEventType.ANNIVERSARY -> stringResource(R.string.family_type_anniversary)
    FamilyEventType.OTHER -> stringResource(R.string.family_type_other)
}
