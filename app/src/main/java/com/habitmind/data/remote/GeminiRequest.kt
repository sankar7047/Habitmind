package com.habitmind.data.remote

import com.google.gson.annotations.SerializedName

data class GeminiRequest(
    @SerializedName("contents") val contents: List<GeminiContent>,
    @SerializedName("generationConfig") val generationConfig: GeminiGenerationConfig? = GeminiGenerationConfig()
)

data class GeminiContent(
    @SerializedName("parts") val parts: List<GeminiPart>
)

data class GeminiPart(
    @SerializedName("text") val text: String
)

data class GeminiGenerationConfig(
    @SerializedName("temperature") val temperature: Double = 0.7,
    @SerializedName("topK") val topK: Int = 1,
    @SerializedName("topP") val topP: Double = 1.0,
    @SerializedName("maxOutputTokens") val maxOutputTokens: Int = 200,
    @SerializedName("stopSequences") val stopSequences: List<String> = emptyList()
)