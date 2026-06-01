package com.kalenyator.app.ui.settings

import android.Manifest
import android.graphics.Bitmap
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.activity.ComponentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kalenyator.app.R
import com.kalenyator.app.data.model.AppLanguage
import com.kalenyator.app.data.model.AppThemeMode
import com.kalenyator.app.data.model.AppVisualTheme
import com.kalenyator.app.data.model.HolidayRegion
import com.kalenyator.app.data.settings.SettingsRepository
import com.kalenyator.app.ui.LocalViewModelFactory
import com.kalenyator.app.ui.theme.TentakoPink
import com.kalenyator.app.util.QrCodeHelper
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(factory = LocalViewModelFactory.current),
    onExport: () -> Unit,
    onExportNew: () -> Unit,
    onImport: () -> Unit,
    onScanQr: () -> Unit,
    onCheckUpdate: () -> Unit
) {
    val language by viewModel.language.collectAsState()
    val region by viewModel.region.collectAsState()
    val theme by viewModel.themeMode.collectAsState()
    val visual by viewModel.visualTheme.collectAsState()
    val reminders by viewModel.reminderSettings.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showQrDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineSmall, color = TentakoPink)
        Spacer(Modifier.height(16.dp))

        SectionTitle(stringResource(R.string.settings_language))
        RadioRow(stringResource(R.string.settings_language_ru), language == AppLanguage.RU) {
            if (language != AppLanguage.RU) {
                scope.launch {
                    viewModel.applyLanguage(AppLanguage.RU)
                    (context as? ComponentActivity)?.recreate()
                }
            }
        }
        RadioRow(stringResource(R.string.settings_language_uz), language == AppLanguage.UZ) {
            if (language != AppLanguage.UZ) {
                scope.launch {
                    viewModel.applyLanguage(AppLanguage.UZ)
                    (context as? ComponentActivity)?.recreate()
                }
            }
        }

        HorizontalDivider(Modifier.padding(vertical = 12.dp))
        SectionTitle(stringResource(R.string.settings_region))
        RadioRow(stringResource(R.string.settings_region_uz), region == HolidayRegion.UZBEKISTAN) { viewModel.setRegion(HolidayRegion.UZBEKISTAN) }
        RadioRow(stringResource(R.string.settings_region_ru), region == HolidayRegion.RUSSIA) { viewModel.setRegion(HolidayRegion.RUSSIA) }
        RadioRow(stringResource(R.string.settings_region_both), region == HolidayRegion.BOTH) { viewModel.setRegion(HolidayRegion.BOTH) }

        HorizontalDivider(Modifier.padding(vertical = 12.dp))
        SectionTitle(stringResource(R.string.settings_visual_theme))
        RadioRow(stringResource(R.string.theme_tentako), visual == AppVisualTheme.TENTAKO) { viewModel.setVisualTheme(AppVisualTheme.TENTAKO) }
        RadioRow(stringResource(R.string.theme_space), visual == AppVisualTheme.SPACE) { viewModel.setVisualTheme(AppVisualTheme.SPACE) }
        RadioRow(stringResource(R.string.theme_minimal), visual == AppVisualTheme.MINIMAL) { viewModel.setVisualTheme(AppVisualTheme.MINIMAL) }
        RadioRow(stringResource(R.string.theme_navruz), visual == AppVisualTheme.NAVRUZ) { viewModel.setVisualTheme(AppVisualTheme.NAVRUZ) }
        RadioRow(stringResource(R.string.theme_new_year), visual == AppVisualTheme.NEW_YEAR) { viewModel.setVisualTheme(AppVisualTheme.NEW_YEAR) }

        HorizontalDivider(Modifier.padding(vertical = 12.dp))
        SectionTitle(stringResource(R.string.settings_theme))
        RadioRow(stringResource(R.string.settings_theme_system), theme == AppThemeMode.SYSTEM) { viewModel.setTheme(AppThemeMode.SYSTEM) }
        RadioRow(stringResource(R.string.settings_theme_light), theme == AppThemeMode.LIGHT) { viewModel.setTheme(AppThemeMode.LIGHT) }
        RadioRow(stringResource(R.string.settings_theme_dark), theme == AppThemeMode.DARK) { viewModel.setTheme(AppThemeMode.DARK) }

        HorizontalDivider(Modifier.padding(vertical = 12.dp))
        ReminderSection(
            enabled = reminders.enabled,
            daysBefore = reminders.daysBefore,
            remindOnDay = reminders.remindOnDay,
            reminderHour = reminders.reminderHour,
            onEnabledChange = { enabled ->
                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val granted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.POST_NOTIFICATIONS
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    if (!granted) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                viewModel.setRemindersEnabled(enabled)
            },
            onDaysChange = viewModel::setReminderDaysBefore,
            onRemindOnDayChange = viewModel::setRemindOnDay,
            onHourChange = viewModel::setReminderHour
        )

        HorizontalDivider(Modifier.padding(vertical = 12.dp))
        SectionTitle(stringResource(R.string.settings_sync))
        Text(stringResource(R.string.settings_sync_hint), style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onExport) { Text(stringResource(R.string.settings_sync_export)) }
        TextButton(onClick = onExportNew) { Text(stringResource(R.string.settings_sync_export_new)) }
        TextButton(onClick = onImport) { Text(stringResource(R.string.settings_sync_import)) }
        TextButton(onClick = {
            scope.launch {
                viewModel.exportJsonString().onSuccess { json ->
                    qrBitmap = QrCodeHelper.encode(json, 480)
                    showQrDialog = true
                }
            }
        }) { Text(stringResource(R.string.settings_sync_qr_show)) }
        TextButton(onClick = onScanQr) { Text(stringResource(R.string.settings_sync_qr_scan)) }
        Text(stringResource(R.string.settings_sync_qr_hint), style = MaterialTheme.typography.bodySmall)

        HorizontalDivider(Modifier.padding(vertical = 12.dp))
        SectionTitle(stringResource(R.string.settings_about))
        Text(stringResource(R.string.settings_about_text))
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.settings_changelog), style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onCheckUpdate) { Text(stringResource(R.string.settings_check_update)) }
    }

    if (showQrDialog && qrBitmap != null) {
        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            title = { Text(stringResource(R.string.settings_sync_qr_show)) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Image(
                        bitmap = qrBitmap!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(260.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showQrDialog = false }) { Text(stringResource(R.string.family_save)) }
            }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, color = TentakoPink)
    Spacer(Modifier.height(4.dp))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReminderSection(
    enabled: Boolean,
    daysBefore: Int,
    remindOnDay: Boolean,
    reminderHour: Int,
    onEnabledChange: (Boolean) -> Unit,
    onDaysChange: (Int) -> Unit,
    onRemindOnDayChange: (Boolean) -> Unit,
    onHourChange: (Int) -> Unit
) {
    SectionTitle(stringResource(R.string.settings_reminders))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.settings_reminders_enable))
            Text(stringResource(R.string.settings_reminders_hint), style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = enabled, onCheckedChange = onEnabledChange)
    }
    if (enabled) {
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.settings_reminders_on_day))
            Switch(checked = remindOnDay, onCheckedChange = onRemindOnDayChange)
        }
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.settings_reminders_days_label), style = MaterialTheme.typography.bodyMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SettingsRepository.REMINDER_DAY_OPTIONS.forEach { days ->
                FilterChip(
                    selected = daysBefore == days,
                    onClick = { onDaysChange(days) },
                    label = { Text(stringResource(R.string.settings_reminders_days_option, days)) }
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.settings_reminders_time_label), style = MaterialTheme.typography.bodyMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SettingsRepository.REMINDER_HOUR_OPTIONS.forEach { hour ->
                FilterChip(
                    selected = reminderHour == hour,
                    onClick = { onHourChange(hour) },
                    label = { Text(stringResource(R.string.settings_reminders_time_option, hour)) }
                )
            }
        }
        Text(stringResource(R.string.settings_reminders_widget_hint), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun RadioRow(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(label, modifier = Modifier.padding(start = 4.dp))
    }
}
