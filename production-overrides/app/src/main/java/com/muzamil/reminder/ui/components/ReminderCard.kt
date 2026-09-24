package com.muzamil.reminder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.muzamil.reminder.data.ReminderEntity
import com.muzamil.reminder.ui.*
import com.muzamil.reminder.util.DateTimeUtils

@Composable
fun ReminderCard(
    reminder: ReminderEntity,
    use24h: Boolean,
    onEdit: () -> Unit,
    onComplete: () -> Unit,
    onSnooze: (Int) -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit
) {
    var snoozeOpen by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    FigmaCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.NotificationsActive, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(21.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    reminder.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Schedule, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        DateTimeUtils.formatTime(reminder.triggerAt, use24h),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "  •  ${DateTimeUtils.formatDate(reminder.triggerAt)}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (reminder.description.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        reminder.description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (reminder.priority != "NORMAL" || reminder.type != "ONE_TIME") {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (reminder.priority != "NORMAL") {
                            AssistChip(
                                onClick = {},
                                enabled = false,
                                label = { Text(reminder.priority.lowercase().replaceFirstChar { it.uppercase() }) },
                                leadingIcon = { Icon(Icons.Rounded.PriorityHigh, null, modifier = Modifier.size(14.dp)) }
                            )
                        }
                        if (reminder.type != "ONE_TIME") {
                            AssistChip(
                                onClick = {},
                                enabled = false,
                                label = { Text(reminder.type.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }) },
                                leadingIcon = { Icon(Icons.Rounded.Repeat, null, modifier = Modifier.size(14.dp)) }
                            )
                        }
                    }
                }
            }
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Rounded.MoreVert, "More actions", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text("Complete") }, leadingIcon = { Icon(Icons.Rounded.CheckCircle, null) }, onClick = { expanded = false; onComplete() })
                    DropdownMenuItem(text = { Text("Snooze") }, leadingIcon = { Icon(Icons.Rounded.Snooze, null) }, onClick = { expanded = false; snoozeOpen = true })
                    DropdownMenuItem(text = { Text("Edit / Reschedule") }, leadingIcon = { Icon(Icons.Rounded.EditCalendar, null) }, onClick = { expanded = false; onEdit() })
                    DropdownMenuItem(text = { Text("Duplicate") }, leadingIcon = { Icon(Icons.Rounded.ContentCopy, null) }, onClick = { expanded = false; onDuplicate() })
                    DropdownMenuItem(text = { Text("Delete") }, leadingIcon = { Icon(Icons.Rounded.DeleteOutline, null) }, onClick = { expanded = false; confirmDelete = true })
                }
            }
        }
    }

    if (snoozeOpen) {
        SnoozeDialog(defaultMinutes = reminder.snoozeDurationMinutes, onDismiss = { snoozeOpen = false }) { minutes ->
            snoozeOpen = false
            onSnooze(minutes)
        }
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            icon = { Icon(Icons.Rounded.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete reminder?") },
            text = { Text("This reminder will be removed from your active list and scheduled alerts will be cancelled.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun SnoozeDialog(defaultMinutes: Int, onDismiss: () -> Unit, onSelect: (Int) -> Unit) {
    var custom by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        icon = { Icon(Icons.Rounded.Snooze, null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("Snooze reminder", color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(5, 10, 30, 60).distinct().forEach { minutes ->
                    FilledTonalButton(onClick = { onSelect(minutes) }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (minutes == 60) "1 hour" else "$minutes minutes")
                    }
                }
                if (defaultMinutes !in listOf(5, 10, 30, 60)) {
                    OutlinedButton(onClick = { onSelect(defaultMinutes) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Default: $defaultMinutes minutes")
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        custom,
                        { custom = it.filter(Char::isDigit).take(4) },
                        label = { Text("Custom minutes") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(enabled = (custom.toIntOrNull() ?: 0) > 0, onClick = { onSelect(custom.toInt()) }) {
                        Text("Snooze")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
