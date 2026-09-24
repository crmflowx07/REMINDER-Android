package com.muzamil.reminder.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.muzamil.reminder.data.ReminderEntity
import com.muzamil.reminder.ui.*
import com.muzamil.reminder.util.DateTimeUtils
import java.time.*

@Composable
fun PlannerScreen(vm: MainViewModel, onEdit: (Long) -> Unit, onCalendar: () -> Unit) {
    val active by vm.active.collectAsState()
    val settings by vm.settings.collectAsState()
    var tab by remember { mutableIntStateOf(0) }
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now()
    val list = active.filter { r ->
        val d = Instant.ofEpochMilli(r.triggerAt).atZone(zone).toLocalDate()
        when (tab) {
            0 -> d == today
            1 -> d == today.plusDays(1)
            2 -> !d.isBefore(today) && d.isBefore(today.plusDays(7))
            else -> d.isAfter(today.plusDays(6))
        }
    }.sortedBy { it.triggerAt }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        FigmaBackground()
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(start = 22.dp, top = 22.dp, end = 18.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Today’s Tasks", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge)
                    Text("Stay focused on what matters", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
                FigmaActionIconButton(Icons.Rounded.CalendarMonth, "Open calendar", onCalendar, accent = true)
            }

            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Today", "Tomorrow", "This Week", "Upcoming").forEachIndexed { i, label ->
                    Surface(
                        modifier = Modifier.widthIn(min = 82.dp),
                        shape = RoundedCornerShape(11.dp),
                        color = if (tab == i) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        border = if (tab == i) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .45f)),
                        shadowElevation = if (tab == i) 0.dp else 1.dp,
                        onClick = { tab = i }
                    ) {
                        Text(
                            label,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            color = if (tab == i) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            fontWeight = if (tab == i) FontWeight.SemiBold else FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }

            Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                val title = listOf("Today", "Tomorrow", "This Week", "Upcoming")[tab]
                FigmaSectionHeader(title, list.size)
                Spacer(Modifier.weight(1f))
                Text(DateTimeUtils.formatDate(System.currentTimeMillis()), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }

            if (list.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(22.dp), contentAlignment = Alignment.TopCenter) {
                    FigmaEmptyState("No tasks in this view", "Your schedule is clear.", Icons.Rounded.TaskAlt)
                }
            } else {
                androidx.compose.foundation.lazy.LazyColumn(
                    contentPadding = PaddingValues(start = 22.dp, top = 4.dp, end = 22.dp, bottom = 116.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(list.size) { i ->
                        val reminder = list[i]
                        TodayTaskCard(vm, reminder, settings.use24Hour) { onEdit(reminder.id) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayTaskCard(vm: MainViewModel, reminder: ReminderEntity, use24h: Boolean, onEdit: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    FigmaCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(28.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(17.dp))
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
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Schedule, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(DateTimeUtils.formatTime(reminder.triggerAt, use24h), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    if (reminder.type != "ONE_TIME") {
                        Text(
                            "  •  ${reminder.type.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Box {
                IconButton(onClick = { expanded = true }) { Icon(Icons.Rounded.MoreVert, "Actions", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text("Edit / Reschedule") }, leadingIcon = { Icon(Icons.Rounded.EditCalendar, null) }, onClick = { expanded = false; onEdit() })
                    DropdownMenuItem(text = { Text("Complete") }, leadingIcon = { Icon(Icons.Rounded.CheckCircle, null) }, onClick = { expanded = false; vm.completeReminder(reminder) })
                    DropdownMenuItem(text = { Text("Snooze ${reminder.snoozeDurationMinutes} min") }, leadingIcon = { Icon(Icons.Rounded.Snooze, null) }, onClick = { expanded = false; vm.snooze(reminder, reminder.snoozeDurationMinutes) })
                    DropdownMenuItem(text = { Text("Duplicate") }, leadingIcon = { Icon(Icons.Rounded.ContentCopy, null) }, onClick = { expanded = false; vm.duplicate(reminder) })
                    DropdownMenuItem(text = { Text("Delete") }, leadingIcon = { Icon(Icons.Rounded.DeleteOutline, null) }, onClick = { expanded = false; confirmDelete = true })
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete reminder?") },
            text = { Text("Scheduled notifications for this reminder will also be cancelled.") },
            confirmButton = { TextButton(onClick = { confirmDelete = false; vm.deleteReminder(reminder) }) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
}
