package com.mohammadpatrick.assistant

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.room.Room
import com.mohammadpatrick.assistant.data.local.db.AppDatabase
import com.mohammadpatrick.assistant.data.local.pref.PreferencesManager
import com.mohammadpatrick.assistant.data.remote.ApiClientProvider
import com.mohammadpatrick.assistant.data.repository.AssistantRepositoryImpl
import com.mohammadpatrick.assistant.data.repository.ChatRepositoryImpl
import com.mohammadpatrick.assistant.data.repository.MemoryRepositoryImpl
import com.mohammadpatrick.assistant.data.repository.ReminderRepositoryImpl
import com.mohammadpatrick.assistant.data.repository.SettingsRepositoryImpl
import com.mohammadpatrick.assistant.data.security.SecurityManager
import com.mohammadpatrick.assistant.domain.repository.AssistantRepository
import com.mohammadpatrick.assistant.domain.repository.ChatRepository
import com.mohammadpatrick.assistant.domain.repository.MemoryRepository
import com.mohammadpatrick.assistant.domain.repository.ReminderRepository
import com.mohammadpatrick.assistant.domain.repository.SettingsRepository
import com.mohammadpatrick.assistant.system.SamsungA23Optimizer
import com.mohammadpatrick.assistant.system.SystemActionsManager

/**
 * Main Application Class for Mohammad Patrick Assistant.
 * Configures Dependency Container, Room Database, Notification Channels, and Samsung A23 optimization.
 */
class MohammadPatrickApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var preferencesManager: PreferencesManager
        private set

    lateinit var securityManager: SecurityManager
        private set

    lateinit var systemActionsManager: SystemActionsManager
        private set

    lateinit var samsungA23Optimizer: SamsungA23Optimizer
        private set

    lateinit var apiClientProvider: ApiClientProvider
        private set

    // Repositories
    lateinit var chatRepository: ChatRepository
        private set
    lateinit var memoryRepository: MemoryRepository
        private set
    lateinit var reminderRepository: ReminderRepository
        private set
    lateinit var settingsRepository: SettingsRepository
        private set
    lateinit var assistantRepository: AssistantRepository
        private set

    companion object {
        const val CHANNEL_VOICE_SERVICE = "channel_voice_service"
        const val CHANNEL_REMINDERS = "channel_reminders"
        lateinit var instance: MohammadPatrickApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize local Room DB
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "mohammad_patrick.db"
        ).fallbackToDestructiveMigration().build()

        preferencesManager = PreferencesManager(applicationContext)
        securityManager = SecurityManager(applicationContext)
        systemActionsManager = SystemActionsManager(applicationContext)
        samsungA23Optimizer = SamsungA23Optimizer(applicationContext)
        apiClientProvider = ApiClientProvider(preferencesManager)

        // Initialize Repositories
        chatRepository = ChatRepositoryImpl(database.chatDao())
        memoryRepository = MemoryRepositoryImpl(database.memoryDao())
        reminderRepository = ReminderRepositoryImpl(database.reminderDao(), applicationContext)
        settingsRepository = SettingsRepositoryImpl(preferencesManager)
        assistantRepository = AssistantRepositoryImpl(apiClientProvider, preferencesManager)

        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Voice foreground service channel (low importance to avoid annoying beeps while listening)
            val voiceChannel = NotificationChannel(
                CHANNEL_VOICE_SERVICE,
                "سرویس صوتی محمد پاتریک",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "نمایش وضعیت شنیدن و پردازش عبارت «سلام پاتریک» در پس‌زمینه"
                setShowBadge(false)
            }

            // Reminders and alerts channel (high importance)
            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "یادآورهای محمد پاتریک",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "اعلان زمان‌بندی‌شده برای هشدارها و کارهای کاربر"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(voiceChannel)
            notificationManager.createNotificationChannel(reminderChannel)
        }
    }
}
