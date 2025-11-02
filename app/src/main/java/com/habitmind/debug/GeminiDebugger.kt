package com.habitmind.debug

import com.habitmind.data.remote.GeminiApiService
import com.habitmind.data.remote.GeminiContent
import com.habitmind.data.remote.GeminiGenerationConfig
import com.habitmind.data.remote.GeminiPart
import com.habitmind.data.remote.GeminiRequest
import javax.inject.Inject

/**
 * Debug class to test Gemini API connectivity
 */
class GeminiDebugger @Inject constructor(
    private val geminiApiService: GeminiApiService,
    private val apiKey: String
) {
    
    suspend fun testGeminiConnection(): Result<String> {
        return try {
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(
                            GeminiPart(text = "Hello, please respond with 'Gemini API is working!'")
                        )
                    )
                ),
                generationConfig = GeminiGenerationConfig(
                    maxOutputTokens = 20,
                    temperature = 0.1
                )
            )
            
            println("GeminiDebugger: Testing connection with API key: ${apiKey.take(10)}...")
            
            val response = geminiApiService.generateContent(
                apiKey = apiKey,
                request = request
            )
            
            val content = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "No response content"
                
            println("GeminiDebugger: Response received: $content")
            Result.success("✅ Gemini API Test Successful: $content")
            
        } catch (e: retrofit2.HttpException) {
            val errorBody = try {
                e.response()?.errorBody()?.string()
            } catch (ex: Exception) {
                "Unable to read error body"
            }
            
            println("GeminiDebugger: HTTP Error ${e.code()}: $errorBody")
            Result.failure(Exception("❌ Gemini HTTP ${e.code()}: $errorBody"))
            
        } catch (e: Exception) {
            println("GeminiDebugger: Exception: ${e.message}")
            e.printStackTrace()
            Result.failure(Exception("❌ Gemini Error: ${e.message}"))
        }
    }
    
    suspend fun testFullFlow(): String {
        val results = StringBuilder()
        results.appendLine("=== Gemini API Full Test ===")
        results.appendLine()
        
        // Basic connection test
        results.appendLine("1. Basic Connection Test:")
        val basicTest = testGeminiConnection()
        results.appendLine("   ${basicTest.getOrElse { it.message }}")
        results.appendLine()
        
        // Test with habit coaching prompt
        results.appendLine("2. Habit Coaching Test:")
        try {
            val coachingRequest = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(
                            GeminiPart(text = """
                                You are a friendly personal habit coach. Provide encouraging, motivating, and practical advice.

                                Here's the user's weekly log:

                                Morning Exercise: ✅✅❌✅✅✅❌
                                Read 30 minutes: ✅❌✅✅❌✅✅

                                Generate a 3-sentence motivational summary and 1 practical improvement tip.
                                Keep it concise, encouraging, and actionable.
                            """.trimIndent())
                        )
                    )
                ),
                generationConfig = GeminiGenerationConfig(
                    maxOutputTokens = 200,
                    temperature = 0.7
                )
            )
            
            val response = geminiApiService.generateContent(
                apiKey = apiKey,
                request = coachingRequest
            )
            
            val insight = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "No coaching response"
                
            results.appendLine("   ✅ Coaching Response: $insight")
            
        } catch (e: Exception) {
            results.appendLine("   ❌ Coaching Test Failed: ${e.message}")
        }
        
        return results.toString()
    }
}