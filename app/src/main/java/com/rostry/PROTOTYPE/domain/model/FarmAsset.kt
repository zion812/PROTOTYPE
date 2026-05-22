package com.rostry.prototype.domain.model

data class FarmAsset(
    val assetId: Long,
    val farmerId: Long,
    val name: String,
    val breed: String,
    val imageUrl: String,
    val createdAt: Long,
    val dirty: Boolean
)
