package com.mohammadpatrick.assistant.data.repository

import com.mohammadpatrick.assistant.data.local.db.dao.ChatDao
import com.mohammadpatrick.assistant.data.local.db.entity.ChatMessageEntity
import com.mohammadpatrick.assistant.data.local.db.entity.ConversationEntity
import com.mohammadpatrick.assistant.domain.model.ChatMessage
import com.mohammadpatrick.assistant.domain.model.Conversation
import com.mohammadpatrick.assistant.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class ChatRepositoryImpl(private val chatDao: ChatDao) : ChatRepository {

    override fun getAllConversations(): Flow<List<Conversation>> {
        return chatDao.getAllConversations().map { list ->
            list.map {
                Conversation(
                    id = it.id,
                    title = it.title,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                    lastMessagePreview = it.lastMessagePreview
                )
            }
        }
    }

    override fun getMessagesForConversation(conversationId: String): Flow<List<ChatMessage>> {
        return chatDao.getMessagesForConversation(conversationId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun createConversation(title: String): String {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val entity = ConversationEntity(
            id = id,
            title = title.ifBlank { "گفتگوی جدید" },
            createdAt = now,
            updatedAt = now,
            lastMessagePreview = ""
        )
        chatDao.insertConversation(entity)
        return id
    }

    override suspend fun renameConversation(conversationId: String, newTitle: String) {
        chatDao.updateConversationTitle(conversationId, newTitle)
    }

    override suspend fun deleteConversation(conversationId: String) {
        chatDao.deleteConversation(conversationId)
    }

    override suspend fun saveMessage(message: ChatMessage) {
        chatDao.insertMessage(ChatMessageEntity.fromDomain(message))
        chatDao.updateLastMessage(message.conversationId, message.text.take(60))
    }

    override suspend fun deleteMessage(messageId: String) {
        chatDao.deleteMessage(messageId)
    }

    override suspend fun clearAllMessages(conversationId: String) {
        chatDao.deleteAllMessagesForConversation(conversationId)
    }
}
