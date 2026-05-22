package com.rostry.prototype.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "outbox",
    indices = [Index(value = ["status"])]
)
data class OutboxEntity(
    @PrimaryKey val outboxId: String = UUID.randomUUID().toString(),
    val entityType: String,
    val entityId: String,
    val payloadJson: String,
    val status: String = "PENDING",
    val createdAt: Long
)
