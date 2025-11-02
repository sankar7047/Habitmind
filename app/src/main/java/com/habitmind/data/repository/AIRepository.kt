package com.habitmind.data.repository

import com.habitmind.data.model.Habit
import com.habitmind.data.model.HabitLog
import com.habitmind.data.remote.Message
import com.habitmind.data.remote.OpenAIApiService
import com.habitmind.data.remote.OpenAIRequest
import javax.inject.Inject

class AIRepository @Inject constructor(
    private val openAIApiService: OpenAIApiService,
    private val apiKey: String
) {
    suspend fun generateWeeklyInsight(
        habits: List<Habit>,
        logs: Map<Int, List<HabitLog>>
    ): Result<String> {
        return try {
            val prompt = buildPrompt(habits, logs)
            val request = OpenAIRequest(
                model = "gpt-4-turbo",
                messages = listOf(
                    Message(
                        role = "system",
                        content = "You are a friendly personal habit coach. Provide encouraging, motivating, and practical advice."
                    ),
                    Message(
                        role = "user",
                        content = prompt
                    )
                ),
                max_tokens = 200
            )

            val response = openAIApiService.getAIResponse(
                authorization = "Bearer $apiKey",
                request = request
            )

            val insight = response.choices.firstOrNull()?.message?.content
                ?: "Unable to generate insight at this time."

            Result.success(insight)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildPrompt(habits: List<Habit>, logs: Map<Int, List<HabitLog>>): String {
        val sb = StringBuilder()
        sb.appendLine("You are a friendly personal habit coach.")
        sb.appendLine("Here's the user's weekly log:")
        sb.appendLine()

        habits.forEach { habit ->
            val habitLogs = logs[habit.id] ?: emptyList()
            val weeklyStatus = buildWeeklyStatus(habitLogs)
            sb.appendLine("${habit.title}: $weeklyStatus")
        }

        sb.appendLine()
        sb.appendLine("Generate a 3-sentence motivational summary and 1 practical improvement tip.")
        sb.appendLine("Keep it concise, encouraging, and actionable.")

        return sb.toString()
    }

    private fun buildWeeklyStatus(logs: List<HabitLog>): String {
        // Get last 7 days
        val status = StringBuilder()
        val today = java.time.LocalDate.now()

        for (i in 6 downTo 0) {
            val date = today.minusDays(i.toLong())
            val log = logs.find { it.date == date }
            val emoji = when {
                log?.completed == true -> "✅"
                log?.completed == false -> "❌"
                else -> "⚪"
            }
            status.append(emoji)
        }

        return status.toString()
    }
}

