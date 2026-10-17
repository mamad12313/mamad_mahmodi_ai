package com.mohammadpatrick.assistant.domain.model

data class ReminderItem(
    val id: String,
    val title: String,
    val description: String = "",
    val triggerTimeMillis: Long,
    val isCompleted: Boolean = false,
    val isRepeating: Boolean = false,
    val repeatIntervalMinutes: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)
