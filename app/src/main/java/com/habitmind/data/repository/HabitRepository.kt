package com.habitmind.data.repository

import com.habitmind.data.model.Habit
import com.habitmind.data.model.HabitLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface HabitRepository {
    fun getAllActiveHabits(): Flow<List<Habit>>
    fun getLogsByHabitAndDateRange(habitId: Int, start: LocalDate, end: LocalDate): Flow<List<HabitLog>>
    fun getLogsByHabitAndWeek(habitId: Int, start: LocalDate, end: LocalDate): Flow<List<HabitLog>>
    fun getHabitStreak(habitId: Int): Int
    fun getCurrentWeekDateRange(): Pair<LocalDate, LocalDate>
    fun getCompletedCountInWeek(habitId: Int, start: LocalDate, end: LocalDate): Int
    fun getTodayDate(): LocalDate
    suspend fun insertHabit(habit: Habit)
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(habit: Habit)
    suspend fun toggleLogCompletion(habitId: Int, date: LocalDate)
    suspend fun getHabitById(id: Int): Habit?
}