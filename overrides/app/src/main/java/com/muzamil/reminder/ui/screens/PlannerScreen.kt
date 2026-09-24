package com.muzamil.reminder.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
                Modifier.fillMaxWidth().padding(start = 22.dp, top = 22.dp, end = 14.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Today’s Tasks", color = FigmaInk, style = MaterialTheme.typography.titleLarge)
                    Text("Stay focused on what matters", color = FigmaSecondary, style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = onCalendar) {
                    Icon(Icons.Outlined.CalendarMonth, "Calendar", tint = FigmaInk)
                }
            }

            Row(
                Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Today", "Tomorrow", "Week", "Upcoming").forEachIndexed { i, label ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = if (tab == i) FigmaPurple else Color.White,
                        shadowElevation = if (tab == i) 0.dp else 1.dp,
                        onClick = { tab = i }
                    ) {
                        Text(
                            label,
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = if (tab == i) Color.White else FigmaSecondary,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontWeight = if (tab == i) FontWeight.SemiBold else FontWeight.Medium
                        )
                    }
                }
            }

            Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                FigmaSectionHeader(if (tab == 0) "Today" else listOf("Today", "Tomorrow", "This Week", "Upcoming")[tab], list.size)
                Spacer(Modifier.weight(1f))
                Text(DateTimeUtils.formatDate(System.currentTimeMillis()), color = FigmaSecondary, style = MaterialTheme.typography.bodySmall)
            }

            if (list.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(22.dp), contentAlignment = Alignment.TopCenter) {
                    FigmaCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            ColorIconTile(FigmaPurpleSoft, Modifier.size(54.dp)) {
                                Icon(Icons.Outlined.TaskAlt, null, tint = FigmaPurple, modifier = Modifier.size(28.dp))
                            }
                            Spacer(Modifier.height(14.dp))
                            Text("No tasks in this view", color = FigmaInk, fontWeight = FontWeight.SemiBold)
                            Text("Your schedule is clear.", color = FigmaSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 22.dp, top = 4.dp, end = 22.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(list, key = { it.id }) { reminder ->
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
    FigmaCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(24.dp).clip(CircleShape).background(FigmaPurpleSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Check, null, tint = FigmaPurple, modifier = Modifier.size(15.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(reminder.title, color = FigmaInk, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Schedule, null, tint = FigmaSecondary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(DateTimeUtils.formatTime(reminder.triggerAt, use24h), color = FigmaSecondary, style = MaterialTheme.typography.bodySmall)
                    if (reminder.type != "ONE_TIME") {
                        Text("  •  ${reminder.type.lowercase().replace('_',' ').replaceFirstChar { it.uppercase() }}", color = FigmaSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Box {
                IconButton(onClick = { expanded = true }) { Icon(Icons.Outlined.MoreVert, "Actions", tint = FigmaSecondary) }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text("Edit") }, leadingIcon = { Icon(Icons.Outlined.Edit, null) }, onClick = { expanded = false; onEdit() })
                    DropdownMenuItem(text = { Text("Complete") }, leadingIcon = { Icon(Icons.Outlined.CheckCircle, null) }, onClick = { expanded = false; vm.completeReminder(reminder) })
                    DropdownMenuItem(text = { Text("Snooze 10 min") }, leadingIcon = { Icon(Icons.Outlined.Snooze, null) }, onClick = { expanded = false; vm.snooze(reminder, 10) })
                }
            }
        }
    }
}
