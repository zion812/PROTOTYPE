package com.rostry.prototype.domain.model

import java.util.UUID

data class DailyLog(
    val logId: String = UUID.randomUUID().toString(),
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
