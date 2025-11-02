package com.habitmind.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitmind.data.model.Habit
import com.habitmind.data.model.HabitLog
import com.habitmind.data.repository.AIRepository
import com.habitmind.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InsightViewModel @Inject constructor(
    private val aiRepository: AIRepository,
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<InsightUiState>(InsightUiState.Idle)
    val uiState: StateFlow<InsightUiState> = _uiState.asStateFlow()

    fun generateWeeklyInsight() {
        viewModelScope.launch {
            try {
                _uiState.value = InsightUiState.Loading

                // Get all active habits
                val habits = habitRepository.getAllActiveHabits().first()
                if (habits.isEmpty()) {
                    _uiState.value = InsightUiState.Error("No habits found. Add some habits first!")
                    return@launch
                }

                // Get logs for current week for each habit
                val (startDate, endDate) = habitRepository.getCurrentWeekDateRange()
                val logsMap = mutableMapOf<Int, List<HabitLog>>()

                habits.forEach { habit: Habit ->
                    val logs: List<HabitLog> = habitRepository.getLogsByHabitAndWeek(
                        habit.id,
                        startDate,
                        endDate
                    ).first()
                    logsMap[habit.id] = logs
                }

                // Generate AI insight
                val result = aiRepository.generateWeeklyInsight(habits, logsMap)
                result.fold(
                    onSuccess = { insight ->
                        _uiState.value = InsightUiState.Success(insight)
                    },
                    onFailure = { error ->
                        _uiState.value = InsightUiState.Error(
                            error.message ?: "Failed to generate insight. Please try again."
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = InsightUiState.Error(
                    e.message ?: "An error occurred. Please try again."
                )
            }
        }
    }

    fun clearUiState() {
        _uiState.value = InsightUiState.Idle
    }
}

sealed class InsightUiState {
    object Idle : InsightUiState()
    object Loading : InsightUiState()
    data class Success(val insight: String) : InsightUiState()
    data class Error(val message: String) : InsightUiState()
}

