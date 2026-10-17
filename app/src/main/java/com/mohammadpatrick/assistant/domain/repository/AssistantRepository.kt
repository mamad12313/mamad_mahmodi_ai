package com.mohammadpatrick.assistant.domain.repository

import com.mohammadpatrick.assistant.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

sealed class AssistantResult {
    data class Success(val replyText: String) : AssistantResult()
    data class StreamChunk(val chunk: String) : AssistantResult()
    data class ApiNotConfigured(val message: String = "برای دریافت پاسخ هوشمند، ابتدا API خود را متصل کنید.") : AssistantResult()
    data class Error(val errorMessage: String, val throwable: Throwable? = null) : AssistantResult()
    object Loading : AssistantResult()
}

interface AssistantRepository {
    suspend fun sendMessage(
        conversationId: String,
        userPrompt: String,
        attachedImageBase64: String? = null,
        attachedFileText: String? = null
    ): AssistantResult

    fun streamMessage(
        conversationId: String,
        userPrompt: String
    ): Flow<AssistantResult>

    suspend fun testApiConnection(): Result<Boolean>
}
