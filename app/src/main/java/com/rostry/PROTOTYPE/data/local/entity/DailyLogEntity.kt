package com.rostry.prototype.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_logs",
    indices = [
        Index(value = ["farmerId"]),
        Index(value = ["logDate"]),
        Index(value = ["farmerId", "logDate"])
    ]
)
data class DailyLogEntity(
    @PrimaryKey val logId: Long,
    val farmerId: Long,
    val assetId: Long?,
    val logDate: String,
    val feedKg: Double,
    val mortalityCount: Int,
    val photoUrl: String,
    val notes: String,
    val dirty: Boolean
)
