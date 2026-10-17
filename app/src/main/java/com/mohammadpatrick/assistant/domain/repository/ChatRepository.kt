package com.mohammadpatrick.assistant.domain.repository

import com.mohammadpatrick.assistant.domain.model.ChatMessage
import com.mohammadpatrick.assistant.domain.model.Conversation
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getAllConversations(): Flow<List<Conversation>>
    fun getMessagesForConversation(conversationId: String): Flow<List<ChatMessage>>
    suspend fun createConversation(title: String): String
    suspend fun renameConversation(conversationId: String, newTitle: String)
    suspend fun deleteConversation(conversationId: String)
    suspend fun saveMessage(message: ChatMessage)
    suspend fun deleteMessage(messageId: String)
    suspend fun clearAllMessages(conversationId: String)
}
