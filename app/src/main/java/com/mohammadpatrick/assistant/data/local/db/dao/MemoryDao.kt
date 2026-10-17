package com.mohammadpatrick.assistant.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mohammadpatrick.assistant.data.local.db.entity.MemoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM local_memory ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllMemoryItems(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM local_memory WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchMemory(query: String): Flow<List<MemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(item: MemoryEntity)

    @Update
    suspend fun updateMemory(item: MemoryEntity)

    @Query("DELETE FROM local_memory WHERE id = :id")
    suspend fun deleteMemory(id: String)

    @Query("DELETE FROM local_memory")
    suspend fun clearAll()
}
