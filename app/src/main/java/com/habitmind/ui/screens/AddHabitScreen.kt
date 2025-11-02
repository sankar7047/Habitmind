package com.habitmind.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitmind.ui.viewmodels.HabitViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(
    habitId: Int = 0,
    onNavigateBack: () -> Unit,
    viewModel: HabitViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var title by remember { mutableStateOf("") }
    var frequencyText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val isEditMode = habitId != 0

    // Load habit if editing
    LaunchedEffect(habitId) {
        if (isEditMode) {
            scope.launch {
                val habit = viewModel.habits.first().find { it.id == habitId }
                    ?: viewModel.getHabitById(habitId)
                habit?.let {
                    title = it.title
                    frequencyText = it.frequencyPerWeek.toString()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Habit" else "Add Habit") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Habit Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = frequencyText,
                onValueChange = { if (it.all { char -> char.isDigit() }) frequencyText = it },
                label = { Text("Times per Week") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val frequency = frequencyText.toIntOrNull() ?: 0
                    if (title.isNotBlank() && frequency > 0 && frequency <= 7) {
                        if (isEditMode) {
                            scope.launch {
                                val habit = viewModel.habits.first().find { it.id == habitId }
                                habit?.let {
                                    viewModel.updateHabit(
                                        it.copy(
                                            title = title,
                                            frequencyPerWeek = frequency
                                        )
                                    )
                                }
                            }
                        } else {
                            viewModel.addHabit(title, frequency)
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = title.isNotBlank() && frequencyText.toIntOrNull()?.let { it > 0 && it <= 7 } == true
            ) {
                Text(if (isEditMode) "Update Habit" else "Add Habit")
            }

            // Handle UI state
            when (val state = uiState) {
                is com.habitmind.ui.viewmodels.HabitUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is com.habitmind.ui.viewmodels.HabitUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                else -> {}
            }
        }
    }
}

