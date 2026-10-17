package com.mohammadpatrick.assistant.data.repository

import com.mohammadpatrick.assistant.data.local.db.dao.MemoryDao
import com.mohammadpatrick.assistant.data.local.db.entity.MemoryEntity
import com.mohammadpatrick.assistant.domain.model.MemoryItem
import com.mohammadpatrick.assistant.domain.repository.MemoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class MemoryRepositoryImpl(private val memoryDao: MemoryDao) : MemoryRepository {

    override fun getAllMemoryItems(): Flow<List<MemoryItem>> {
        return memoryDao.getAllMemoryItems().map { list -> list.map { it.toDomain() } }
    }

    override fun searchMemory(query: String): Flow<List<MemoryItem>> {
        return memoryDao.searchMemory(query).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertMemoryItem(item: MemoryItem) {
        memoryDao.insertMemory(MemoryEntity.fromDomain(item))
    }

    override suspend fun updateMemoryItem(item: MemoryItem) {
        memoryDao.updateMemory(MemoryEntity.fromDomain(item))
    }

    override suspend fun deleteMemoryItem(id: String) {
        memoryDao.deleteMemory(id)
    }

    override suspend fun clearAllMemory() {
        memoryDao.clearAll()
    }

    override suspend fun exportMemoryAsJson(): String {
        val items = memoryDao.getAllMemoryItems().first()
        val jsonArray = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("content", item.content)
                put("category", item.category)
                put("createdAt", item.createdAt)
                put("updatedAt", item.updatedAt)
            }
            jsonArray.put(obj)
        }
        return jsonArray.toString(2)
    }
}
