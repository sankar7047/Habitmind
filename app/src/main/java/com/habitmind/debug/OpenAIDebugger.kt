package com.habitmind.debug

import com.habitmind.data.remote.Message
import com.habitmind.data.remote.OpenAIApiService
import com.habitmind.data.remote.OpenAIRequest
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Debug class to test OpenAI API connectivity and diagnose issues
 */
class OpenAIDebugger @Inject constructor(
    private val apiKey: String
) {
    
    private val debugClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val debugRetrofit = Retrofit.Builder()
        .baseUrl("https://api.openai.com/")
        .client(debugClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val debugApiService = debugRetrofit.create(OpenAIApiService::class.java)
    
    suspend fun runDiagnostics(): String {
        val results = StringBuilder()
        results.appendLine("=== OpenAI API Diagnostics ===")
        results.appendLine()
        
        // 1. Check API key format
        results.appendLine("1. API Key Validation:")
        results.appendLine("   Format: ${if (apiKey.startsWith("sk-")) "✅ Valid" else "❌ Invalid"}")
        results.appendLine("   Length: ${apiKey.length} characters")
        results.appendLine("   Preview: ${apiKey.take(10)}...")
        results.appendLine()
        
        // 2. Test different models
        val modelsToTest = listOf(
            "gpt-3.5-turbo",
            "gpt-4",
            "gpt-4-turbo-preview",
            "gpt-4-0613"
        )
        
        results.appendLine("2. Model Availability Tests:")
        for (model in modelsToTest) {
            try {
                val request = OpenAIRequest(
                    model = model,
                    messages = listOf(Message(role = "user", content = "Test")),
                    max_tokens = 5
                )
                
                val response = debugApiService.getAIResponse(
                    authorization = "Bearer $apiKey",
                    request = request
                )
                
                results.appendLine("   $model: ✅ Available")
            } catch (e: retrofit2.HttpException) {
                results.appendLine("   $model: ❌ HTTP ${e.code()} - ${e.message()}")
                try {
                    val errorBody = e.response()?.errorBody()?.string()
                    results.appendLine("     Error: $errorBody")
                } catch (ex: Exception) {
                    results.appendLine("     Error body unreadable")
                }
            } catch (e: Exception) {
                results.appendLine("   $model: ❌ ${e.javaClass.simpleName} - ${e.message}")
            }
        }
        
        return results.toString()
    }
    
    suspend fun testBasicConnection(): Result<String> {
        return try {
            val request = OpenAIRequest(
                model = "gpt-3.5-turbo",
                messages = listOf(
                    Message(role = "user", content = "Hello")
                ),
                max_tokens = 5
            )
            
            val response = debugApiService.getAIResponse(
                authorization = "Bearer $apiKey",
                request = request
            )
            
            val content = response.choices.firstOrNull()?.message?.content
            Result.success("Success: $content")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}