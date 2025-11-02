package com.habitmind.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitmind.data.model.Habit
import com.habitmind.data.model.HabitLog
import com.habitmind.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HabitProgressData(
    val habit: Habit,
    val weeklyLogs: List<HabitLog>,
    val streak: Int,
    val successRate: Float
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _progressData = MutableStateFlow<List<HabitProgressData>>(emptyList())
    val progressData: StateFlow<List<HabitProgressData>> = _progressData.asStateFlow()

    private val _uiState = MutableStateFlow<ProgressUiState>(ProgressUiState.Loading)
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        loadProgressData()
    }

    fun loadProgressData() {
        viewModelScope.launch {
            try {
                _uiState.value = ProgressUiState.Loading

                val habits = habitRepository.getAllActiveHabits().first()
                val (startDate, endDate) = habitRepository.getCurrentWeekDateRange()

                val progressList = habits.map { habit: Habit ->
                    val weeklyLogs: List<HabitLog> = habitRepository.getLogsByHabitAndWeek(
                        habit.id,
                        startDate,
                        endDate
                    ).first()

                    val streak = habitRepository.getHabitStreak(habit.id)
                    val completedCount = habitRepository.getCompletedCountInWeek(
                        habit.id,
                        startDate,
                        endDate
                    )
                    val successRate = if (habit.frequencyPerWeek > 0) {
                        completedCount.toFloat() / habit.frequencyPerWeek
                    } else 0f

                    HabitProgressData(
                        habit = habit,
                        weeklyLogs = weeklyLogs,
                        streak = streak,
                        successRate = successRate.coerceIn(0f, 1f)
                    )
                }

                _progressData.value = progressList
                _uiState.value = ProgressUiState.Success
            } catch (e: Exception) {
                _uiState.value = ProgressUiState.Error(
                    e.message ?: "Failed to load progress data"
                )
            }
        }
    }
}

sealed class ProgressUiState {
    object Loading : ProgressUiState()
    object Success : ProgressUiState()
    data class Error(val message: String) : ProgressUiState()
}

