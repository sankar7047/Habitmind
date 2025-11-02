package com.habitmind.data.remote

import com.google.gson.annotations.SerializedName

data class OpenAIRequest(
    @SerializedName("model") val model: String = "gpt-3.5-turbo",
    @SerializedName("messages") val messages: List<Message>,
    @SerializedName("max_tokens") val max_tokens: Int = 150
)

data class Message(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String
)

