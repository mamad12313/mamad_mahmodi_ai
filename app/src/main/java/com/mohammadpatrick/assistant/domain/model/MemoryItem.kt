package com.mohammadpatrick.assistant.domain.model

data class MemoryItem(
    val id: String,
    val title: String,
    val content: String,
    val category: String = "عمومی",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)
