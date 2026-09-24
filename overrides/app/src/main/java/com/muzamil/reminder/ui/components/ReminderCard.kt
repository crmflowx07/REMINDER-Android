package com.muzamil.reminder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
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

    FigmaCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(38.dp).clip(CircleShape).background(FigmaPurpleSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.NotificationsActive, null, tint = FigmaPurple, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(reminder.title, color = FigmaInk, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Schedule, null, tint = FigmaSecondary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(DateTimeUtils.formatTime(reminder.triggerAt, use24h), color = FigmaSecondary, style = MaterialTheme.typography.bodySmall)
                    Text("  •  ${DateTimeUtils.formatDate(reminder.triggerAt)}", color = FigmaSecondary, style = MaterialTheme.typography.bodySmall)
                }
                if (reminder.description.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(reminder.description, color = FigmaSecondary, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                }
            }
            Box {
                IconButton(onClick = { expanded = true }) { Icon(Icons.Outlined.MoreVert, "More actions", tint = FigmaSecondary) }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text("Complete") }, leadingIcon = { Icon(Icons.Outlined.CheckCircle, null) }, onClick = { expanded = false; onComplete() })
                    DropdownMenuItem(text = { Text("Snooze") }, leadingIcon = { Icon(Icons.Outlined.Snooze, null) }, onClick = { expanded = false; snoozeOpen = true })
                    DropdownMenuItem(text = { Text("Edit / Reschedule") }, leadingIcon = { Icon(Icons.Outlined.Edit, null) }, onClick = { expanded = false; onEdit() })
                    DropdownMenuItem(text = { Text("Duplicate") }, leadingIcon = { Icon(Icons.Outlined.ContentCopy, null) }, onClick = { expanded = false; onDuplicate() })
                    DropdownMenuItem(text = { Text("Delete") }, leadingIcon = { Icon(Icons.Outlined.Delete, null) }, onClick = { expanded = false; onDelete() })
                }
            }
        }
    }
    if (snoozeOpen) SnoozeDialog(defaultMinutes = reminder.snoozeDurationMinutes, onDismiss = { snoozeOpen = false }) { minutes -> snoozeOpen = false; onSnooze(minutes) }
}

@Composable
private fun SnoozeDialog(defaultMinutes: Int, onDismiss: () -> Unit, onSelect: (Int) -> Unit) {
    var custom by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Snooze reminder", color = FigmaInk) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(5, 10, 30, 60).distinct().forEach { minutes ->
                    OutlinedButton(onClick = { onSelect(minutes) }, modifier = Modifier.fillMaxWidth()) { Text(if (minutes == 60) "1 hour" else "$minutes minutes") }
                }
                if (defaultMinutes !in listOf(5,10,30,60)) OutlinedButton(onClick = { onSelect(defaultMinutes) }, modifier = Modifier.fillMaxWidth()) { Text("Default: $defaultMinutes minutes") }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(custom, { custom = it.filter(Char::isDigit).take(4) }, label = { Text("Custom minutes") }, modifier = Modifier.weight(1f), singleLine = true)
                    Button(enabled = (custom.toIntOrNull() ?: 0) > 0, onClick = { onSelect(custom.toInt()) }) { Text("Snooze") }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
