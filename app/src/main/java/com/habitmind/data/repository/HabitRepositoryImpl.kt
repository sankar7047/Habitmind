package com.habitmind.data.repository

import com.habitmind.data.local.HabitDao
import com.habitmind.data.local.HabitLogDao
import com.habitmind.data.local.HabitLogEntity
import com.habitmind.data.mapper.toEntity
import com.habitmind.data.mapper.toModel
import com.habitmind.data.model.Habit
import com.habitmind.data.model.HabitLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepositoryImpl @Inject constructor(
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao
) : HabitRepository {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun getAllActiveHabits(): Flow<List<Habit>> {
        return habitDao.getAllActiveHabits().map { entities ->
            entities.map { it.toModel() }
        }
    }

    override fun getLogsByHabitAndDateRange(
        habitId: Int, 
        start: LocalDate, 
        end: LocalDate
    ): Flow<List<HabitLog>> {
        return habitLogDao.getLogsByHabitAndDateRange(
            habitId = habitId,
            startDate = start.format(dateFormatter),
            endDate = end.format(dateFormatter)
        ).map { entities ->
            entities.map { entity ->
                HabitLog(
                    id = entity.id,
                    habitId = entity.habitId,
                    date = LocalDate.parse(entity.date, dateFormatter),
                    completed = entity.completed
                )
            }
        }
    }

    override fun getLogsByHabitAndWeek(habitId: Int, start: LocalDate, end: LocalDate): Flow<List<HabitLog>> {
        return getLogsByHabitAndDateRange(habitId, start, end)
    }

    override fun getHabitStreak(habitId: Int): Int {
        // For now, return 0. In a full implementation, this would calculate
        // consecutive completed days
        return 0
    }

    override fun getCurrentWeekDateRange(): Pair<LocalDate, LocalDate> {
        val today = LocalDate.now()
        val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        val endOfWeek = startOfWeek.plusDays(6)
        return startOfWeek to endOfWeek
    }

    override fun getCompletedCountInWeek(
        habitId: Int, 
        start: LocalDate, 
        end: LocalDate
    ): Int {
        // This would need to be implemented as a suspend function in a real implementation
        // For now, returning 0
        return 0
    }

    override fun getTodayDate(): LocalDate = LocalDate.now()

    override suspend fun insertHabit(habit: Habit) {
        habitDao.insertHabit(habit.toEntity())
    }

    override suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit.toEntity())
    }

    override suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteHabit(habit.toEntity())
    }

    override suspend fun toggleLogCompletion(habitId: Int, date: LocalDate) {
        val dateStr = date.format(dateFormatter)
        val existingLog = habitLogDao.getLogByHabitAndDate(habitId, dateStr)
        
        if (existingLog != null) {
            // Toggle existing log
            val updatedLog = existingLog.copy(completed = !existingLog.completed)
            habitLogDao.insertLog(updatedLog)
        } else {
            // Create new log as completed
            val newLog = HabitLogEntity(
                habitId = habitId,
                date = dateStr,
                completed = true
            )
            habitLogDao.insertLog(newLog)
        }
    }

    override suspend fun getHabitById(id: Int): Habit? {
        return habitDao.getHabitById(id)?.toModel()
    }
}