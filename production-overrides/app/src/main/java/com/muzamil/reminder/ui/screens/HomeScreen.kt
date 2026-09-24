package com.muzamil.reminder.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.muzamil.reminder.data.ReminderEntity
import com.muzamil.reminder.ui.*
import com.muzamil.reminder.util.DateTimeUtils

@Composable
fun HomeScreen(vm: MainViewModel, onAdd: () -> Unit, onEdit: (Long) -> Unit, onSearch: () -> Unit) {
    val today by vm.today.collectAsState()
    val upcoming by vm.upcoming.collectAsState()
    val overdue by vm.overdue.collectAsState()
    val active by vm.active.collectAsState()
    val categories by vm.categories.collectAsState()
    val history by vm.completedHistory.collectAsState()

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        FigmaBackground()
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 22.dp, top = 20.dp, end = 22.dp, bottom = 116.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(Modifier.size(46.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Hello!", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                        Text("Your day, organized", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge)
                    }
                    FigmaActionIconButton(Icons.Rounded.Search, "Search reminders", onSearch)
                }
            }

            item {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 146.dp)
                ) {
                    Row(Modifier.fillMaxWidth().padding(22.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Your today’s tasks", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(3.dp))
                            Text(
                                when {
                                    today.isEmpty() -> "All clear for now!"
                                    overdue.isNotEmpty() -> "${today.size} today • ${overdue.size} overdue"
                                    else -> "${today.size} reminder${if (today.size == 1) "" else "s"} waiting"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(18.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.clickable(onClick = { if (today.isNotEmpty()) onEdit(today.first().id) else onAdd() })
                            ) {
                                Text(
                                    if (today.isEmpty()) "Add Task" else "View Task",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        FigmaProgressRing(
                            value = if (today.isEmpty()) 1f else (1f - overdue.size.toFloat() / (today.size + overdue.size).coerceAtLeast(1)).coerceIn(.08f, 1f),
                            centerText = if (today.isEmpty()) "100%" else "${today.size}\nToday",
                            modifier = Modifier.size(78.dp)
                        )
                    }
                }
            }

            item { FigmaSectionHeader("In Progress", (upcoming + today).distinctBy { it.id }.size.coerceAtMost(99)) }
            item {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    val cards = (upcoming + today).distinctBy { it.id }.take(5)
                    if (cards.isEmpty()) {
                        ProgressTaskCard(null, onAdd)
                    } else {
                        cards.forEachIndexed { index, r -> ProgressTaskCard(r, { onEdit(r.id) }, index) }
                    }
                }
            }

            item { FigmaSectionHeader("Task Groups", categories.size.coerceAtMost(99)) }
            if (categories.isEmpty()) {
                item {
                    FigmaCard(Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            ColorIconTile(FigmaPurpleSoft, Modifier.size(38.dp)) {
                                Icon(Icons.Rounded.Folder, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("General", fontWeight = FontWeight.SemiBold)
                                Text("${active.size} Tasks", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            } else {
                items(categories.take(8).size) { index ->
                    val c = categories[index]
                    val activeCount = active.count { it.categoryId == c.id }
                    val completedCount = history.count { it.categoryName == c.name }
                    val total = activeCount + completedCount
                    val percent = if (total == 0) 0 else ((completedCount * 100f) / total).toInt().coerceIn(0, 100)
                    val fallback = categoryVisual(c.name, index)
                    val visual = CategoryVisual(categoryIcon(c.icon), if (c.colorArgb != null) categoryTint(c.colorArgb, index) else fallback.tint)
                    FigmaCard(Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            ColorIconTile(visual.tint, Modifier.size(38.dp)) {
                                Icon(visual.icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    c.name,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text("$activeCount active • $completedCount done", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            }
                            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape, modifier = Modifier.size(44.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("$percent%", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressTaskCard(reminder: ReminderEntity?, onClick: () -> Unit, index: Int = 0) {
    val tint = listOf(FigmaPurpleSoft, FigmaPink, FigmaBlue, FigmaMint)[index % 4]
    val progress = reminder?.let { r ->
        val span = (r.triggerAt - r.createdAt).coerceAtLeast(1L)
        val elapsed = (System.currentTimeMillis() - r.createdAt).coerceAtLeast(0L)
        (elapsed.toFloat() / span).coerceIn(.08f, .96f)
    } ?: .18f

    FigmaCard(
        modifier = Modifier.width(202.dp).heightIn(min = 116.dp).clickable(onClick = onClick),
        radius = 15.dp
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                reminder?.let { DateTimeUtils.formatDate(it.triggerAt) } ?: "New reminder",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
            Spacer(Modifier.height(8.dp))
            Text(
                reminder?.title ?: "Create your next task",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(14.dp))
            Box(Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(adaptiveFigmaTint(tint))) {
                Box(
                    Modifier.fillMaxWidth(progress).fillMaxHeight().clip(CircleShape).background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

private data class CategoryVisual(val icon: ImageVector, val tint: androidx.compose.ui.graphics.Color)

private fun categoryVisual(name: String, index: Int): CategoryVisual = when (name.lowercase()) {
    "work", "meeting" -> CategoryVisual(Icons.Rounded.Work, FigmaPink)
    "personal" -> CategoryVisual(Icons.Rounded.Person, FigmaBlue)
    "family", "birthday" -> CategoryVisual(Icons.Rounded.FamilyRestroom, FigmaPink)
    "payment" -> CategoryVisual(Icons.Rounded.Payments, FigmaWarm)
    "documents" -> CategoryVisual(Icons.Rounded.Description, FigmaBlue)
    "travel" -> CategoryVisual(Icons.Rounded.Flight, FigmaBlue)
    "fitness" -> CategoryVisual(Icons.Rounded.FitnessCenter, FigmaMint)
    "food" -> CategoryVisual(Icons.Rounded.Restaurant, FigmaWarm)
    "routine" -> CategoryVisual(Icons.Rounded.Repeat, FigmaPurpleSoft)
    "pets" -> CategoryVisual(Icons.Rounded.Pets, FigmaMint)
    "vehicle" -> CategoryVisual(Icons.Rounded.DirectionsCar, FigmaBlue)
    "ideas" -> CategoryVisual(Icons.Rounded.Lightbulb, FigmaWarm)
    "goals" -> CategoryVisual(Icons.Rounded.Flag, FigmaMint)
    "plans" -> CategoryVisual(Icons.Rounded.EventNote, FigmaPurpleSoft)
    else -> CategoryVisual(
        listOf(Icons.Rounded.Folder, Icons.Rounded.Label, Icons.Rounded.CheckCircle)[index % 3],
        listOf(FigmaPurpleSoft, FigmaPink, FigmaBlue, FigmaMint, FigmaWarm)[index % 5]
    )
}
