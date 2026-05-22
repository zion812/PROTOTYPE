package com.rostry.prototype.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "farm_assets",
    indices = [Index(value = ["farmerId"])]
)
data class FarmAssetEntity(
    @PrimaryKey val assetId: String = UUID.randomUUID().toString(),
    val farmerId: String,
    val name: String,
    val breed: String,
    val imageUrl: String? = null,
    val createdAt: Long,
    val dirty: Boolean = true
)
