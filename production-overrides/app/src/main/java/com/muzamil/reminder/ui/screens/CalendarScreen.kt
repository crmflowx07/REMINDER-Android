package com.muzamil.reminder.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.muzamil.reminder.data.CompletedReminderHistoryEntity
import com.muzamil.reminder.data.ReminderEntity
import com.muzamil.reminder.ui.*
import com.muzamil.reminder.ui.components.ReminderCard
import com.muzamil.reminder.util.DateTimeUtils
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@Composable
fun CalendarScreen(vm: MainViewModel, onBack: () -> Unit, onEdit: (Long) -> Unit) {
    var selected by remember { mutableStateOf(LocalDate.now()) }
    var month by remember { mutableStateOf(YearMonth.from(selected)) }
    val reminders by remember(selected) { vm.repository.remindersForDay(selected) }.collectAsState(initial = emptyList())
    val active by vm.active.collectAsState()
    val history by vm.completedHistory.collectAsState()
    val categories by vm.categories.collectAsState()
    val settings by vm.settings.collectAsState()
    val zone = ZoneId.systemDefault()

    val completedForSelected = remember(history, selected) {
        history.filter { Instant.ofEpochMilli(it.originalTriggerAt).atZone(zone).toLocalDate() == selected }
    }

    FigmaPageScaffold(
        title = "Calendar",
        onBack = onBack,
        actions = {
            TextButton(onClick = { selected = LocalDate.now(); month = YearMonth.now() }) { Text("Today") }
        }
    ) { padding ->
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                MonthCalendarCard(
                    month = month,
                    selected = selected,
                    weekStartsMonday = settings.weekStartsMonday,
                    active = active,
                    completedHistory = history,
                    categories = categories.associateBy { it.id },
                    onPrevious = { month = month.minusMonths(1) },
                    onNext = { month = month.plusMonths(1) },
                    onSelect = { selected = it; month = YearMonth.from(it) }
                )
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        FigmaSectionHeader(selected.format(DateTimeFormatter.ofPattern("EEEE, d MMM")), reminders.size + completedForSelected.size)
                        Text("Tap a task to edit or reschedule it.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            if (reminders.isEmpty() && completedForSelected.isEmpty()) {
                item {
                    FigmaEmptyState("No reminders", "Your schedule is clear for this date.", Icons.Rounded.CalendarMonth)
                }
            } else {
                items(reminders.size) { index ->
                    val r = reminders[index]
                    ReminderCard(
                        reminder = r,
                        use24h = settings.use24Hour,
                        onEdit = { onEdit(r.id) },
                        onComplete = { vm.completeReminder(r) },
                        onSnooze = { minutes -> vm.snooze(r, minutes) },
                        onDelete = { vm.deleteReminder(r) },
                        onDuplicate = { vm.duplicate(r) }
                    )
                }
                if (completedForSelected.isNotEmpty()) {
                    item { Text("Completed", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold) }
                    items(completedForSelected.size) { index ->
                        val h = completedForSelected[index]
                        FigmaRowCard(
                            title = h.title,
                            subtitle = "Completed ${DateTimeUtils.formatDateTime(h.completedAt, settings.use24Hour)}",
                            icon = Icons.Rounded.TaskAlt,
                            tint = FigmaMint
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun MonthCalendarCard(
    month: YearMonth,
    selected: LocalDate,
    weekStartsMonday: Boolean,
    active: List<ReminderEntity>,
    completedHistory: List<CompletedReminderHistoryEntity>,
    categories: Map<Long, com.muzamil.reminder.data.CategoryEntity>,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSelect: (LocalDate) -> Unit
) {
    val zone = ZoneId.systemDefault()
    val firstDow = if (weekStartsMonday) DayOfWeek.MONDAY else DayOfWeek.SUNDAY
    val first = month.atDay(1)
    val gridStart = first.with(TemporalAdjusters.previousOrSame(firstDow))
    val dates = (0L until 42L).map { gridStart.plusDays(it) }
    val dayNames = (0L until 7L).map { firstDow.plus(it) }.map { it.name.take(3).lowercase().replaceFirstChar(Char::uppercase) }
    val today = LocalDate.now()
    val now = System.currentTimeMillis()

    fun activeFor(date: LocalDate) = active.filter { Instant.ofEpochMilli(it.triggerAt).atZone(zone).toLocalDate() == date }
    fun completedFor(date: LocalDate) = completedHistory.filter { Instant.ofEpochMilli(it.originalTriggerAt).atZone(zone).toLocalDate() == date }

    FigmaCard(Modifier.fillMaxWidth(), radius = 18.dp) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FigmaActionIconButton(Icons.Rounded.ChevronLeft, "Previous month", onPrevious)
                Text(
                    month.atDay(1).format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                FigmaActionIconButton(Icons.Rounded.ChevronRight, "Next month", onNext)
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth()) {
                dayNames.forEach { name ->
                    Text(name, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(6.dp))

            dates.chunked(7).forEach { week ->
                Row(Modifier.fillMaxWidth()) {
                    week.forEach { date ->
                        val dayActive = activeFor(date)
                        val dayCompleted = completedFor(date)
                        val inMonth = YearMonth.from(date) == month
                        val hasOverdue = dayActive.any { it.triggerAt < now }
                        val hasMeeting = dayActive.any { categories[it.categoryId]?.name == "Meeting" }
                        CalendarDayCell(
                            date = date,
                            selected = date == selected,
                            today = date == today,
                            inMonth = inMonth,
                            hasReminder = dayActive.isNotEmpty(),
                            hasMeeting = hasMeeting,
                            hasOverdue = hasOverdue,
                            hasCompleted = dayCompleted.isNotEmpty(),
                            onClick = { onSelect(date) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CalendarLegendDot(MaterialTheme.colorScheme.primary, "Reminder")
                CalendarLegendDot(Color(0xFF4C8EDB), "Meeting")
                CalendarLegendDot(MaterialTheme.colorScheme.error, "Overdue")
                CalendarLegendDot(Color(0xFF50A782), "Done")
            }
        }
    }
}

@Composable
private fun RowScope.CalendarLegendDot(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    }
}

@Composable
private fun RowScope.CalendarDayCell(
    date: LocalDate,
    selected: Boolean,
    today: Boolean,
    inMonth: Boolean,
    hasReminder: Boolean,
    hasMeeting: Boolean,
    hasOverdue: Boolean,
    hasCompleted: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent
    val textColor = when {
        selected -> MaterialTheme.colorScheme.onPrimary
        !inMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .42f)
        else -> MaterialTheme.colorScheme.onSurface
    }
    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(bg)
            .then(if (today && !selected) Modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = .7f)) else Modifier)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(date.dayOfMonth.toString(), color = textColor, style = MaterialTheme.typography.labelMedium, fontWeight = if (selected || today) FontWeight.SemiBold else FontWeight.Normal)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                if (hasReminder) CalendarDot(if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
                if (hasMeeting) CalendarDot(if (selected) MaterialTheme.colorScheme.onPrimary.copy(alpha = .75f) else Color(0xFF4C8EDB))
                if (hasOverdue) CalendarDot(if (selected) MaterialTheme.colorScheme.onPrimary.copy(alpha = .6f) else MaterialTheme.colorScheme.error)
                if (hasCompleted) CalendarDot(if (selected) MaterialTheme.colorScheme.onPrimary.copy(alpha = .5f) else Color(0xFF50A782))
            }
        }
    }
}

@Composable
private fun CalendarDot(color: androidx.compose.ui.graphics.Color) {
    Box(Modifier.size(4.dp).clip(CircleShape).background(color))
}
