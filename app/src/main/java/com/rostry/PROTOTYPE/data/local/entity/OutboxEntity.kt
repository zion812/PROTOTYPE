package com.rostry.prototype.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "outbox",
    indices = [Index(value = ["status"])]
)
data class OutboxEntity(
    @PrimaryKey(autoGenerate = true) val outboxId: Long = 0,
    val entityType: String,
    val entityId: Long,
    val payloadJson: String,
    val status: String,
    val createdAt: Long
)
