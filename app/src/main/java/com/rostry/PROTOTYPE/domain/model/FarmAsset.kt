package com.rostry.prototype.domain.model

import java.util.UUID

data class FarmAsset(
    val assetId: String = UUID.randomUUID().toString(),
    val farmerId: String,
    val name: String,
    val breed: String,
    val imageUrl: String? = null,
    val createdAt: Long,
    val dirty: Boolean = true
)
