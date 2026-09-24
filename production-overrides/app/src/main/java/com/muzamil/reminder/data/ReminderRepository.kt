package com.muzamil.reminder.data

import com.muzamil.reminder.domain.Defaults
import kotlinx.coroutines.flow.Flow
import java.time.*
import java.time.temporal.TemporalAdjusters

class ReminderRepository(private val dao: AppDao) {
    fun activeReminders() = dao.activeReminders()
    fun completedReminders() = dao.completedReminders()
    fun categories() = dao.categories()
    fun routines() = dao.routines()
    fun ideas() = dao.ideas()
    fun plans() = dao.plans()
    fun goals() = dao.goals()
    fun trips() = dao.trips()
    fun profiles() = dao.profiles()
    fun customSounds() = dao.customSounds()
    fun completedHistory() = dao.completedHistory()
    fun search(query: String) = dao.searchReminders(query)
    fun reminderByIdFlow(id: Long) = dao.reminderByIdFlow(id)
    suspend fun reminderById(id: Long) = dao.reminderById(id)
    fun overdue(now: Long = System.currentTimeMillis()) = dao.overdue(now)
    fun routineItems(id: Long) = dao.routineItems(id)
    fun tripPlaces(id: Long) = dao.tripPlaces(id)

    fun remindersForDay(date: LocalDate): Flow<List<ReminderEntity>> {
        val zone = ZoneId.systemDefault()
        val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
        return dao.remindersBetween(start, end)
    }

    suspend fun ensureDefaults() {
        if (dao.categoryCount() == 0) {
            dao.insertCategories(Defaults.categories.map { (name, icon) ->
                CategoryEntity(name = name, icon = icon, isDefault = true)
            })
        }
        if (dao.profileCount() == 0) dao.insertProfile(ProfileEntity(name = "Me", type = "ME"))
    }

    suspend fun saveReminder(reminder: ReminderEntity, offsetsMinutes: List<Long>): Long {
        val id = dao.insertReminder(reminder)
        dao.deleteAlertsForReminder(id)
        val alerts = offsetsMinutes.distinct().filter { it >= 0 }.map { offset ->
            ReminderAlertEntity(
                reminderId = id,
                offsetMinutes = offset,
                triggerTime = reminder.triggerAt - offset * 60_000L
            )
        }
        if (alerts.isNotEmpty()) dao.insertAlerts(alerts)
        return id
    }

    suspend fun updateReminder(reminder: ReminderEntity, offsetsMinutes: List<Long>) {
        dao.updateReminder(reminder.copy(updatedAt = System.currentTimeMillis()))
        dao.deleteAlertsForReminder(reminder.id)
        val alerts = offsetsMinutes.distinct().filter { it >= 0 }.map { offset ->
            ReminderAlertEntity(reminderId = reminder.id, offsetMinutes = offset, triggerTime = reminder.triggerAt - offset * 60_000L)
        }
        if (alerts.isNotEmpty()) dao.insertAlerts(alerts)
    }

    suspend fun alertsForReminder(id: Long) = dao.alertsForReminder(id)
    suspend fun pendingAlerts(now: Long = System.currentTimeMillis()) = dao.pendingAlerts(now)
    suspend fun markAlertTriggered(id: Long) = dao.markAlertTriggered(id)
    suspend fun deleteReminder(id: Long) = dao.softDeleteReminder(id)

    suspend fun completeReminder(reminder: ReminderEntity): ReminderEntity? {
        val now = System.currentTimeMillis()
        val category = reminder.categoryId?.let { dao.categoryById(it) }
        dao.insertCompletedHistory(
            CompletedReminderHistoryEntity(
                reminderId = reminder.id,
                title = reminder.title,
                completedAt = now,
                originalTriggerAt = reminder.triggerAt,
                categoryName = category?.name
            )
        )
        val next = nextOccurrence(reminder)
        return if (next != null) {
            val updated = reminder.copy(triggerAt = next, isCompleted = false, occurrenceCompletedAt = now, updatedAt = now)
            dao.updateReminder(updated)
            updated
        } else {
            dao.setReminderCompleted(reminder.id, true, now)
            null
        }
    }

    suspend fun snooze(reminder: ReminderEntity, minutes: Int): ReminderEntity {
        val next = System.currentTimeMillis() + minutes.coerceAtLeast(1) * 60_000L
        dao.rescheduleReminder(reminder.id, next)
        return reminder.copy(triggerAt = next, isCompleted = false, occurrenceCompletedAt = null, updatedAt = System.currentTimeMillis())
    }

    suspend fun duplicate(reminder: ReminderEntity): Long {
        val originalAlerts = dao.alertsForReminder(reminder.id).map { it.offsetMinutes }
        return saveReminder(reminder.copy(id = 0, title = "${reminder.title} (Copy)", createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()), originalAlerts)
    }

    suspend fun addCategory(name: String, icon: String = "label", colorArgb: Long? = null) = dao.insertCategory(CategoryEntity(name = name.trim(), icon = icon, colorArgb = colorArgb, isDefault = false))
    suspend fun deleteCategory(item: CategoryEntity) = dao.deleteCategory(item)
    suspend fun addRoutine(name: String) = dao.insertRoutine(RoutineEntity(name = name.trim()))
    suspend fun deleteRoutine(item: RoutineEntity) = dao.deleteRoutine(item)
    suspend fun addRoutineItem(item: RoutineItemEntity) = dao.insertRoutineItem(item)
    suspend fun deleteRoutineItem(item: RoutineItemEntity) = dao.deleteRoutineItem(item)
    suspend fun addIdea(item: IdeaEntity) = dao.insertIdea(item)
    suspend fun deleteIdea(item: IdeaEntity) = dao.deleteIdea(item)
    suspend fun addPlan(item: PlanEntity) = dao.insertPlan(item)
    suspend fun updatePlan(item: PlanEntity) = dao.updatePlan(item)
    suspend fun deletePlan(item: PlanEntity) = dao.deletePlan(item)
    suspend fun addGoal(item: GoalEntity) = dao.insertGoal(item)
    suspend fun updateGoal(item: GoalEntity) = dao.updateGoal(item)
    suspend fun deleteGoal(item: GoalEntity) = dao.deleteGoal(item)
    suspend fun addTrip(item: TripEntity) = dao.insertTrip(item)
    suspend fun deleteTrip(item: TripEntity) = dao.deleteTrip(item)
    suspend fun addTripPlace(item: TripPlaceEntity) = dao.insertTripPlace(item)
    suspend fun deleteTripPlace(item: TripPlaceEntity) = dao.deleteTripPlace(item)
    suspend fun addProfile(item: ProfileEntity) = dao.insertProfile(item)
    suspend fun deleteProfile(item: ProfileEntity) = dao.deleteProfile(item)
    suspend fun addCustomSound(item: CustomSoundEntity) = dao.insertCustomSound(item)
    suspend fun deleteCustomSound(item: CustomSoundEntity) = dao.deleteCustomSound(item)
    suspend fun clearCompletedHistory() = dao.clearCompletedHistory()

    fun nextOccurrence(reminder: ReminderEntity): Long? {
        val zone = ZoneId.of(reminder.timezone.ifBlank { ZoneId.systemDefault().id })
        val current = Instant.ofEpochMilli(reminder.triggerAt).atZone(zone)
        val type = reminder.type
        val next = when (type) {
            "DAILY" -> current.plusDays(reminder.repeatInterval.toLong())
            "WEEKLY" -> current.plusWeeks(reminder.repeatInterval.toLong())
            "MONTHLY" -> current.plusMonths(reminder.repeatInterval.toLong())
            "YEARLY" -> current.plusYears(reminder.repeatInterval.toLong())
            "WEEKDAYS" -> generateSequence(current.plusDays(1)) { it.plusDays(1) }.first { it.dayOfWeek !in setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY) }
            "WEEKENDS" -> generateSequence(current.plusDays(1)) { it.plusDays(1) }.first { it.dayOfWeek in setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY) }
            "SPECIFIC_DAYS" -> {
                val wanted = reminder.daysOfWeekCsv.split(',').mapNotNull { it.toIntOrNull() }.toSet()
                if (wanted.isEmpty()) null else generateSequence(current.plusDays(1)) { it.plusDays(1) }.first { it.dayOfWeek.value in wanted }
            }
            "CUSTOM" -> when (reminder.repeatUnit) {
                "MINUTES" -> current.plusMinutes(reminder.repeatInterval.toLong())
                "HOURS" -> current.plusHours(reminder.repeatInterval.toLong())
                "WEEKS" -> current.plusWeeks(reminder.repeatInterval.toLong())
                "MONTHS" -> current.plusMonths(reminder.repeatInterval.toLong())
                else -> current.plusDays(reminder.repeatInterval.toLong())
            }
            else -> null
        } ?: return null
        if (reminder.recurrenceEndAt != null && next.toInstant().toEpochMilli() > reminder.recurrenceEndAt) return null
        return next.toInstant().toEpochMilli()
    }
}
