package com.mohammadpatrick.assistant.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mohammadpatrick.assistant.domain.model.MemoryItem
import com.mohammadpatrick.assistant.domain.model.ReminderItem

@Entity(tableName = "local_memory")
data class MemoryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val category: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isPinned: Boolean
) {
    fun toDomain() = MemoryItem(
        id = id,
        title = title,
        content = content,
        category = category,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isPinned = isPinned
    )

    companion object {
        fun fromDomain(item: MemoryItem) = MemoryEntity(
            id = item.id,
            title = item.title,
            content = item.content,
            category = item.category,
            createdAt = item.createdAt,
            updatedAt = item.updatedAt,
            isPinned = item.isPinned
        )
    }
}

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val triggerTimeMillis: Long,
    val isCompleted: Boolean,
    val isRepeating: Boolean,
    val repeatIntervalMinutes: Long,
    val createdAt: Long
) {
    fun toDomain() = ReminderItem(
        id = id,
        title = title,
        description = description,
        triggerTimeMillis = triggerTimeMillis,
        isCompleted = isCompleted,
        isRepeating = isRepeating,
        repeatIntervalMinutes = repeatIntervalMinutes,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(item: ReminderItem) = ReminderEntity(
            id = item.id,
            title = item.title,
            description = item.description,
            triggerTimeMillis = item.triggerTimeMillis,
            isCompleted = item.isCompleted,
            isRepeating = item.isRepeating,
            repeatIntervalMinutes = item.repeatIntervalMinutes,
            createdAt = item.createdAt
        )
    }
}
