package com.mohammadpatrick.assistant.domain.repository

import com.mohammadpatrick.assistant.domain.model.ReminderItem
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun getAllReminders(): Flow<List<ReminderItem>>
    suspend fun addReminder(reminder: ReminderItem)
    suspend fun updateReminder(reminder: ReminderItem)
    suspend fun deleteReminder(id: String)
    suspend fun toggleCompleted(id: String, completed: Boolean)
    suspend fun rescheduleAllPendingReminders()
}
