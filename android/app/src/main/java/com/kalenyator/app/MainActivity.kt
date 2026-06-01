package com.kalenyator.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.kalenyator.app.data.sync.ImportMode
import com.kalenyator.app.ui.AppViewModelFactory
import com.kalenyator.app.ui.LocalViewModelFactory
import com.kalenyator.app.ui.navigation.KalenyatorNav
import com.kalenyator.app.ui.settings.SettingsViewModel
import com.kalenyator.app.ui.theme.KalenyatorTheme
import com.kalenyator.app.util.LocaleHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase, com.kalenyator.app.util.LanguageStore.current))
    }

    private val importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@registerForActivityResult
        lifecycleScope.launch {
            val json = contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
            if (json.isNullOrBlank()) {
                toast(getString(R.string.settings_sync_error))
                return@launch
            }
            showImportModeDialog(json)
        }
    }

    private val qrScanLauncher = registerForActivityResult(ScanContract()) { result ->
        result.contents?.let { showImportModeDialog(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIncomingIntent(intent)

        val container = (application as KalenyatorApplication).container
        val factory = AppViewModelFactory(container)

        setContent {
            val settingsVm: SettingsViewModel = viewModel(factory = factory)
            val themeMode by settingsVm.themeMode.collectAsState()
            val visualTheme by settingsVm.visualTheme.collectAsState()

            CompositionLocalProvider(LocalViewModelFactory provides factory) {
                KalenyatorTheme(themeMode = themeMode, visualTheme = visualTheme) {
                    KalenyatorNav(
                        onExport = {
                            lifecycleScope.launch {
                                container.syncRepository.exportToCache()
                                    .onSuccess { file ->
                                        container.settingsRepository.markExportCompleted()
                                        shareFile(file.absolutePath)
                                        toast(getString(R.string.settings_sync_success_export))
                                    }
                                    .onFailure { toast(getString(R.string.settings_sync_error)) }
                            }
                        },
                        onExportNew = {
                            lifecycleScope.launch {
                                val since = container.settingsRepository.lastExportTime.first()
                                container.syncRepository.exportNewToCache(since)
                                    .onSuccess { file ->
                                        container.settingsRepository.markExportCompleted()
                                        shareFile(file.absolutePath)
                                        toast(getString(R.string.settings_sync_success_export_new))
                                    }
                                    .onFailure { toast(getString(R.string.settings_sync_error)) }
                            }
                        },
                        onImport = { importLauncher.launch(arrayOf("application/json", "text/*", "*/*")) },
                        onScanQr = {
                            val options = ScanOptions().apply {
                                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                                setPrompt(getString(R.string.settings_sync_qr_scan_prompt))
                                setBeepEnabled(false)
                            }
                            qrScanLauncher.launch(options)
                        },
                        onCheckUpdate = { openUpdateCheck() }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if (intent.type?.contains("json") == true) {
                    intent.getStringExtra(Intent.EXTRA_TEXT)?.let { showImportModeDialog(it) }
                }
            }
            Intent.ACTION_VIEW -> {
                intent.data?.let { uri ->
                    lifecycleScope.launch {
                        val json = contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                        json?.let { showImportModeDialog(it) }
                    }
                }
            }
        }
    }

    private fun showImportModeDialog(json: String) {
        val options = arrayOf(
            getString(R.string.import_mode_merge),
            getString(R.string.import_mode_replace),
            getString(R.string.import_mode_skip)
        )
        AlertDialog.Builder(this)
            .setTitle(R.string.import_mode_title)
            .setItems(options) { _, which ->
                val mode = when (which) {
                    1 -> ImportMode.REPLACE
                    2 -> ImportMode.SKIP_DUPLICATES
                    else -> ImportMode.MERGE
                }
                performImport(json, mode)
            }
            .setNegativeButton(R.string.family_cancel, null)
            .show()
    }

    private fun performImport(json: String, mode: ImportMode) {
        lifecycleScope.launch {
            val app = application as KalenyatorApplication
            app.container.syncRepository.importFromJson(json, mode)
                .onSuccess {
                    app.container.reminderRepository.refreshWidget()
                    app.container.reminderScheduler.reschedule()
                    toast(getString(R.string.settings_sync_success_import, it))
                }
                .onFailure { toast(getString(R.string.settings_sync_error)) }
        }
    }

    private fun openUpdateCheck() {
        val url = getString(R.string.update_check_url)
        if (!isUpdateUrlConfigured(url)) {
            toast(getString(R.string.update_check_hint))
            return
        }
        startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)))
    }

    private fun isUpdateUrlConfigured(url: String): Boolean {
        if (url.isBlank()) return false
        if (url.contains("example.com", ignoreCase = true)) return false
        if (url.contains("YOUR_GITHUB_USER", ignoreCase = true)) return false
        return url.startsWith("https://github.com/") && url.endsWith("/kalenyator.apk")
    }

    private fun shareFile(path: String) {
        val file = java.io.File(path)
        val uri = androidx.core.content.FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.settings_sync_export)))
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
