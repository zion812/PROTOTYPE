package com.rostry.prototype.domain.model

data class DailyLog(
    val logId: Long,
    val farmerId: Long,
    val assetId: Long?,
    val logDate: String,
    val feedKg: Double,
    val mortalityCount: Int,
    val photoUrl: String,
    val notes: String,
    val dirty: Boolean
)
