package com.habitmind.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitmind.ui.components.HabitCard
import com.habitmind.ui.viewmodels.HabitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitListScreen(
    onNavigateToAddHabit: () -> Unit,
    onNavigateToEditHabit: (Int) -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToAIInsights: () -> Unit,
    viewModel: HabitViewModel = hiltViewModel()
) {
    val habitsWithProgress by viewModel.habitsWithProgress.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HabitMind") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddHabit) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add habit"
                )
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.TrendingUp, contentDescription = null) },
                    label = { Text("Progress") },
                    selected = false,
                    onClick = onNavigateToProgress
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Insights, contentDescription = null) },
                    label = { Text("AI Insights") },
                    selected = false,
                    onClick = onNavigateToAIInsights
                )
            }
        }
    ) { paddingValues ->
        when {
            habitsWithProgress.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = "No habits yet!",
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap the + button to add your first habit",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = habitsWithProgress,
                        key = { it.habit.id }
                    ) { habitWithProgress ->
                        HabitCard(
                            habitWithProgress = habitWithProgress,
                            onToggleCompletion = {
                                viewModel.toggleHabitCompletion(habitWithProgress.habit.id)
                            },
                            onEdit = {
                                onNavigateToEditHabit(habitWithProgress.habit.id)
                            },
                            onDelete = {
                                viewModel.deleteHabit(habitWithProgress.habit)
                            }
                        )
                    }
                }
            }
        }

        // Handle UI state - show snackbar on error
        val snackbarHostState = remember { SnackbarHostState() }
        
        LaunchedEffect(uiState) {
            if (uiState is com.habitmind.ui.viewmodels.HabitUiState.Error) {
                snackbarHostState.showSnackbar(
                    message = (uiState as com.habitmind.ui.viewmodels.HabitUiState.Error).message
                )
                viewModel.clearUiState()
            }
        }
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

