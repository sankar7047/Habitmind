package com.habitmind.data.model

import java.time.LocalDate

data class HabitLog(
    val id: Int = 0,
    val habitId: Int,
    val date: LocalDate,
    val completed: Boolean
)