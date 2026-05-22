package com.rostry.prototype.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.rostry.prototype.data.local.entity.OutboxEntity

@Dao
interface OutboxDao {
    @Query("SELECT * FROM outbox WHERE status = 'PENDING'")
    suspend fun getPending(): List<OutboxEntity>

    @Query("UPDATE outbox SET status = 'COMPLETED' WHERE outboxId = :outboxId")
    suspend fun markCompleted(outboxId: Long)

    @Insert
    suspend fun insert(outbox: OutboxEntity)
}
