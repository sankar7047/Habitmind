package com.habitmind.data.mapper

import com.habitmind.data.model.Habit
import com.habitmind.data.local.HabitEntity

fun HabitEntity.toModel(): Habit =
    Habit(
        id = id,
        title = title,
        frequencyPerWeek = frequencyPerWeek,
        createdAt = createdAt,
        isActive = isActive
    )

fun Habit.toEntity(): HabitEntity =
    HabitEntity(
        id = id,
        title = title,
        frequencyPerWeek = frequencyPerWeek,
        createdAt = createdAt,
        isActive = isActive
    )