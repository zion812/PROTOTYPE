package com.rostry.prototype.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "daily_logs",
    indices = [
        Index(value = ["farmerId"]),
        Index(value = ["logDate"]),
        Index(value = ["farmerId", "logDate"])
    ]
)
data class DailyLogEntity(
    @PrimaryKey val logId: String = UUID.randomUUID().toString(),
    val farmerId: String,
    val assetId: String? = null,
    val logDate: Long,
    val feedKg: Double? = null,
    val mortalityCount: Int = 0,
    val photoUrl: String? = null,
    val notes: String? = null,
    val createdAt: Long,
    val dirty: Boolean = true
)
