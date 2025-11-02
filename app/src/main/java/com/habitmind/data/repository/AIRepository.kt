package com.habitmind.data.repository

import com.habitmind.data.model.Habit
import com.habitmind.data.model.HabitLog
import com.habitmind.data.remote.*
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Named

class AIRepository @Inject constructor(
    private val openAIApiService: OpenAIApiService,
    private val geminiApiService: GeminiApiService,
    @Named("openai_key") openAIApiKey: String,
    @Named("gemini_key") geminiApiKey: String
) {
    private val cleanOpenAIApiKey = openAIApiKey.trim()
    private val cleanGeminiApiKey = geminiApiKey.trim()
    init {
        // Validate API keys
        if (cleanGeminiApiKey.isNotBlank()) {
            println("AIRepository initialized with Gemini API key: ${cleanGeminiApiKey.take(10)}... (length: ${cleanGeminiApiKey.length})")
        }
        if (cleanOpenAIApiKey.isNotBlank() && cleanOpenAIApiKey.startsWith("sk-")) {
            println("AIRepository initialized with OpenAI API key: ${cleanOpenAIApiKey.take(10)}... (length: ${cleanOpenAIApiKey.length})")
        }
        
        if (cleanGeminiApiKey.isBlank() && cleanOpenAIApiKey.isBlank()) {
            throw IllegalArgumentException("No valid API keys found. Please check your api_key.properties file.")
        }
    }

    suspend fun testConnection(): Result<String> {
        val results = mutableListOf<String>()
        
        // Test Gemini
        if (cleanGeminiApiKey.isNotBlank()) {
            try {
                val geminiRequest = GeminiRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(
                                GeminiPart(text = "Say 'Hello, this is a Gemini test'")
                            )
                        )
                    ),
                    generationConfig = GeminiGenerationConfig(maxOutputTokens = 20)
                )
                
                println("Testing connection to Gemini 2.0 Flash API...")
                val geminiResponse = geminiApiService.generateContent(
                    apiKey = cleanGeminiApiKey,
                    request = geminiRequest
                )
                
                val geminiResult = geminiResponse.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                results.add("Gemini 2.0 Flash: ✅ $geminiResult")
            } catch (e: Exception) {
                results.add("Gemini 2.0 Flash: ❌ ${e.message}")
            }
        } else {
            results.add("Gemini: ⚠️ No API key configured")
        }
        
        // Test OpenAI
        if (cleanOpenAIApiKey.isNotBlank() && cleanOpenAIApiKey.startsWith("sk-")) {
            try {
                val openAIRequest = OpenAIRequest(
                    model = "gpt-3.5-turbo",
                    messages = listOf(
                        Message(role = "user", content = "Say 'Hello, this is an OpenAI test'")
                    ),
                    max_tokens = 10
                )
                
                println("Testing connection to OpenAI API...")
                val openAIResponse = openAIApiService.getAIResponse(
                    authorization = "Bearer $cleanOpenAIApiKey",
                    request = openAIRequest
                )
                
                val openAIResult = openAIResponse.choices.firstOrNull()?.message?.content
                results.add("OpenAI: ✅ $openAIResult")
            } catch (e: Exception) {
                results.add("OpenAI: ❌ ${e.message}")
            }
        } else {
            results.add("OpenAI: ⚠️ No valid API key configured")
        }
        
        return Result.success(results.joinToString("\n"))
    }
    suspend fun generateWeeklyInsight(
        habits: List<Habit>,
        logs: Map<Int, List<HabitLog>>
    ): Result<String> {
        val prompt = buildPrompt(habits, logs)
        
        // Try Gemini first if available
        if (cleanGeminiApiKey.isNotBlank()) {
            println("AIRepository: Attempting Gemini API call...")
            val geminiResult = tryGeminiAPI(prompt)
            if (geminiResult.isSuccess) {
                println("AIRepository: Gemini API call successful")
                return geminiResult
            } else {
                println("AIRepository: Gemini API failed: ${geminiResult.exceptionOrNull()?.message}")
            }
        }
        
        // Fallback to OpenAI if Gemini fails or is not available
        if (cleanOpenAIApiKey.isNotBlank() && cleanOpenAIApiKey.startsWith("sk-")) {
            println("AIRepository: Attempting OpenAI API call as fallback...")
            return tryOpenAIAPI(prompt)
        }
        
        return Result.failure(Exception("No available AI service. Both Gemini and OpenAI failed or are not configured."))
    }

    private suspend fun tryGeminiAPI(prompt: String): Result<String> {
        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(text = "You are a friendly personal habit coach. Provide encouraging, motivating, and practical advice.\n\n$prompt")
                    )
                )
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.7,
                maxOutputTokens = 200
            )
        )

        // Try the main gemini-2.0-flash model first (works better for text generation)
        try {
            println("AIRepository: Trying Gemini 2.0 Flash model...")
            val response = geminiApiService.generateContent(
                apiKey = cleanGeminiApiKey,
                request = request
            )

            val insight = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Unable to generate insight at this time."

            println("AIRepository: Gemini 2.0 Flash response received successfully")
            return Result.success(insight)
        } catch (e: HttpException) {
            println("AIRepository: Gemini 2.0 Flash failed with ${e.code()}, trying 2.5 Flash Lite...")
            
            // Try gemini-2.5-flash-lite as fallback
            try {
                val fallbackResponse = geminiApiService.generateContentFallback(
                    apiKey = cleanGeminiApiKey,
                    request = request
                )

                val insight = fallbackResponse.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "Unable to generate insight at this time."

                println("AIRepository: Gemini 2.5 Flash Lite response received successfully")
                return Result.success(insight)
            } catch (e2: HttpException) {
                val responseBody = try {
                    e2.response()?.errorBody()?.string()
                } catch (ex: Exception) {
                    "Unable to read error body"
                }
                
                println("AIRepository: Both Gemini models failed")
                println("AIRepository: 2.0 Flash error: ${e.code()}")
                println("AIRepository: 2.5 Flash Lite error: ${e2.code()}")
                println("AIRepository: 2.5 Flash Lite URL: ${e2.response()?.raw()?.request?.url}")
                println("AIRepository: 2.5 Flash Lite response: $responseBody")
                
                val errorMessage = when (e2.code()) {
                    400 -> "Bad request to Gemini API. Check request format. Error: $responseBody"
                    403 -> "Invalid Gemini API key or quota exceeded. Error: $responseBody"
                    404 -> "Gemini API endpoint not found. URL: ${e2.response()?.raw()?.request?.url}. Error: $responseBody"
                    429 -> "Gemini rate limit exceeded. Error: $responseBody"
                    else -> "Gemini HTTP error ${e2.code()}: ${e2.message()}. Response: $responseBody"
                }
                return Result.failure(Exception(errorMessage))
            } catch (e2: Exception) {
                return Result.failure(Exception("Gemini API error: ${e2.message}"))
            }
        } catch (e: Exception) {
            println("AIRepository: Gemini API exception: ${e.javaClass.simpleName} - ${e.message}")
            e.printStackTrace()
            return Result.failure(Exception("Gemini API error: ${e.message}"))
        }
    }

    private suspend fun tryOpenAIAPI(prompt: String): Result<String> {
        return try {
            val request = OpenAIRequest(
                model = "gpt-3.5-turbo",
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
                authorization = "Bearer $cleanOpenAIApiKey",
                request = request
            )

            val insight = response.choices.firstOrNull()?.message?.content
                ?: "Unable to generate insight at this time."

            Result.success(insight)
        } catch (e: HttpException) {
            val responseBody = try {
                e.response()?.errorBody()?.string()
            } catch (ex: Exception) {
                "Unable to read error body"
            }
            
            val errorMessage = when (e.code()) {
                401 -> "Invalid OpenAI API key. Error: $responseBody"
                404 -> "OpenAI API endpoint not found. Error: $responseBody"
                429 -> "OpenAI rate limit exceeded. Error: $responseBody"
                500, 502, 503 -> "OpenAI service temporarily unavailable. Error: $responseBody"
                else -> "OpenAI HTTP error ${e.code()}: ${e.message()}. Response: $responseBody"
            }
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Result.failure(Exception("OpenAI API error: ${e.message}"))
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

