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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        FigmaBackground()
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 22.dp, top = 20.dp, end = 22.dp, bottom = 118.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(Modifier.size(46.dp), shape = CircleShape, color = FigmaPurpleSoft) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Person, null, tint = FigmaPurple, modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Hello!", color = FigmaSecondary, style = MaterialTheme.typography.bodyMedium)
                        Text("Your day, organized", color = FigmaInk, style = MaterialTheme.typography.titleLarge)
                    }
                    IconButton(onClick = onSearch) {
                        BadgedBox(badge = { if (overdue.isNotEmpty()) Badge { Text(overdue.size.coerceAtMost(9).toString()) } }) {
                            Icon(Icons.Outlined.Notifications, "Notifications", tint = FigmaInk)
                        }
                    }
                }
            }

            item {
                Surface(
                    color = FigmaPurple,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth().height(146.dp)
                ) {
                    Row(Modifier.fillMaxSize().padding(22.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Your today’s tasks", color = Color.White, style = MaterialTheme.typography.titleMedium)
                            Text(
                                if (today.isEmpty()) "All clear for now!" else "${today.size} reminder${if (today.size == 1) "" else "s"} waiting",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(18.dp))
                            Surface(
                                shape = RoundedCornerShape(9.dp),
                                color = Color.White,
                                modifier = Modifier.clickable(onClick = { if (today.isNotEmpty()) onEdit(today.first().id) else onAdd() })
                            ) {
                                Text(
                                    if (today.isEmpty()) "Add Task" else "View Task",
                                    color = FigmaPurple,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                                )
                            }
                        }
                        FigmaProgressRing(
                            value = if (today.isEmpty()) 1f else (1f - overdue.size.toFloat() / (today.size + overdue.size).coerceAtLeast(1)).coerceIn(.08f, 1f),
                            centerText = if (today.isEmpty()) "100%" else "${today.size}\nToday",
                            modifier = Modifier.size(78.dp)
                        )
                    }
                }
            }

            item { FigmaSectionHeader("In Progress", upcoming.size.coerceAtMost(99)) }
            item {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    val cards = (upcoming + today).distinctBy { it.id }.take(4)
                    if (cards.isEmpty()) {
                        ProgressProjectCard(null, onAdd)
                    } else {
                        cards.forEachIndexed { index, r ->
                            ProgressProjectCard(r, { onEdit(r.id) }, index)
                        }
                    }
                }
            }

            item { FigmaSectionHeader("Task Groups", categories.size.coerceAtMost(99)) }
            if (categories.isEmpty()) {
                item {
                    FigmaCard(Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            ColorIconTile(FigmaPurpleSoft, Modifier.size(36.dp)) { Icon(Icons.Outlined.Folder, null, tint = FigmaPurple, modifier = Modifier.size(20.dp)) }
                            Spacer(Modifier.width(12.dp))
                            Column { Text("General", fontWeight = FontWeight.SemiBold); Text("${active.size} Tasks", color = FigmaSecondary, style = MaterialTheme.typography.bodySmall) }
                        }
                    }
                }
            } else {
                items(categories.take(6).size) { index ->
                    val c = categories[index]
                    val count = active.count { it.categoryId == c.id }
                    val tint = listOf(FigmaPink, FigmaBlue, FigmaMint, FigmaWarm, FigmaPurpleSoft)[index % 5]
                    val icon = listOf(Icons.Outlined.WorkOutline, Icons.Outlined.PersonOutline, Icons.Outlined.MenuBook, Icons.Outlined.ShoppingBag, Icons.Outlined.FavoriteBorder)[index % 5]
                    FigmaCard(Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            ColorIconTile(tint, Modifier.size(36.dp)) { Icon(icon, null, tint = FigmaPurple, modifier = Modifier.size(20.dp)) }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(c.name, color = FigmaInk, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                                Text("$count Tasks", color = FigmaSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                            Surface(color = FigmaPurpleSoft, shape = CircleShape, modifier = Modifier.size(42.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(if (count == 0) "0%" else "${(count.coerceAtMost(10) * 10)}%", color = FigmaPurple, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
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
private fun ProgressProjectCard(reminder: ReminderEntity?, onClick: () -> Unit, index: Int = 0) {
    val tint = listOf(FigmaPurpleSoft, FigmaPink, FigmaBlue, FigmaMint)[index % 4]
    FigmaCard(
        modifier = Modifier.width(202.dp).height(116.dp).clickable(onClick = onClick),
        radius = 15.dp
    ) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                reminder?.let { DateTimeUtils.formatDate(it.triggerAt) } ?: "New reminder",
                color = FigmaSecondary,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(8.dp))
            Text(
                reminder?.title ?: "Create your next task",
                color = FigmaInk,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.weight(1f))
            Box(Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(tint)) {
                Box(Modifier.fillMaxWidth(if (reminder == null) .18f else .62f).fillMaxHeight().clip(CircleShape).background(FigmaPurple))
            }
        }
    }
}
