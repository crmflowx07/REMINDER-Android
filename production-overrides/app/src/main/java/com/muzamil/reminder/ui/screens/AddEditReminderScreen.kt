package com.muzamil.reminder.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.muzamil.reminder.data.ReminderEntity
import com.muzamil.reminder.scheduling.SoundCatalog
import com.muzamil.reminder.ui.*
import com.muzamil.reminder.util.DateTimeUtils
import com.muzamil.reminder.util.NaturalLanguageParser
import java.time.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditReminderScreen(vm: MainViewModel, reminderId: Long, onDone: () -> Unit) {
    val context = LocalContext.current
    val categories by vm.categories.collectAsState()
    val customSounds by vm.customSounds.collectAsState()
    val profiles by vm.profiles.collectAsState()
    val settings by vm.settings.collectAsState()
    val zone = ZoneId.systemDefault()

    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var triggerAt by remember { mutableLongStateOf(System.currentTimeMillis() + 60 * 60 * 1000L) }
    var repeat by remember { mutableStateOf("ONE_TIME") }
    var interval by remember { mutableIntStateOf(1) }
    var repeatUnit by remember { mutableStateOf("DAYS") }
    var daysCsv by remember { mutableStateOf("") }
    var recurrenceEndAt by remember { mutableStateOf<Long?>(null) }
    var categoryId by remember { mutableStateOf<Long?>(null) }
    var ownerProfileId by remember { mutableStateOf<Long?>(null) }
    var priority by remember { mutableStateOf("NORMAL") }
    var soundKey by remember { mutableStateOf(settings.defaultSound) }
    var customSoundUri by remember { mutableStateOf<String?>(null) }
    var soundTouched by remember { mutableStateOf(false) }
    var vibration by remember { mutableStateOf(settings.vibration) }
    var snoozeMinutes by remember { mutableIntStateOf(settings.defaultSnoozeMinutes) }
    var location by remember { mutableStateOf("") }
    var meetingLink by remember { mutableStateOf("") }
    var personCompany by remember { mutableStateOf("") }
    var meetingEndAt by remember { mutableStateOf<Long?>(null) }
    var advanced by remember { mutableStateOf(false) }
    var quickText by remember { mutableStateOf("") }
    var parsedMessage by remember { mutableStateOf<String?>(null) }
    var alertOffsets by remember {
        mutableStateOf(if (settings.defaultReminderBeforeMinutes > 0) setOf(settings.defaultReminderBeforeMinutes) else emptySet())
    }
    var loaded by remember { mutableStateOf(reminderId == 0L) }

    val phoneTonePicker = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        val uri: Uri? = if (Build.VERSION.SDK_INT >= 33) {
            data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
        }
        if (uri != null) {
            customSoundUri = uri.toString()
            soundKey = "system"
            soundTouched = true
        }
    }

    LaunchedEffect(reminderId) {
        if (reminderId != 0L) {
            vm.reminder(reminderId)?.let { r ->
                title = r.title
                notes = r.description
                triggerAt = r.triggerAt
                repeat = r.type
                interval = r.repeatInterval
                repeatUnit = r.repeatUnit
                daysCsv = r.daysOfWeekCsv
                recurrenceEndAt = r.recurrenceEndAt
                categoryId = r.categoryId
                priority = r.priority
                ownerProfileId = r.ownerProfileId
                soundKey = r.soundKey
                customSoundUri = r.customSoundUri
                soundTouched = true
                vibration = r.vibrationEnabled
                snoozeMinutes = r.snoozeDurationMinutes
                location = r.location.orEmpty()
                meetingLink = r.meetingLink.orEmpty()
                personCompany = r.personCompany.orEmpty()
                meetingEndAt = r.meetingEndAt
                alertOffsets = vm.alertOffsets(r.id).filter { it > 0 }.toSet()
                loaded = true
            }
        }
    }

    if (!loaded) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val selectedCategory = categories.firstOrNull { it.id == categoryId }
    val isMeeting = selectedCategory?.name == "Meeting"
    val invalidPast = triggerAt <= System.currentTimeMillis()

    FigmaPageScaffold(
        title = if (reminderId == 0L) "Add Reminder" else "Edit Reminder",
        onBack = onDone,
        actions = {
            TextButton(
                enabled = title.isNotBlank() && !invalidPast,
                onClick = {
                    val reminder = ReminderEntity(
                        id = reminderId,
                        title = title.trim(),
                        description = notes.trim(),
                        triggerAt = triggerAt,
                        timezone = zone.id,
                        type = repeat,
                        repeatInterval = interval.coerceAtLeast(1),
                        repeatUnit = repeatUnit,
                        daysOfWeekCsv = daysCsv,
                        recurrenceEndAt = recurrenceEndAt,
                        categoryId = categoryId,
                        priority = priority,
                        ownerProfileId = ownerProfileId,
                        soundKey = soundKey,
                        customSoundUri = customSoundUri,
                        vibrationEnabled = vibration,
                        snoozeDurationMinutes = snoozeMinutes,
                        location = location.ifBlank { null },
                        meetingLink = meetingLink.ifBlank { null },
                        personCompany = personCompany.ifBlank { null },
                        meetingEndAt = meetingEndAt
                    )
                    vm.saveReminder(reminder, alertOffsets.toList()) { onDone() }
                }
            ) {
                Icon(Icons.Rounded.Check, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Save", fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            FigmaFormSection("Reminder", Icons.Rounded.TaskAlt) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it.take(160) },
                    label = { Text("Title") },
                    placeholder = { Text("Pay electricity bill") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Rounded.EditNote, null) }
                )

                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            val d = Instant.ofEpochMilli(triggerAt).atZone(zone)
                            DatePickerDialog(context, { _, y, m, day ->
                                triggerAt = LocalDate.of(y, m + 1, day)
                                    .atTime(Instant.ofEpochMilli(triggerAt).atZone(zone).toLocalTime())
                                    .atZone(zone).toInstant().toEpochMilli()
                            }, d.year, d.monthValue - 1, d.dayOfMonth).show()
                        }
                    ) {
                        Icon(Icons.Rounded.CalendarMonth, null)
                        Spacer(Modifier.width(6.dp))
                        Text(DateTimeUtils.formatDate(triggerAt), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    OutlinedButton(
                        onClick = {
                            val d = Instant.ofEpochMilli(triggerAt).atZone(zone)
                            TimePickerDialog(context, { _, h, min ->
                                triggerAt = Instant.ofEpochMilli(triggerAt).atZone(zone).toLocalDate()
                                    .atTime(h, min).atZone(zone).toInstant().toEpochMilli()
                            }, d.hour, d.minute, settings.use24Hour).show()
                        }
                    ) {
                        Icon(Icons.Rounded.Schedule, null)
                        Spacer(Modifier.width(6.dp))
                        Text(DateTimeUtils.formatTime(triggerAt, settings.use24Hour), maxLines = 1)
                    }
                }
                if (invalidPast) {
                    Text("Choose a future date and time.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }

            FigmaFormSection("Smart quick entry", Icons.Rounded.AutoAwesome) {
                Text(
                    "Type naturally. REMINDER interprets common dates, times and repeats offline, then lets you review everything before saving.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = quickText,
                    onValueChange = { quickText = it.take(300) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Remind me tomorrow at 8 AM to feed my birds") },
                    maxLines = 3
                )
                FilledTonalButton(
                    onClick = {
                        val parsed = NaturalLanguageParser.parse(quickText)
                        title = parsed.title
                        parsed.triggerAt?.let { triggerAt = it }
                        repeat = parsed.type.name
                        daysCsv = parsed.daysOfWeekCsv
                        if (parsed.alertOffsetsMinutes.isNotEmpty()) alertOffsets = parsed.alertOffsetsMinutes.toSet()
                        parsed.suggestedCategoryName?.let { suggested ->
                            categories.firstOrNull { it.name == suggested }?.let { c ->
                                categoryId = c.id
                                if (!soundTouched) {
                                    soundKey = defaultSoundForCategory(c.name)
                                    customSoundUri = null
                                }
                            }
                        }
                        parsedMessage = "${parsed.explanation} • ${"%.0f".format(parsed.confidence * 100)}% confidence. Review before saving."
                    },
                    enabled = quickText.isNotBlank()
                ) {
                    Icon(Icons.Rounded.AutoAwesome, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Interpret")
                }
                parsedMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }

            FigmaRowCard(
                title = if (advanced) "Hide advanced options" else "More reminder options",
                subtitle = if (advanced) "Repeat, alerts, category, profile, sound and priority are visible." else "Repeat, multiple alerts, category, profile, sound, meeting details and priority.",
                icon = if (advanced) Icons.Rounded.ExpandLess else Icons.Rounded.Tune,
                tint = MaterialTheme.colorScheme.primarySoft,
                onClick = { advanced = !advanced }
            ) {
                Icon(if (advanced) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (advanced) {
                FigmaFormSection("Schedule", Icons.Rounded.Repeat) {
                    SectionTitle("Repeat")
                    SingleChoiceChips(
                        options = listOf(
                            "ONE_TIME" to "Doesn't repeat",
                            "DAILY" to "Daily",
                            "WEEKDAYS" to "Weekdays",
                            "WEEKENDS" to "Weekends",
                            "WEEKLY" to "Weekly",
                            "MONTHLY" to "Monthly",
                            "YEARLY" to "Yearly",
                            "SPECIFIC_DAYS" to "Specific days",
                            "CUSTOM" to "Custom"
                        ),
                        selected = repeat,
                        onSelect = { repeat = it }
                    )

                    if (repeat == "CUSTOM") {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = interval.toString(),
                                onValueChange = { interval = it.toIntOrNull()?.coerceIn(1, 10000) ?: 1 },
                                label = { Text("Every") },
                                modifier = Modifier.widthIn(min = 110.dp, max = 150.dp),
                                singleLine = true
                            )
                            var unitMenu by remember { mutableStateOf(false) }
                            Box {
                                OutlinedButton(onClick = { unitMenu = true }) { Text(repeatUnit.lowercase().replaceFirstChar { it.uppercase() }) }
                                DropdownMenu(expanded = unitMenu, onDismissRequest = { unitMenu = false }) {
                                    listOf("MINUTES", "HOURS", "DAYS", "WEEKS", "MONTHS").forEach { u ->
                                        DropdownMenuItem(text = { Text(u.lowercase().replaceFirstChar { it.uppercase() }) }, onClick = { repeatUnit = u; unitMenu = false })
                                    }
                                }
                            }
                        }
                    }

                    if (repeat == "SPECIFIC_DAYS") {
                        val selectedDays = daysCsv.split(',').mapNotNull { it.toIntOrNull() }.toSet()
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(1 to "Mon", 2 to "Tue", 3 to "Wed", 4 to "Thu", 5 to "Fri", 6 to "Sat", 7 to "Sun").forEach { (value, label) ->
                                FilterChip(selected = value in selectedDays, onClick = {
                                    val updated = if (value in selectedDays) selectedDays - value else selectedDays + value
                                    daysCsv = updated.sorted().joinToString(",")
                                }, label = { Text(label) })
                            }
                        }
                    }

                    if (repeat != "ONE_TIME") {
                        OutlinedButton(onClick = {
                            val start = recurrenceEndAt?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() } ?: LocalDate.now().plusMonths(3)
                            DatePickerDialog(context, { _, y, m, day ->
                                recurrenceEndAt = LocalDate.of(y, m + 1, day).atTime(23, 59).atZone(zone).toInstant().toEpochMilli()
                            }, start.year, start.monthValue - 1, start.dayOfMonth).show()
                        }) {
                            Icon(Icons.Rounded.EventBusy, null)
                            Spacer(Modifier.width(6.dp))
                            Text(recurrenceEndAt?.let { "Repeat ends ${DateTimeUtils.formatDate(it)}" } ?: "Optional repeat end date")
                        }
                        if (recurrenceEndAt != null) {
                            TextButton(onClick = { recurrenceEndAt = null }) { Text("No end date") }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .35f))
                    SectionTitle("Remind Before")
                    val presets = listOf(
                        259200L to "6 months", 129600L to "3 months", 43200L to "1 month", 20160L to "2 weeks", 10080L to "1 week",
                        4320L to "3 days", 1440L to "1 day", 720L to "12 hours", 360L to "6 hours", 180L to "3 hours",
                        60L to "1 hour", 30L to "30 min", 15L to "15 min", 10L to "10 min", 5L to "5 min"
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        presets.forEach { (minutes, label) ->
                            FilterChip(
                                selected = minutes in alertOffsets,
                                onClick = { alertOffsets = if (minutes in alertOffsets) alertOffsets - minutes else alertOffsets + minutes },
                                label = { Text(label) }
                            )
                        }
                    }
                    CustomBeforeField(onAdd = { alertOffsets = alertOffsets + it })
                    if (alertOffsets.isNotEmpty()) {
                        Text("Selected: ${alertOffsets.sortedDescending().joinToString { beforeLabel(it) }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                FigmaFormSection("Organization", Icons.Rounded.FolderOpen) {
                    SectionTitle("Category")
                    var categoryMenu by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = categoryMenu, onExpandedChange = { categoryMenu = it }) {
                        OutlinedTextField(
                            value = selectedCategory?.name ?: "No category",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryMenu) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = categoryMenu, onDismissRequest = { categoryMenu = false }) {
                            DropdownMenuItem(text = { Text("No category") }, onClick = { categoryId = null; categoryMenu = false })
                            categories.forEach { c ->
                                DropdownMenuItem(text = { Text(c.name) }, onClick = {
                                    categoryId = c.id
                                    if (!soundTouched) {
                                        soundKey = defaultSoundForCategory(c.name)
                                        customSoundUri = null
                                    }
                                    categoryMenu = false
                                })
                            }
                        }
                    }

                    SectionTitle("Profile")
                    var profileMenu by remember { mutableStateOf(false) }
                    val profileName = profiles.firstOrNull { it.id == ownerProfileId }?.name ?: "No profile"
                    Box {
                        OutlinedButton(onClick = { profileMenu = true }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Rounded.Person, null)
                            Spacer(Modifier.width(6.dp))
                            Text(profileName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        DropdownMenu(expanded = profileMenu, onDismissRequest = { profileMenu = false }) {
                            DropdownMenuItem(text = { Text("No profile") }, onClick = { ownerProfileId = null; profileMenu = false })
                            profiles.forEach { p ->
                                DropdownMenuItem(text = { Text("${p.name} • ${p.type.lowercase()}") }, onClick = { ownerProfileId = p.id; profileMenu = false })
                            }
                        }
                    }

                    SectionTitle("Priority")
                    SingleChoiceChips(listOf("NORMAL" to "Normal", "IMPORTANT" to "Important", "URGENT" to "Urgent"), priority) { priority = it }
                }

                if (isMeeting) {
                    FigmaFormSection("Meeting details", Icons.Rounded.Groups) {
                        OutlinedTextField(location, { location = it.take(180) }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Rounded.Place, null) })
                        OutlinedTextField(meetingLink, { meetingLink = it.take(500) }, label = { Text("Meeting link") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Rounded.Link, null) })
                        OutlinedTextField(personCompany, { personCompany = it.take(180) }, label = { Text("Person / company") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Rounded.Business, null) })
                        OutlinedButton(onClick = {
                            val base = meetingEndAt ?: (triggerAt + 60 * 60 * 1000L)
                            val d = Instant.ofEpochMilli(base).atZone(zone)
                            TimePickerDialog(context, { _, h, min ->
                                val startDate = Instant.ofEpochMilli(triggerAt).atZone(zone).toLocalDate()
                                var candidate = startDate.atTime(h, min).atZone(zone).toInstant().toEpochMilli()
                                if (candidate <= triggerAt) candidate += 24 * 60 * 60 * 1000L
                                meetingEndAt = candidate
                            }, d.hour, d.minute, settings.use24Hour).show()
                        }) {
                            Icon(Icons.Rounded.Schedule, null)
                            Spacer(Modifier.width(6.dp))
                            Text(meetingEndAt?.let { "Ends ${DateTimeUtils.formatDateTime(it, settings.use24Hour)}" } ?: "Add optional end time")
                        }
                    }
                }

                FigmaFormSection("Alert", Icons.Rounded.NotificationsActive) {
                    SectionTitle("Sound")
                    var soundMenu by remember { mutableStateOf(false) }
                    val soundLabel = customSoundUri?.let { uri ->
                        customSounds.firstOrNull { it.uri == uri }?.displayName ?: SoundCatalog.displayName(context, soundKey, uri)
                    } ?: SoundCatalog.displayName(context, soundKey, null)
                    ExposedDropdownMenuBox(expanded = soundMenu, onExpandedChange = { soundMenu = it }) {
                        OutlinedTextField(
                            soundLabel,
                            {},
                            readOnly = true,
                            label = { Text("Reminder sound") },
                            leadingIcon = { Icon(Icons.Rounded.MusicNote, null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(soundMenu) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = soundMenu, onDismissRequest = { soundMenu = false }) {
                            SoundCatalog.builtIns.forEach { s ->
                                DropdownMenuItem(text = { Text(s.name) }, leadingIcon = { Icon(Icons.Rounded.MusicNote, null) }, onClick = {
                                    soundKey = s.key
                                    customSoundUri = null
                                    soundTouched = true
                                    soundMenu = false
                                })
                            }
                            customSounds.forEach { s ->
                                DropdownMenuItem(text = { Text("My Sounds • ${s.displayName}") }, leadingIcon = { Icon(Icons.Rounded.AudioFile, null) }, onClick = {
                                    soundKey = "custom"
                                    customSoundUri = s.uri
                                    soundTouched = true
                                    soundMenu = false
                                })
                            }
                            DropdownMenuItem(
                                text = { Text("Choose phone tone…") },
                                leadingIcon = { Icon(Icons.Rounded.PhoneAndroid, null) },
                                onClick = {
                                    soundMenu = false
                                    phoneTonePicker.launch(Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION or RingtoneManager.TYPE_ALARM or RingtoneManager.TYPE_RINGTONE)
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                    })
                                }
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Vibration", fontWeight = FontWeight.SemiBold)
                            Text("Vibrate with the reminder", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = vibration, onCheckedChange = { vibration = it })
                    }

                    OutlinedTextField(
                        value = snoozeMinutes.toString(),
                        onValueChange = { snoozeMinutes = it.toIntOrNull()?.coerceIn(1, 1440) ?: settings.defaultSnoozeMinutes },
                        label = { Text("Snooze duration (minutes)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Rounded.Snooze, null) }
                    )
                }
            }

            FigmaFormSection("Notes", Icons.Rounded.Notes) {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it.take(3000) },
                    label = { Text("Optional notes") },
                    placeholder = { Text("Bring presentation, account number, packing note…") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                    minLines = 4,
                    maxLines = 8
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) = Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SingleChoiceChips(options: List<Pair<String, String>>, selected: String, onSelect: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        options.forEach { (value, label) ->
            FilterChip(selected = selected == value, onClick = { onSelect(value) }, label = { Text(label) })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CustomBeforeField(onAdd: (Long) -> Unit) {
    var value by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("minutes") }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value,
            { value = it.filter(Char::isDigit).take(5) },
            label = { Text("Custom") },
            modifier = Modifier.widthIn(min = 120.dp, max = 170.dp),
            singleLine = true
        )
        var menu by remember { mutableStateOf(false) }
        Box {
            OutlinedButton(onClick = { menu = true }) { Text(unit) }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                listOf("minutes", "hours", "days").forEach { u ->
                    DropdownMenuItem(text = { Text(u) }, onClick = { unit = u; menu = false })
                }
            }
        }
        FilledTonalIconButton(onClick = {
            val n = value.toLongOrNull() ?: return@FilledTonalIconButton
            val mins = when (unit) {
                "hours" -> n * 60
                "days" -> n * 1440
                else -> n
            }
            if (mins > 0) {
                onAdd(mins)
                value = ""
            }
        }) {
            Icon(Icons.Rounded.Add, "Add custom advance alert")
        }
    }
}

private fun beforeLabel(minutes: Long): String = when {
    minutes % 43200L == 0L -> "${minutes / 43200}mo"
    minutes % 10080L == 0L -> "${minutes / 10080}w"
    minutes % 1440L == 0L -> "${minutes / 1440}d"
    minutes % 60L == 0L -> "${minutes / 60}h"
    else -> "${minutes}m"
}

private fun defaultSoundForCategory(category: String): String = when (category) {
    "Meeting", "Work" -> "digital_alert"
    "Birthday" -> "bright_chime"
    "Payment", "Documents" -> "important_alert"
    "Family" -> "gentle_chime"
    "Fitness" -> "morning_bell"
    "Travel" -> "elegant"
    "Vehicle" -> "clock_reminder"
    else -> "classic_bell"
}
