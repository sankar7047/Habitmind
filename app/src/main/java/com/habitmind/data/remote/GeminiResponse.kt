package com.habitmind.data.remote

import com.google.gson.annotations.SerializedName

data class GeminiResponse(
    @SerializedName("candidates") val candidates: List<GeminiCandidate> = emptyList(),
    @SerializedName("promptFeedback") val promptFeedback: GeminiPromptFeedback? = null
)

data class GeminiCandidate(
    @SerializedName("content") val content: GeminiContent? = null,
    @SerializedName("finishReason") val finishReason: String? = null,
    @SerializedName("index") val index: Int? = null,
    @SerializedName("safetyRatings") val safetyRatings: List<GeminiSafetyRating> = emptyList()
)

data class GeminiPromptFeedback(
    @SerializedName("blockReason") val blockReason: String? = null,
    @SerializedName("safetyRatings") val safetyRatings: List<GeminiSafetyRating> = emptyList()
)

data class GeminiSafetyRating(
    @SerializedName("category") val category: String? = null,
    @SerializedName("probability") val probability: String? = null
)