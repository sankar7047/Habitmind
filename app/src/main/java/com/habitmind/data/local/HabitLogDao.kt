package com.habitmind.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitLogDao {
    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND date BETWEEN :startDate AND :endDate")
    fun getLogsByHabitAndDateRange(
        habitId: Int,
        startDate: String,
        endDate: String
    ): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getLogByHabitAndDate(habitId: Int, date: String): HabitLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HabitLogEntity): Long

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND date = :date")
    suspend fun deleteLogByHabitAndDate(habitId: Int, date: String)

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY date DESC")
    fun getLogsByHabit(habitId: Int): Flow<List<HabitLogEntity>>

    @Query("SELECT COUNT(*) FROM habit_logs WHERE habitId = :habitId AND completed = 1 AND date BETWEEN :startDate AND :endDate")
    suspend fun getCompletedCountInRange(
        habitId: Int,
        startDate: String,
        endDate: String
    ): Int
}

