package com.rostry.prototype.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.rostry.prototype.data.local.entity.OutboxEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OutboxDao {
    @Query("SELECT * FROM outbox WHERE status = 'PENDING'")
    suspend fun getPending(): List<OutboxEntity>

    @Query("SELECT COUNT(*) FROM outbox WHERE status = 'PENDING'")
    fun observePendingCount(): Flow<Int>

    @Query("UPDATE outbox SET status = 'COMPLETED' WHERE outboxId = :outboxId")
    suspend fun markCompleted(outboxId: Long)

    @Query("SELECT * FROM outbox ORDER BY createdAt DESC")
    suspend fun getAll(): List<OutboxEntity>

    @Insert
    suspend fun insert(outbox: OutboxEntity)
}
