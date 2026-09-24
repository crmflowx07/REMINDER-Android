package com.muzamil.reminder.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.muzamil.reminder.ui.*
import com.muzamil.reminder.ui.components.ReminderCard
import com.muzamil.reminder.util.DateTimeUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(vm: MainViewModel, onBack: () -> Unit, onEdit: (Long) -> Unit) {
    var query by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("All") }
    var priority by remember { mutableStateOf("All") }
    var categoryId by remember { mutableStateOf<Long?>(null) }
    val raw by vm.search(query).collectAsState(initial = emptyList())
    val categories by vm.categories.collectAsState()
    val settings by vm.settings.collectAsState()
    val now = System.currentTimeMillis()
    val filtered = raw.filter { r ->
        val statusOk = when (status) {
            "Today" -> !r.isCompleted && r.triggerAt in DateTimeUtils.startOfToday()..DateTimeUtils.endOfToday()
            "Upcoming" -> !r.isCompleted && r.triggerAt > DateTimeUtils.endOfToday()
            "Overdue" -> !r.isCompleted && r.triggerAt < now
            "Completed" -> r.isCompleted
            else -> true
        }
        val priorityOk = priority == "All" || r.priority == priority.uppercase()
        val categoryOk = categoryId == null || r.categoryId == categoryId
        statusOk && priorityOk && categoryOk
    }

    FigmaPageScaffold(title = "Search & Filter", onBack = onBack) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(horizontal = 20.dp)) {
            FigmaCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    OutlinedTextField(
                        query,
                        { query = it },
                        label = { Text("Search title, notes or category") },
                        leadingIcon = { Icon(Icons.Rounded.Search, null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("All", "Today", "Upcoming", "Overdue", "Completed").forEach { value ->
                            FilterChip(selected = status == value, onClick = { status = value }, label = { Text(value) })
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterMenu("Priority: $priority", listOf("All", "Normal", "Important", "Urgent")) { priority = it }
                        val categoryName = categories.firstOrNull { it.id == categoryId }?.name ?: "All categories"
                        FilterMenu(categoryName, listOf("All categories") + categories.map { it.name }) { selected -> categoryId = categories.firstOrNull { it.name == selected }?.id }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            FigmaSectionHeader("Results", filtered.size)
            Spacer(Modifier.height(10.dp))
            if (filtered.isEmpty()) {
                FigmaEmptyState("No results", "Try a different keyword or filter.", Icons.Rounded.SearchOff)
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 32.dp)) {
                    items(filtered, key = { it.id }) { r ->
                        if (r.isCompleted) {
                            FigmaRowCard(
                                title = r.title,
                                subtitle = "Completed • ${DateTimeUtils.formatDateTime(r.occurrenceCompletedAt ?: r.triggerAt, settings.use24Hour)}",
                                icon = Icons.Rounded.CheckCircle,
                                tint = FigmaMint
                            ) {
                                IconButton(onClick = { vm.deleteReminder(r) }) { Icon(Icons.Rounded.Delete, "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                            }
                        } else {
                            ReminderCard(r, settings.use24Hour, { onEdit(r.id) }, { vm.completeReminder(r) }, { minutes -> vm.snooze(r, minutes) }, { vm.deleteReminder(r) }, { vm.duplicate(r) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterMenu(label: String, values: List<String>, onSelect: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { open = true }) { Text(label, maxLines = 1) }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            values.forEach { value -> DropdownMenuItem(text = { Text(value) }, onClick = { onSelect(value); open = false }) }
        }
    }
}
