package com.habitmind.data.model

data class Habit(
    val id: Int = 0,
    val title: String,
    val frequencyPerWeek: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

