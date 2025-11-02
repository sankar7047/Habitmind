package com.habitmind.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitmind.data.model.Habit
import com.habitmind.data.model.HabitLog
import com.habitmind.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HabitWithProgress(
    val habit: Habit,
    val completedToday: Boolean,
    val streak: Int,
    val weeklyCompletionRate: Float
)

@HiltViewModel
class HabitViewModel @Inject constructor(
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    private val _habitsWithProgress = MutableStateFlow<List<HabitWithProgress>>(emptyList())
    val habitsWithProgress: StateFlow<List<HabitWithProgress>> = _habitsWithProgress.asStateFlow()

    private val _uiState = MutableStateFlow<HabitUiState>(HabitUiState.Idle)
    val uiState: StateFlow<HabitUiState> = _uiState.asStateFlow()

    init { loadHabits() }

    fun loadHabits() {
        viewModelScope.launch {
            habitRepository.getAllActiveHabits().collect { list ->
                _habits.value = list
                loadProgressForHabits(list)
            }
        }
    }

    private fun loadProgressForHabits(habitList: List<Habit>) {
        viewModelScope.launch {
            val today = habitRepository.getTodayDate()
            val result = habitList.map { habit ->
                val todayLogs: List<HabitLog> =
                    habitRepository.getLogsByHabitAndDateRange(habit.id, today, today).first()
                val completedToday = todayLogs.any { log -> log.completed && log.date == today }
                val streak = habitRepository.getHabitStreak(habit.id)
                val (startDate, endDate) = habitRepository.getCurrentWeekDateRange()
                val completedCount = habitRepository.getCompletedCountInWeek(habit.id, startDate, endDate)
                val weeklyRate = if (habit.frequencyPerWeek > 0)
                    (completedCount.toFloat() / habit.frequencyPerWeek).coerceIn(0f, 1f) else 0f
                HabitWithProgress(habit, completedToday, streak, weeklyRate)
            }
            _habitsWithProgress.value = result
        }
    }

    fun addHabit(title: String, frequencyPerWeek: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = HabitUiState.Loading
                habitRepository.insertHabit(Habit(title = title, frequencyPerWeek = frequencyPerWeek))
                _uiState.value = HabitUiState.Success("Habit added")
            } catch (e: Exception) {
                _uiState.value = HabitUiState.Error(e.message ?: "Add failed")
            }
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            try {
                _uiState.value = HabitUiState.Loading
                habitRepository.updateHabit(habit)
                _uiState.value = HabitUiState.Success("Habit updated")
            } catch (e: Exception) {
                _uiState.value = HabitUiState.Error(e.message ?: "Update failed")
            }
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            try {
                habitRepository.deleteHabit(habit)
            } catch (e: Exception) {
                _uiState.value = HabitUiState.Error(e.message ?: "Delete failed")
            }
        }
    }

    fun toggleHabitCompletion(habitId: Int) {
        viewModelScope.launch {
            try {
                habitRepository.toggleLogCompletion(habitId, habitRepository.getTodayDate())
            } catch (e: Exception) {
                _uiState.value = HabitUiState.Error(e.message ?: "Toggle failed")
            }
        }
    }

    suspend fun getHabitById(id: Int): Habit? = habitRepository.getHabitById(id)
    fun clearUiState() { _uiState.value = HabitUiState.Idle }
}

sealed class HabitUiState {
    object Idle : HabitUiState()
    object Loading : HabitUiState()
    data class Success(val message: String) : HabitUiState()
    data class Error(val message: String) : HabitUiState()
}