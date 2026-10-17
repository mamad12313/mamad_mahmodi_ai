package com.mohammadpatrick.assistant.data.repository

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.mohammadpatrick.assistant.data.local.db.dao.ReminderDao
import com.mohammadpatrick.assistant.data.local.db.entity.ReminderEntity
import com.mohammadpatrick.assistant.domain.model.ReminderItem
import com.mohammadpatrick.assistant.domain.repository.ReminderRepository
import com.mohammadpatrick.assistant.worker.ReminderNotificationWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

class ReminderRepositoryImpl(
    private val reminderDao: ReminderDao,
    private val context: Context
) : ReminderRepository {

    private val workManager = WorkManager.getInstance(context)

    override fun getAllReminders(): Flow<List<ReminderItem>> {
        return reminderDao.getAllReminders().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun addReminder(reminder: ReminderItem) {
        reminderDao.insertReminder(ReminderEntity.fromDomain(reminder))
        scheduleWork(reminder)
    }

    override suspend fun updateReminder(reminder: ReminderItem) {
        reminderDao.updateReminder(ReminderEntity.fromDomain(reminder))
        if (!reminder.isCompleted) {
            scheduleWork(reminder)
        } else {
            cancelWork(reminder.id)
        }
    }

    override suspend fun deleteReminder(id: String) {
        reminderDao.deleteReminder(id)
        cancelWork(id)
    }

    override suspend fun toggleCompleted(id: String, completed: Boolean) {
        reminderDao.setCompleted(id, completed)
        if (completed) {
            cancelWork(id)
        } else {
            val item = reminderDao.getReminderById(id)
            if (item != null) {
                scheduleWork(item.toDomain())
            }
        }
    }

    override suspend fun rescheduleAllPendingReminders() {
        val pending = reminderDao.getPendingReminders()
        for (item in pending) {
            scheduleWork(item.toDomain())
        }
    }

    private fun scheduleWork(reminder: ReminderItem) {
        val delayMillis = reminder.triggerTimeMillis - System.currentTimeMillis()
        if (delayMillis <= 0) return

        val inputData = Data.Builder()
            .putString("REMINDER_ID", reminder.id)
            .putString("REMINDER_TITLE", reminder.title)
            .putString("REMINDER_DESC", reminder.description)
            .build()

        val request = OneTimeWorkRequestBuilder<ReminderNotificationWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag("reminder_${reminder.id}")
            .build()

        workManager.enqueueUniqueWork(
            "work_reminder_${reminder.id}",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    private fun cancelWork(id: String) {
        workManager.cancelUniqueWork("work_reminder_$id")
    }
}
