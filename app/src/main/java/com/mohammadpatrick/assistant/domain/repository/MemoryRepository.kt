package com.mohammadpatrick.assistant.domain.repository

import com.mohammadpatrick.assistant.domain.model.MemoryItem
import kotlinx.coroutines.flow.Flow

interface MemoryRepository {
    fun getAllMemoryItems(): Flow<List<MemoryItem>>
    fun searchMemory(query: String): Flow<List<MemoryItem>>
    suspend fun insertMemoryItem(item: MemoryItem)
    suspend fun updateMemoryItem(item: MemoryItem)
    suspend fun deleteMemoryItem(id: String)
    suspend fun clearAllMemory()
    suspend fun exportMemoryAsJson(): String
}
