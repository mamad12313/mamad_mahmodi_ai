package com.mohammadpatrick.assistant.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.mohammadpatrick.assistant.MainActivity
import com.mohammadpatrick.assistant.MohammadPatrickApp
import com.mohammadpatrick.assistant.R
import com.mohammadpatrick.assistant.audio.VoiceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Android 14 Foreground Service with microphone type.
 * Runs in background to monitor wake word "سلام پاتریک" or standby for user triggers.
 * Includes clear stop action, notification controls, and handles Android 14 strict foreground service requirements.
 */
class WakeWordForegroundService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private var voiceManager: VoiceManager? = null

    companion object {
        const val ACTION_START = "ACTION_START_WAKE_WORD_SERVICE"
        const val ACTION_STOP = "ACTION_STOP_WAKE_WORD_SERVICE"
        const val ACTION_TRIGGER_VOICE = "ACTION_TRIGGER_VOICE_LISTENING"
        private const val NOTIFICATION_ID = 2001

        private val _isRunningFlow = MutableStateFlow(false)
        val isRunningFlow = _isRunningFlow.asStateFlow()

        fun startService(context: Context) {
            val intent = Intent(context, WakeWordForegroundService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, WakeWordForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        voiceManager = VoiceManager(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopForegroundService()
                return START_NOT_STICKY
            }
            ACTION_START -> {
                startForegroundWithNotification()
                _isRunningFlow.value = true
            }
            ACTION_TRIGGER_VOICE -> {
                voiceManager?.startListening()
            }
        }
        return START_STICKY
    }

    private fun startForegroundWithNotification() {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, WakeWordForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("OPEN_VOICE_SCREEN", true)
        }
        val triggerPendingIntent = PendingIntent.getActivity(
            this, 2, triggerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, MohammadPatrickApp.CHANNEL_VOICE_SERVICE)
            .setContentTitle("دستیار صوتی محمد پاتریک فعال است")
            .setContentText("آماده شنیدن «سلام پاتریک» یا زدن دکمه فرمان صوتی")
            .setSmallIcon(R.drawable.ic_assistant_logo)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .addAction(R.drawable.ic_assistant_logo, "صحبت فوری", triggerPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "توقف سرویس", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // Android 14 FOREGROUND_SERVICE_TYPE_MICROPHONE
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                )
            } else {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                )
            }
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun stopForegroundService() {
        _isRunningFlow.value = false
        voiceManager?.stopListening()
        voiceManager?.destroy()
        voiceManager = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        _isRunningFlow.value = false
        voiceManager?.destroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
