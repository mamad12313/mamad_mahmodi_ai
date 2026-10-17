package com.mohammadpatrick.assistant.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mohammadpatrick.assistant.MohammadPatrickApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver {
    constructor() : super()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            val app = context.applicationContext as? MohammadPatrickApp ?: return
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    app.reminderRepository.rescheduleAllPendingReminders()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
