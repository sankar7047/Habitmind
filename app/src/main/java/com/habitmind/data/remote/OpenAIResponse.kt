package com.habitmind.data.remote

data class OpenAIResponse(
    val id: String? = null,
    val choices: List<Choice> = emptyList()
)

data class Choice(
    val message: Message? = null,
    val finish_reason: String? = null
)

