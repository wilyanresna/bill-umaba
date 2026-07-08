package com.pndnwngi.billumaba.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pndnwngi.billumaba.data.database.entities.ReceiptPatternEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiptPatternDao {

    @Query("SELECT * FROM receipt_patterns WHERE LOWER(restaurantName) = LOWER(:name) LIMIT 1")
    suspend fun findByName(name: String): ReceiptPatternEntity?

    @Query("SELECT * FROM receipt_patterns ORDER BY lastUsedAt DESC")
    fun observeAll(): Flow<List<ReceiptPatternEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(pattern: ReceiptPatternEntity): Long

    @Delete
    suspend fun delete(pattern: ReceiptPatternEntity)

    @Query("UPDATE receipt_patterns SET lastUsedAt = :ts, usageCount = usageCount + 1 WHERE id = :id")
    suspend fun touch(id: Long, ts: Long)

    @Query("SELECT receiptPhotoPath FROM visits WHERE receiptPhotoPath IS NOT NULL ORDER BY visitDate DESC LIMIT :limit")
    suspend fun getRecentPhotoPaths(limit: Int = 20): List<String>
}
