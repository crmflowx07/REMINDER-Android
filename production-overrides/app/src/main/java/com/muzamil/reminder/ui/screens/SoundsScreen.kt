package com.muzamil.reminder.ui.screens

import android.content.Intent
import android.media.MediaPlayer
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.muzamil.reminder.data.CustomSoundEntity
import com.muzamil.reminder.scheduling.DeviceTone
import com.muzamil.reminder.scheduling.SoundCatalog
import com.muzamil.reminder.ui.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SoundsScreen(vm: MainViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val customs by vm.customSounds.collectAsState()
    val settings by vm.settings.collectAsState()
    val scope = rememberCoroutineScope()
    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    var deviceTones by remember { mutableStateOf<List<DeviceTone>>(emptyList()) }
    var deviceLoading by remember { mutableStateOf(true) }
    var showAllDeviceTones by remember { mutableStateOf(false) }

    DisposableEffect(Unit) { onDispose { player?.release() } }
    LaunchedEffect(Unit) {
        deviceLoading = true
        deviceTones = withContext(Dispatchers.IO) { SoundCatalog.deviceTones(context) }
        deviceLoading = false
    }

    fun play(mp: MediaPlayer?) {
        player?.release()
        player = mp
        runCatching { player?.start() }
    }

    fun displayName(uri: android.net.Uri): String {
        var name = "Custom sound"
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) name = c.getString(0) ?: name
        }
        return name
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            vm.addCustomSound(CustomSoundEntity(displayName = displayName(uri), uri = uri.toString()))
        }
    }

    FigmaPageScaffold(title = "Reminder Sounds", onBack = onBack) { padding ->
        LazyColumn(
            Modifier.padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                FigmaSectionHeader("Built-in", SoundCatalog.builtIns.size)
                Text("Original tones bundled with REMINDER and available offline.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(SoundCatalog.builtIns, key = { it.key }) { sound ->
                FigmaRowCard(sound.name, "Built-in reminder tone", Icons.Rounded.MusicNote, FigmaPurpleSoft) {
                    IconButton(onClick = { play(MediaPlayer.create(context, sound.rawRes)) }) {
                        Icon(Icons.Rounded.PlayArrow, "Preview ${sound.name}", tint = MaterialTheme.colorScheme.primary)
                    }
                    RadioButton(selected = settings.defaultSound == sound.key, onClick = { scope.launch { vm.settingsStore.setDefaultSound(sound.key) } })
                }
            }

            item {
                Spacer(Modifier.height(6.dp))
                FigmaCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                FigmaSectionHeader("Phone tones", if (deviceLoading) null else deviceTones.size)
                                Text(
                                    "Uses notification, alarm and ringtone sounds already installed on this phone. On an OPPO A5X, available OPPO system tones appear here automatically.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (!deviceLoading && deviceTones.isNotEmpty()) {
                                TextButton(onClick = { showAllDeviceTones = !showAllDeviceTones }) { Text(if (showAllDeviceTones) "Hide" else "Show") }
                            }
                        }
                        if (deviceLoading) {
                            Spacer(Modifier.height(12.dp))
                            LinearProgressIndicator(Modifier.fillMaxWidth())
                        }
                    }
                }
            }

            if (showAllDeviceTones) {
                if (deviceTones.isEmpty() && !deviceLoading) {
                    item { FigmaEmptyState("No phone tones found", "Your device did not expose system tones to the app.", Icons.Rounded.AudioFile) }
                } else {
                    items(deviceTones, key = { it.uri.toString() }) { tone ->
                        val key = SoundCatalog.keyForUri(tone.uri)
                        FigmaRowCard(tone.name, "Phone / system tone", Icons.Rounded.NotificationsActive, FigmaBlue) {
                            IconButton(onClick = { play(MediaPlayer.create(context, tone.uri)) }) {
                                Icon(Icons.Rounded.PlayArrow, "Preview ${tone.name}", tint = MaterialTheme.colorScheme.primary)
                            }
                            RadioButton(selected = settings.defaultSound == key, onClick = { scope.launch { vm.settingsStore.setDefaultSound(key) } })
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        FigmaSectionHeader("My Sounds", customs.size)
                        Text("Import your own licensed audio with Android's system picker.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    FilledTonalButton(onClick = { picker.launch(arrayOf("audio/*")) }) {
                        Icon(Icons.Rounded.Add, null)
                        Spacer(Modifier.width(4.dp))
                        Text("Add")
                    }
                }
            }
            if (customs.isEmpty()) {
                item { FigmaEmptyState("No custom sounds", "Add an audio file from your phone and use it for reminders.", Icons.Rounded.AudioFile) }
            }
            items(customs, key = { it.id }) { sound ->
                FigmaRowCard(sound.displayName, "My Sounds", Icons.Rounded.AudioFile, FigmaMint) {
                    IconButton(onClick = { play(MediaPlayer.create(context, android.net.Uri.parse(sound.uri))) }) {
                        Icon(Icons.Rounded.PlayArrow, "Preview ${sound.displayName}", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { vm.deleteCustomSound(sound) }) {
                        Icon(Icons.Rounded.DeleteOutline, "Remove ${sound.displayName}", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
