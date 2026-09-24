package com.muzamil.reminder.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.muzamil.reminder.data.RoutineEntity
import com.muzamil.reminder.data.RoutineItemEntity
import com.muzamil.reminder.ui.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun RoutinesScreen(vm: MainViewModel) {
    val routines by vm.routines.collectAsState()
    var addRoutine by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        FigmaBackground()
        Column(Modifier.fillMaxSize()) {
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    FigmaSectionHeader("Routines", routines.size)
                    Spacer(Modifier.height(4.dp))
                    Text("Morning, fitness, work, pets and any routine you create.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                FilledTonalButton(onClick = { addRoutine = true }) { Icon(Icons.Rounded.Add, null); Spacer(Modifier.width(4.dp)); Text("Routine") }
            }
            Spacer(Modifier.height(16.dp))
            if (routines.isEmpty()) {
                FigmaEmptyState("No routines yet", "Create a routine, then add timed items such as Wake up, Breakfast or Workout.", Icons.Rounded.Repeat)
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 110.dp)) {
                    items(routines, key = { it.id }) { routine -> RoutineCard(vm, routine) }
                }
            }
        }
    }
    if (addRoutine) RoutineNameDialog("New routine", "Routine name", onDismiss = { addRoutine = false }) { vm.addRoutine(it); addRoutine = false }
}

@Composable
private fun RoutineCard(vm: MainViewModel, routine: RoutineEntity) {
    val items by vm.repository.routineItems(routine.id).collectAsState(initial = emptyList())
    var expanded by remember { mutableStateOf(false) }
    var addItem by remember { mutableStateOf(false) }
    FigmaCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ColorIconTile(FigmaPurpleSoft, Modifier.size(38.dp)) { Icon(Icons.Rounded.Repeat, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(routine.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text("${items.size} items", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { expanded = !expanded }) { Icon(if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, "Expand", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                if (items.isEmpty()) Text("No items yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 10.dp))
                items.forEach { item ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.small) {
                            Text(formatMinutes(item.minutesFromMidnight), modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(item.title, modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                        IconButton(onClick = { vm.deleteRoutineItem(item) }) { Icon(Icons.Rounded.Delete, "Delete item", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { addItem = true }) { Text("+ Add item") }
                    TextButton(onClick = { vm.deleteRoutine(routine) }) { Text("Delete routine") }
                }
            }
        }
    }
    if (addItem) RoutineItemDialog(onDismiss = { addItem = false }) { title, mins ->
        vm.addRoutineItem(RoutineItemEntity(routineId = routine.id, title = title, minutesFromMidnight = mins, sortOrder = items.size)); addItem = false
    }
}

@Composable
private fun RoutineItemDialog(onDismiss: () -> Unit, onSave: (String, Int) -> Unit) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var mins by remember { mutableIntStateOf(8 * 60) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add routine item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedButton(onClick = { TimePickerDialog(context, { _, h, m -> mins = h * 60 + m }, mins / 60, mins % 60, false).show() }) {
                    Icon(Icons.Rounded.Schedule, null); Spacer(Modifier.width(6.dp)); Text(formatMinutes(mins))
                }
            }
        },
        confirmButton = { TextButton(enabled = title.isNotBlank(), onClick = { onSave(title.trim(), mins) }) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun formatMinutes(total: Int): String = LocalTime.of(total / 60, total % 60).format(DateTimeFormatter.ofPattern("hh:mm a"))


@Composable
private fun RoutineNameDialog(title: String, label: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { OutlinedTextField(text, { text = it }, label = { Text(label) }, singleLine = true) },
        confirmButton = { TextButton(enabled = text.isNotBlank(), onClick = { onSave(text.trim()) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
