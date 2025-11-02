package com.habitmind.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Header

interface OpenAIApiService {
    @POST("v1/chat/completions")
    suspend fun getAIResponse(
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: OpenAIRequest
    ): OpenAIResponse
}

