package com.mohammadpatrick.assistant.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mohammadpatrick.assistant.domain.model.ChatMessage
import com.mohammadpatrick.assistant.domain.model.MessageSender
import com.mohammadpatrick.assistant.domain.model.MessageStatus

@Entity(
    tableName = "conversations"
)
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val lastMessagePreview: String = ""
)

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["conversationId"])]
)
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val text: String,
    val sender: String,
    val timestamp: Long,
    val status: String,
    val attachedImagePath: String?,
    val attachedFilePath: String?,
    val attachedFileName: String?,
    val attachedFileSize: Long?
) {
    fun toDomain(): ChatMessage {
        return ChatMessage(
            id = id,
            conversationId = conversationId,
            text = text,
            sender = try { MessageSender.valueOf(sender) } catch (e: Exception) { MessageSender.ASSISTANT },
            timestamp = timestamp,
            status = try { MessageStatus.valueOf(status) } catch (e: Exception) { MessageStatus.SENT },
            attachedImagePath = attachedImagePath,
            attachedFilePath = attachedFilePath,
            attachedFileName = attachedFileName,
            attachedFileSize = attachedFileSize
        )
    }

    companion object {
        fun fromDomain(msg: ChatMessage): ChatMessageEntity {
            return ChatMessageEntity(
                id = msg.id,
                conversationId = msg.conversationId,
                text = msg.text,
                sender = msg.sender.name,
                timestamp = msg.timestamp,
                status = msg.status.name,
                attachedImagePath = msg.attachedImagePath,
                attachedFilePath = msg.attachedFilePath,
                attachedFileName = msg.attachedFileName,
                attachedFileSize = msg.attachedFileSize
            )
        }
    }
}
