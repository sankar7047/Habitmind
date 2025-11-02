package com.habitmind.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val frequencyPerWeek: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

