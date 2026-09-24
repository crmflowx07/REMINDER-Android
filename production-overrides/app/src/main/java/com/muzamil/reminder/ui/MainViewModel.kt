package com.muzamil.reminder.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.muzamil.reminder.ReminderApplication
import com.muzamil.reminder.data.*
import com.muzamil.reminder.scheduling.ScheduleCoordinator
import com.muzamil.reminder.util.DateTimeUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.ZoneId

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as ReminderApplication
    val repository = app.repository
    val settingsStore = app.settingsStore
    private val coordinator = ScheduleCoordinator(application, repository)

    val settings = settingsStore.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())
    val active = repository.activeReminders().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val categories = repository.categories().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val routines = repository.routines().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val ideas = repository.ideas().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val plans = repository.plans().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val goals = repository.goals().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val trips = repository.trips().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val profiles = repository.profiles().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val customSounds = repository.customSounds().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val completedHistory = repository.completedHistory().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val today = active.map { list -> list.filter { it.triggerAt in DateTimeUtils.startOfToday()..DateTimeUtils.endOfToday() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val overdue = active.map { list -> list.filter { it.triggerAt < System.currentTimeMillis() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val upcoming = active.map { list -> list.filter { it.triggerAt > DateTimeUtils.endOfToday() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun reminder(id: Long) = repository.reminderById(id)
    suspend fun alertOffsets(id: Long) = repository.alertsForReminder(id).map { it.offsetMinutes }

    fun saveReminder(reminder: ReminderEntity, offsets: List<Long>, onDone: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val allOffsets = (offsets + 0L).distinct()
            val id = if (reminder.id == 0L) repository.saveReminder(reminder, allOffsets)
            else { coordinator.cancel(reminder.id); repository.updateReminder(reminder, allOffsets); reminder.id }
            repository.reminderById(id)?.let { coordinator.reschedule(it) }
            onDone(id)
        }
    }

    fun deleteReminder(reminder: ReminderEntity) = viewModelScope.launch { coordinator.cancel(reminder.id); repository.deleteReminder(reminder.id) }
    fun completeReminder(reminder: ReminderEntity) = viewModelScope.launch {
        val offsets = repository.alertsForReminder(reminder.id).map { it.offsetMinutes }.ifEmpty { listOf(0L) }
        coordinator.cancel(reminder.id)
        val next = repository.completeReminder(reminder)
        if (next != null) { repository.updateReminder(next, offsets); coordinator.reschedule(next) }
    }
    fun snooze(reminder: ReminderEntity, minutes: Int) = viewModelScope.launch {
        coordinator.cancel(reminder.id)
        val updated = repository.snooze(reminder, minutes)
        repository.updateReminder(updated, listOf(0L))
        coordinator.reschedule(updated)
    }
    fun duplicate(reminder: ReminderEntity) = viewModelScope.launch {
        val id = repository.duplicate(reminder)
        repository.reminderById(id)?.let { coordinator.reschedule(it) }
    }

    fun addCategory(name: String, icon: String = "label", colorArgb: Long? = null) = viewModelScope.launch { if (name.isNotBlank()) repository.addCategory(name, icon, colorArgb) }
    fun deleteCategory(item: CategoryEntity) = viewModelScope.launch { if (!item.isDefault) repository.deleteCategory(item) }
    fun addRoutine(name: String) = viewModelScope.launch { if (name.isNotBlank()) repository.addRoutine(name) }
    fun deleteRoutine(item: RoutineEntity) = viewModelScope.launch {
        repository.routineItems(item.id).first().forEach { ri ->
            ri.reminderId?.let { coordinator.cancel(it); repository.deleteReminder(it) }
        }
        repository.deleteRoutine(item)
    }
    fun addRoutineItem(item: RoutineItemEntity) = viewModelScope.launch { createRoutineItemReminder(item) }
    fun deleteRoutineItem(item: RoutineItemEntity) = viewModelScope.launch {
        item.reminderId?.let { id -> coordinator.cancel(id); repository.reminderById(id)?.let { repository.deleteReminder(it.id) } }
        repository.deleteRoutineItem(item)
    }

    private suspend fun createRoutineItemReminder(item: RoutineItemEntity) {
        val zone = ZoneId.systemDefault()
        val now = java.time.ZonedDateTime.now(zone)
        var dt = now.toLocalDate().atStartOfDay(zone).plusMinutes(item.minutesFromMidnight.toLong())
        if (!dt.isAfter(now)) dt = dt.plusDays(1)
        val routineCategory = categories.value.firstOrNull { it.name == "Routine" }?.id
        val reminder = ReminderEntity(
            title = item.title,
            triggerAt = dt.toInstant().toEpochMilli(),
            timezone = zone.id,
            type = "DAILY",
            categoryId = item.categoryId ?: routineCategory,
            soundKey = settings.value.defaultSound,
            vibrationEnabled = settings.value.vibration,
            snoozeDurationMinutes = settings.value.defaultSnoozeMinutes
        )
        val reminderId = repository.saveReminder(reminder, listOf(0L))
        repository.addRoutineItem(item.copy(reminderId = reminderId))
        repository.reminderById(reminderId)?.let { coordinator.reschedule(it) }
    }
    fun addIdea(item: IdeaEntity) = viewModelScope.launch { repository.addIdea(item) }
    fun deleteIdea(item: IdeaEntity) = viewModelScope.launch { repository.deleteIdea(item) }
    fun addPlan(item: PlanEntity) = viewModelScope.launch { repository.addPlan(item) }
    fun updatePlan(item: PlanEntity) = viewModelScope.launch { repository.updatePlan(item) }
    fun deletePlan(item: PlanEntity) = viewModelScope.launch { repository.deletePlan(item) }
    fun addGoal(item: GoalEntity) = viewModelScope.launch { repository.addGoal(item) }
    fun updateGoal(item: GoalEntity) = viewModelScope.launch { repository.updateGoal(item) }
    fun deleteGoal(item: GoalEntity) = viewModelScope.launch { repository.deleteGoal(item) }
    fun addTrip(item: TripEntity) = viewModelScope.launch { repository.addTrip(item) }
    fun deleteTrip(item: TripEntity) = viewModelScope.launch { repository.deleteTrip(item) }
    fun addProfile(item: ProfileEntity) = viewModelScope.launch { repository.addProfile(item) }
    fun deleteProfile(item: ProfileEntity) = viewModelScope.launch { if (item.type != "ME") repository.deleteProfile(item) }
    fun addCustomSound(item: CustomSoundEntity) = viewModelScope.launch { repository.addCustomSound(item) }
    fun deleteCustomSound(item: CustomSoundEntity) = viewModelScope.launch { repository.deleteCustomSound(item) }
    fun clearHistory() = viewModelScope.launch { repository.clearCompletedHistory() }

    fun search(query: String): Flow<List<ReminderEntity>> = repository.search(query.trim())


    fun loadDemoData(onDone: (Int) -> Unit = {}) = viewModelScope.launch {
        if (active.value.any { it.title == "Feed Birds" || it.title == "Team Meeting" }) { onDone(0); return@launch }
        val zone = ZoneId.systemDefault()
        val now = java.time.ZonedDateTime.now(zone)
        fun nextAt(hour: Int, minute: Int): Long {
            var dt = now.toLocalDate().atTime(hour, minute).atZone(zone)
            if (!dt.isAfter(now)) dt = dt.plusDays(1)
            return dt.toInstant().toEpochMilli()
        }
        val categoryByName = categories.value.associateBy { it.name }
        val demos = listOf(
            Triple("Feed Birds", 8 to 0, "Pets"),
            Triple("Leave for Office", 9 to 0, "Work"),
            Triple("Team Meeting", 10 to 30, "Meeting"),
            Triple("Lunch", 13 to 0, "Food"),
            Triple("Pick Up Child", 14 to 0, "Family"),
            Triple("Workout", 18 to 0, "Fitness"),
            Triple("Family Time", 20 to 0, "Family")
        )
        var count = 0
        demos.forEach { (title, time, category) ->
            val r = ReminderEntity(title = title, triggerAt = nextAt(time.first, time.second), timezone = zone.id, categoryId = categoryByName[category]?.id)
            val id = repository.saveReminder(r, listOf(0L))
            repository.reminderById(id)?.let { coordinator.reschedule(it) }
            count++
        }
        val routines = listOf(
            "Morning Routine" to listOf("Wake up" to 420, "Water" to 435, "Breakfast" to 450, "Leave for office" to 480),
            "Fitness Routine" to listOf("Workout" to 360, "Walk" to 405, "Breakfast" to 420),
            "Pet / Bird Routine" to listOf("Feed birds" to 480, "Give water" to 780, "Feed birds" to 1080),
            "Work Routine" to listOf("Start office" to 510, "Lunch" to 780, "Review tasks" to 1020)
        )
        routines.forEach { (name, items) ->
            val rid = repository.addRoutine(name)
            items.forEachIndexed { index, pair -> createRoutineItemReminder(RoutineItemEntity(routineId = rid, title = pair.first, minutesFromMidnight = pair.second, sortOrder = index)) }
        }
        repository.addIdea(IdeaEntity(title = "Start online business", description = "Open a clothing business."))
        repository.addPlan(PlanEntity(goal = "Learn coding", targetAt = now.plusMonths(6).toInstant().toEpochMilli(), progress = 10))
        repository.addGoal(GoalEntity(title = "Exercise regularly", targetAt = now.plusMonths(3).toInstant().toEpochMilli(), reminderFrequency = "DAILY", progress = 20))
        val tripId = repository.addTrip(TripEntity(name = "Washington Trip", startAt = now.plusMonths(2).toInstant().toEpochMilli(), endAt = now.plusMonths(2).plusDays(7).toInstant().toEpochMilli()))
        listOf("White House", "National Mall", "Smithsonian", "Washington Monument").forEach { repository.addTripPlace(TripPlaceEntity(tripId = tripId, name = it)) }
        onDone(count)
    }

    fun exportJson(): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("reminders", JSONArray().apply {
            active.value.forEach { r ->
                put(JSONObject().apply {
                    put("title", r.title); put("description", r.description); put("triggerAt", r.triggerAt)
                    put("timezone", r.timezone); put("type", r.type); put("repeatInterval", r.repeatInterval); put("repeatUnit", r.repeatUnit)
                    put("daysOfWeekCsv", r.daysOfWeekCsv); put("priority", r.priority); put("soundKey", r.soundKey)
                    put("vibrationEnabled", r.vibrationEnabled); put("snoozeDurationMinutes", r.snoozeDurationMinutes)
                })
            }
        })
        return root.toString(2)
    }

    fun importJson(json: String, onResult: (Int) -> Unit) = viewModelScope.launch {
        var imported = 0
        runCatching {
            val arr = JSONObject(json).optJSONArray("reminders") ?: JSONArray()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val reminder = ReminderEntity(
                    title = o.optString("title", "Imported reminder"),
                    description = o.optString("description", ""),
                    triggerAt = o.optLong("triggerAt", System.currentTimeMillis() + 60_000),
                    timezone = o.optString("timezone", ZoneId.systemDefault().id),
                    type = o.optString("type", "ONE_TIME"),
                    repeatInterval = o.optInt("repeatInterval", 1),
                    repeatUnit = o.optString("repeatUnit", "DAYS"),
                    daysOfWeekCsv = o.optString("daysOfWeekCsv", ""),
                    priority = o.optString("priority", "NORMAL"),
                    soundKey = o.optString("soundKey", "classic_bell"),
                    vibrationEnabled = o.optBoolean("vibrationEnabled", true),
                    snoozeDurationMinutes = o.optInt("snoozeDurationMinutes", 10)
                )
                val id = repository.saveReminder(reminder, listOf(0L))
                repository.reminderById(id)?.let { coordinator.reschedule(it) }
                imported++
            }
        }
        onResult(imported)
    }
}
