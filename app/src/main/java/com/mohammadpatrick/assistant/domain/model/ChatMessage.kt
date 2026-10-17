package com.mohammadpatrick.assistant.domain.model

enum class MessageSender {
    USER,
    ASSISTANT,
    SYSTEM
}

enum class MessageStatus {
    SENT,
    DELIVERED,
    FAILED,
    PENDING_API
}

data class ChatMessage(
    val id: String,
    val conversationId: String,
    val text: String,
    val sender: MessageSender,
    val timestamp: Long,
    val status: MessageStatus = MessageStatus.SENT,
    val attachedImagePath: String? = null,
    val attachedFilePath: String? = null,
    val attachedFileName: String? = null,
    val attachedFileSize: Long? = null
)
