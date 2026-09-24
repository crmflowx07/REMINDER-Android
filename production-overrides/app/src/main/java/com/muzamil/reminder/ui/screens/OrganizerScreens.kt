package com.muzamil.reminder.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.ui.unit.dp
import com.muzamil.reminder.data.*
import com.muzamil.reminder.ui.*
import com.muzamil.reminder.util.DateTimeUtils
import java.time.*

@Composable
private fun FeatureScaffold(title: String, onBack: () -> Unit, onAdd: () -> Unit, content: @Composable (PaddingValues) -> Unit) {
    FigmaPageScaffold(
        title = title,
        onBack = onBack,
        actions = {
            IconButton(onClick = onAdd) {
                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium) {
                    Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Add, "Add", tint = MaterialTheme.colorScheme.primary) }
                }
            }
        },
        content = content
    )
}

@Composable
fun IdeasScreen(vm: MainViewModel, onBack: () -> Unit) {
    val list by vm.ideas.collectAsState(); var add by remember { mutableStateOf(false) }
    FeatureScaffold("Ideas", onBack, { add = true }) { padding ->
        EmptyOrList(padding, list.isEmpty(), "No ideas yet", "Capture ideas before they disappear.", Icons.Rounded.Lightbulb) {
            items(list, key = { it.id }) { x ->
                FigmaRowCard(
                    title = x.title,
                    subtitle = x.description.ifBlank { "Created ${DateTimeUtils.formatDate(x.createdAt)}" },
                    icon = Icons.Rounded.Lightbulb,
                    tint = FigmaWarm
                ) { IconButton(onClick = { vm.deleteIdea(x) }) { Icon(Icons.Rounded.Delete, "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant) } }
            }
        }
    }
    if (add) TwoTextDialog("New idea", "Idea title", "Description", { add = false }) { a, b -> vm.addIdea(IdeaEntity(title = a, description = b)); add = false }
}

@Composable
fun PlansScreen(vm: MainViewModel, onBack: () -> Unit) {
    val list by vm.plans.collectAsState(); var add by remember { mutableStateOf(false) }
    FeatureScaffold("Plans", onBack, { add = true }) { padding ->
        EmptyOrList(padding, list.isEmpty(), "No plans yet", "Create simple future plans with target dates.", Icons.Rounded.EventNote) {
            items(list, key = { it.id }) { x ->
                FigmaCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ColorIconTile(FigmaPurpleSoft, Modifier.size(38.dp)) { Icon(Icons.Rounded.EventNote, null, tint = MaterialTheme.colorScheme.primary) }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(x.goal, style = MaterialTheme.typography.labelLarge, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                Text("${x.progress}%${x.targetAt?.let { " • Target ${DateTimeUtils.formatDate(it)}" } ?: ""}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { vm.deletePlan(x) }) { Icon(Icons.Rounded.Delete, "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(progress = { x.progress / 100f }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                            FilledTonalIconButton(enabled = x.progress > 0, onClick = { vm.updatePlan(x.copy(progress = (x.progress - 10).coerceAtLeast(0))) }) {
                                Icon(Icons.Rounded.Remove, "Decrease progress")
                            }
                            Text("${x.progress}%", modifier = Modifier.padding(horizontal = 10.dp), style = MaterialTheme.typography.labelLarge)
                            FilledTonalIconButton(enabled = x.progress < 100, onClick = { vm.updatePlan(x.copy(progress = (x.progress + 10).coerceAtMost(100))) }) {
                                Icon(Icons.Rounded.Add, "Increase progress")
                            }
                        }
                    }
                }
            }
        }
    }
    if (add) DatedItemDialog("New plan", "Plan / goal", { add = false }) { text, date -> vm.addPlan(PlanEntity(goal = text, targetAt = date)); add = false }
}

@Composable
fun GoalsScreen(vm: MainViewModel, onBack: () -> Unit) {
    val list by vm.goals.collectAsState(); var add by remember { mutableStateOf(false) }
    FeatureScaffold("Goals", onBack, { add = true }) { padding ->
        EmptyOrList(padding, list.isEmpty(), "No goals yet", "Keep goals simple and visible.", Icons.Rounded.Flag) {
            items(list, key = { it.id }) { x ->
                FigmaCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ColorIconTile(FigmaMint, Modifier.size(38.dp)) { Icon(Icons.Rounded.Flag, null, tint = MaterialTheme.colorScheme.primary) }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(x.title, style = MaterialTheme.typography.labelLarge, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                Text("Progress ${x.progress}%${x.targetAt?.let { " • ${DateTimeUtils.formatDate(it)}" } ?: ""}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { vm.deleteGoal(x) }) { Icon(Icons.Rounded.DeleteOutline, "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(progress = { x.progress / 100f }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                            FilledTonalIconButton(enabled = x.progress > 0, onClick = { vm.updateGoal(x.copy(progress = (x.progress - 10).coerceAtLeast(0))) }) {
                                Icon(Icons.Rounded.Remove, "Decrease progress")
                            }
                            Text("${x.progress}%", modifier = Modifier.padding(horizontal = 10.dp), style = MaterialTheme.typography.labelLarge)
                            FilledTonalIconButton(enabled = x.progress < 100, onClick = { vm.updateGoal(x.copy(progress = (x.progress + 10).coerceAtMost(100))) }) {
                                Icon(Icons.Rounded.Add, "Increase progress")
                            }
                        }
                    }
                }
            }
        }
    }
    if (add) DatedItemDialog("New goal", "Goal", { add = false }) { text, date -> vm.addGoal(GoalEntity(title = text, targetAt = date)); add = false }
}

@Composable
fun TravelScreen(vm: MainViewModel, onBack: () -> Unit) {
    val list by vm.trips.collectAsState(); var add by remember { mutableStateOf(false) }
    FeatureScaffold("Travel", onBack, { add = true }) { padding ->
        EmptyOrList(padding, list.isEmpty(), "No trips yet", "Plan trips without turning REMINDER into a maps app.", Icons.Rounded.Flight) {
            items(list, key = { it.id }) { x ->
                var expanded by remember(x.id) { mutableStateOf(false) }
                val places by vm.repository.tripPlaces(x.id).collectAsState(initial = emptyList())
                FigmaCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ColorIconTile(FigmaBlue, Modifier.size(38.dp)) { Icon(Icons.Rounded.Flight, null, tint = MaterialTheme.colorScheme.primary) }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(x.name, style = MaterialTheme.typography.labelLarge, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                Text("${DateTimeUtils.formatDate(x.startAt)} — ${DateTimeUtils.formatDate(x.endAt)} • ${places.size} places", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { expanded = !expanded }) { Icon(if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, "Places", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                            IconButton(onClick = { vm.deleteTrip(x) }) { Icon(Icons.Rounded.Delete, "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                        if (expanded) TripPlaces(vm, x.id, places)
                    }
                }
            }
        }
    }
    if (add) TripDialog(onDismiss = { add = false }) { name, start, end -> vm.addTrip(TripEntity(name = name, startAt = start, endAt = end)); add = false }
}

@Composable
private fun TripPlaces(vm: MainViewModel, tripId: Long, places: List<TripPlaceEntity>) {
    var add by remember { mutableStateOf(false) }
    Column(Modifier.padding(start = 8.dp, top = 8.dp)) {
        places.forEach { p ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Place, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp)); Text(p.name, modifier = Modifier.weight(1f), maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                IconButton(onClick = { vm.deleteTripPlace(p) }) { Icon(Icons.Rounded.Close, "Remove", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
        TextButton(onClick = { add = true }) { Text("+ Add place") }
    }
    if (add) OrganizerTextInputDialog("Add place", "Place name", { add = false }) { name -> vm.addTripPlace(TripPlaceEntity(tripId = tripId, name = name)); add = false }
}

@Composable
private fun EmptyOrList(
    padding: PaddingValues,
    empty: Boolean,
    title: String,
    message: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit
) {
    if (empty) {
        Box(Modifier.padding(padding).fillMaxSize().padding(horizontal = 20.dp, vertical = 10.dp)) { FigmaEmptyState(title, message, icon) }
    } else {
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
private fun TwoTextDialog(title: String, label1: String, label2: String, onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var a by remember { mutableStateOf("") }; var b by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { OutlinedTextField(a, { a = it }, label = { Text(label1) }); OutlinedTextField(b, { b = it }, label = { Text(label2) }) } },
        confirmButton = { TextButton(enabled = a.isNotBlank(), onClick = { onSave(a.trim(), b.trim()) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun DatedItemDialog(title: String, label: String, onDismiss: () -> Unit, onSave: (String, Long?) -> Unit) {
    val context = LocalContext.current; var text by remember { mutableStateOf("") }; var date by remember { mutableStateOf<Long?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(text, { text = it }, label = { Text(label) })
                OutlinedButton(onClick = {
                    val d = LocalDate.now().plusMonths(1)
                    DatePickerDialog(context, { _, y, m, day -> date = LocalDate.of(y, m + 1, day).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() }, d.year, d.monthValue - 1, d.dayOfMonth).show()
                }) { Text(date?.let { DateTimeUtils.formatDate(it) } ?: "Choose target date") }
            }
        },
        confirmButton = { TextButton(enabled = text.isNotBlank(), onClick = { onSave(text.trim(), date) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun TripDialog(onDismiss: () -> Unit, onSave: (String, Long, Long) -> Unit) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var start by remember { mutableLongStateOf(LocalDate.now().plusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()) }
    var end by remember { mutableLongStateOf(LocalDate.now().plusDays(14).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()) }
    fun picker(current: Long, set: (Long) -> Unit) {
        val d = Instant.ofEpochMilli(current).atZone(ZoneId.systemDefault()).toLocalDate()
        DatePickerDialog(context, { _, y, m, day -> set(LocalDate.of(y, m + 1, day).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()) }, d.year, d.monthValue - 1, d.dayOfMonth).show()
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New trip") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Trip name") })
                OutlinedButton(onClick = { picker(start) { start = it } }) { Text("Start: ${DateTimeUtils.formatDate(start)}") }
                OutlinedButton(onClick = { picker(end) { end = it } }) { Text("End: ${DateTimeUtils.formatDate(end)}") }
            }
        },
        confirmButton = { TextButton(enabled = name.isNotBlank() && end >= start, onClick = { onSave(name.trim(), start, end) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}


@Composable
private fun OrganizerTextInputDialog(title: String, label: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { OutlinedTextField(text, { text = it }, label = { Text(label) }, singleLine = true) },
        confirmButton = { TextButton(enabled = text.isNotBlank(), onClick = { onSave(text.trim()) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
