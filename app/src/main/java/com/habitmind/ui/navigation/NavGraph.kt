package com.habitmind.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.habitmind.data.model.Habit
import com.habitmind.ui.screens.AddHabitScreen
import com.habitmind.ui.screens.AIInsightsScreen
import com.habitmind.ui.screens.HabitListScreen
import com.habitmind.ui.screens.ProgressScreen

sealed class Screen(val route: String) {
    object HabitList : Screen("habit_list")
    object AddHabit : Screen("add_habit")
    object EditHabit : Screen("edit_habit/{habitId}") {
        fun createRoute(habitId: Int) = "edit_habit/$habitId"
    }
    object Progress : Screen("progress")
    object AIInsights : Screen("ai_insights")
}

@Composable
fun HabitMindNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.HabitList.route
    ) {
        composable(Screen.HabitList.route) {
            HabitListScreen(
                onNavigateToAddHabit = {
                    navController.navigate(Screen.AddHabit.route)
                },
                onNavigateToEditHabit = { habitId ->
                    navController.navigate(Screen.EditHabit.createRoute(habitId))
                },
                onNavigateToProgress = {
                    navController.navigate(Screen.Progress.route)
                },
                onNavigateToAIInsights = {
                    navController.navigate(Screen.AIInsights.route)
                }
            )
        }

        composable(Screen.AddHabit.route) {
            AddHabitScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.EditHabit.route) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId")?.toIntOrNull() ?: 0
            AddHabitScreen(
                habitId = habitId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Progress.route) {
            ProgressScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AIInsights.route) {
            AIInsightsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

