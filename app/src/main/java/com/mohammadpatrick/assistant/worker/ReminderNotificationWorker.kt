package com.mohammadpatrick.assistant.worker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mohammadpatrick.assistant.MainActivity
import com.mohammadpatrick.assistant.MohammadPatrickApp
import com.mohammadpatrick.assistant.R

class ReminderNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val reminderId = inputData.getString("REMINDER_ID") ?: return Result.failure()
        val title = inputData.getString("REMINDER_TITLE") ?: "یادآور محمد پاتریک"
        val desc = inputData.getString("REMINDER_DESC") ?: ""

        sendNotification(reminderId, title, desc)

        // Mark as completed in Room
        try {
            val app = context.applicationContext as MohammadPatrickApp
            app.reminderRepository.toggleCompleted(reminderId, true)
        } catch (e: Exception) {
            // Log or ignore
        }

        return Result.success()
    }

    private fun sendNotification(id: String, title: String, description: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_REMINDERS_SCREEN", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, MohammadPatrickApp.CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_assistant_logo)
            .setContentTitle("⏰ $title")
            .setContentText(description.ifBlank { "زمان یادآوری شما فرا رسیده است." })
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(id.hashCode(), notification)
    }
}
