package com.example.data.model

import java.util.UUID

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}

data class ConversationMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: MessageRole,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val commandType: CommandType? = null,
    val actionSuccess: Boolean? = null,
    val isError: Boolean = false
)
