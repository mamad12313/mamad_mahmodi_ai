package com.mohammadpatrick.assistant.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mohammadpatrick.assistant.data.local.db.dao.ChatDao
import com.mohammadpatrick.assistant.data.local.db.dao.MemoryDao
import com.mohammadpatrick.assistant.data.local.db.dao.ReminderDao
import com.mohammadpatrick.assistant.data.local.db.entity.ChatMessageEntity
import com.mohammadpatrick.assistant.data.local.db.entity.ConversationEntity
import com.mohammadpatrick.assistant.data.local.db.entity.MemoryEntity
import com.mohammadpatrick.assistant.data.local.db.entity.ReminderEntity

@Database(
    entities = [
        ConversationEntity::class,
        ChatMessageEntity::class,
        MemoryEntity::class,
        ReminderEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun memoryDao(): MemoryDao
    abstract fun reminderDao(): ReminderDao
}
