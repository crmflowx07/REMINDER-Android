package com.muzamil.reminder.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.muzamil.reminder.scheduling.SoundCatalog
import com.muzamil.reminder.BuildConfig
import com.muzamil.reminder.ui.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(vm: MainViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val s by vm.settings.collectAsState()
    var info by remember { mutableStateOf<Pair<String, String>?>(null) }
    var importResult by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(vm.exportJson()) }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            if (text != null) vm.importJson(text) { importResult = "$it reminders imported." }
        }
    }

    FigmaPageScaffold(title = "Settings", onBack = onBack) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SettingsSection("Appearance")
            FigmaCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Theme", style = MaterialTheme.typography.labelLarge)
                    Text("Light, dark or follow your phone.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("SYSTEM" to "System", "LIGHT" to "Light", "DARK" to "Dark").forEach { (value, label) ->
                            FilterChip(
                                selected = s.theme == value,
                                onClick = { scope.launch { vm.settingsStore.setTheme(value) } },
                                label = { Text(label) },
                                leadingIcon = {
                                    Icon(
                                        when (value) { "LIGHT" -> Icons.Rounded.LightMode; "DARK" -> Icons.Rounded.DarkMode; else -> Icons.Rounded.SettingsBrightness },
                                        null,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }
            SwitchRow(Icons.Rounded.Schedule, FigmaBlue, "24-hour time", "Use 24-hour time throughout the app", s.use24Hour) { scope.launch { vm.settingsStore.setUse24Hour(it) } }
            SwitchRow(Icons.Rounded.CalendarMonth, FigmaPink, "Week starts Monday", "Planner and calendar preference", s.weekStartsMonday) { scope.launch { vm.settingsStore.setWeekStartsMonday(it) } }

            SettingsSection("Reminder defaults")
            DefaultSoundRow(
                soundName = SoundCatalog.displayName(context, s.defaultSound, null),
                selectedKey = s.defaultSound,
                onChange = { scope.launch { vm.settingsStore.setDefaultSound(it) } }
            )
            NumberChoiceRow(Icons.Rounded.Snooze, FigmaWarm, "Default snooze", s.defaultSnoozeMinutes, listOf(5, 10, 15, 30, 60), " min") { scope.launch { vm.settingsStore.setSnooze(it) } }
            SwitchRow(Icons.Rounded.Vibration, FigmaMint, "Vibration", "Use vibration by default", s.vibration) { scope.launch { vm.settingsStore.setVibration(it) } }
            NumberChoiceRow(Icons.Rounded.NotificationsActive, FigmaPurpleSoft, "Reminder before", s.defaultReminderBeforeMinutes.toInt(), listOf(0, 5, 10, 15, 30, 60, 1440), " min") { scope.launch { vm.settingsStore.setDefaultBefore(it.toLong()) } }

            SettingsSection("Reliability")
            ActionRow(Icons.Rounded.Notifications, FigmaPurpleSoft, "Notification settings", "Allow REMINDER to send notifications reliably") {
                context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName))
            }
            ActionRow(Icons.Rounded.Alarm, FigmaBlue, "Exact alarm access", "Android may require special access for precise reminder times") {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) runCatching {
                    context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}")))
                }
            }
            ActionRow(Icons.Rounded.BatterySaver, FigmaWarm, "Battery optimization", "Review Android battery restrictions if alarms are delayed") {
                context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            }

            SettingsSection("Data & backup")
            ActionRow(Icons.Rounded.AutoAwesome, FigmaPurpleSoft, "Load demo data", "Add sample tasks, routines, goals and travel data once") {
                vm.loadDemoData { count -> importResult = if (count == 0) "Demo data already appears to be loaded." else "$count demo reminders added." }
            }
            ActionRow(Icons.Rounded.FileUpload, FigmaMint, "Export reminders", "Save a local JSON backup; no account required") { exportLauncher.launch("REMINDER-backup.json") }
            ActionRow(Icons.Rounded.FileDownload, FigmaBlue, "Import reminders", "Import a REMINDER JSON backup") { importLauncher.launch(arrayOf("application/json", "text/json", "text/plain")) }
            importResult?.let {
                FigmaCard(Modifier.fillMaxWidth()) { Text(it, modifier = Modifier.padding(14.dp), color = MaterialTheme.colorScheme.primary) }
            }

            SettingsSection("Privacy & about")
            ActionRow(Icons.Rounded.PrivacyTip, FigmaMint, "Privacy", "Core reminder data stays on this device") {
                info = "Privacy" to "REMINDER is offline-first. Reminder contents are stored locally and are not uploaded by this build. Custom audio and phone tones are selected through Android system APIs."
            }
            ActionRow(Icons.Rounded.Gavel, FigmaWarm, "Terms", "Local-use terms for the current release") {
                info = "Terms" to "Use REMINDER to manage personal reminders and tasks. You remain responsible for critical deadlines and for keeping device notification and battery settings configured correctly."
            }
            ActionRow(Icons.Rounded.Info, FigmaPurpleSoft, "About REMINDER", "Version ${BuildConfig.VERSION_NAME}") {
                info = "REMINDER" to "Remember your life. Plan your time. Never forget what matters. Offline-first Android reminder and to-do app."
            }
            Spacer(Modifier.height(40.dp))
        }
    }

    info?.let { (title, text) ->
        AlertDialog(
            onDismissRequest = { info = null },
            title = { Text(title) },
            text = { Text(text) },
            confirmButton = { TextButton(onClick = { info = null }) { Text("OK") } }
        )
    }
}

@Composable
private fun SettingsSection(text: String) {
    Spacer(Modifier.height(4.dp))
    FigmaSectionHeader(text)
}

@Composable
private fun SwitchRow(icon: ImageVector, tint: Color, title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    FigmaRowCard(title, subtitle, icon, tint) { Switch(checked, onChange) }
}

@Composable
private fun ActionRow(icon: ImageVector, tint: Color, title: String, subtitle: String, onClick: () -> Unit) {
    FigmaRowCard(title, subtitle, icon, tint, onClick) { Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
}

@Composable
private fun DefaultSoundRow(soundName: String, selectedKey: String, onChange: (String) -> Unit) {
    var menu by remember { mutableStateOf(false) }
    FigmaRowCard("Default reminder sound", soundName, Icons.Rounded.MusicNote, FigmaPink) {
        Box {
            TextButton(onClick = { menu = true }) { Text("Change") }
            DropdownMenu(menu, { menu = false }) {
                SoundCatalog.builtIns.forEach { sound ->
                    DropdownMenuItem(
                        text = { Text(sound.name) },
                        leadingIcon = { if (selectedKey == sound.key) Icon(Icons.Rounded.Check, null) else Icon(Icons.Rounded.MusicNote, null) },
                        onClick = { onChange(sound.key); menu = false }
                    )
                }
                if (selectedKey.startsWith("uri:")) {
                    DropdownMenuItem(
                        text = { Text("Keep current phone tone") },
                        leadingIcon = { Icon(Icons.Rounded.PhoneAndroid, null) },
                        onClick = { menu = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun NumberChoiceRow(icon: ImageVector, tint: Color, title: String, value: Int, values: List<Int>, suffix: String, onChange: (Int) -> Unit) {
    var menu by remember { mutableStateOf(false) }
    val valueLabel = if (title == "Reminder before" && value == 0) "At reminder time" else "$value$suffix"
    FigmaRowCard(title, valueLabel, icon, tint) {
        Box {
            TextButton(onClick = { menu = true }) { Text("Change") }
            DropdownMenu(menu, { menu = false }) {
                values.forEach { v ->
                    val label = if (title == "Reminder before" && v == 0) "At reminder time" else "$v$suffix"
                    DropdownMenuItem(text = { Text(label) }, onClick = { onChange(v); menu = false })
                }
            }
        }
    }
}
