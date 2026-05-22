package com.rostry.prototype.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.rostry.prototype.data.local.entity.OutboxEntity

@Dao
interface OutboxDao {
    @Insert
    suspend fun insert(outbox: OutboxEntity)

    @Query("SELECT * FROM outbox WHERE status = 'PENDING'")
    suspend fun getPending(): List<OutboxEntity>

    @Query("SELECT * FROM outbox WHERE status = 'PENDING' OR status = 'FAILED'")
    suspend fun getPendingOrFailed(): List<OutboxEntity>

    @Query("UPDATE outbox SET status = 'COMPLETED' WHERE outboxId = :outboxId")
    suspend fun markCompleted(outboxId: String)

    @Query("UPDATE outbox SET status = 'FAILED' WHERE outboxId = :outboxId")
    suspend fun markFailed(outboxId: String)

    @Query("UPDATE outbox SET status = 'PENDING' WHERE outboxId = :outboxId")
    suspend fun markPending(outboxId: String)
}
