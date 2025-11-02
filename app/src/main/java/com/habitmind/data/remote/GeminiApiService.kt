package com.habitmind.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApiService {
    @POST("v1/models/gemini-2.0-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
    
    // Alternative models for fallback  
    @POST("v1/models/gemini-2.5-flash-lite:generateContent")
    suspend fun generateContentFallback(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}