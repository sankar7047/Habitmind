package com.habitmind.data.remote

import com.google.gson.annotations.SerializedName

data class OpenAIResponse(
    @SerializedName("id") val id: String? = null,
    @SerializedName("choices") val choices: List<Choice> = emptyList()
)

data class Choice(
    @SerializedName("message") val message: Message? = null,
    @SerializedName("finish_reason") val finish_reason: String? = null
)

