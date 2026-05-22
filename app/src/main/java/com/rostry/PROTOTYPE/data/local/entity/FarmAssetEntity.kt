package com.rostry.prototype.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "farm_assets",
    indices = [Index(value = ["farmerId"])]
)
data class FarmAssetEntity(
    @PrimaryKey val assetId: Long,
    val farmerId: Long,
    val name: String,
    val breed: String,
    val imageUrl: String,
    val createdAt: Long,
    val dirty: Boolean
)
