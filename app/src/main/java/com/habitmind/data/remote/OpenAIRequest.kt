package com.habitmind.data.remote

data class OpenAIRequest(
    val model: String = "gpt-4-turbo",
    val messages: List<Message>,
    val max_tokens: Int = 150
)

data class Message(
    val role: String,
    val content: String
)

