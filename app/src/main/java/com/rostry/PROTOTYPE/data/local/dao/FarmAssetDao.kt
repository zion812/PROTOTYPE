package com.rostry.prototype.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.rostry.prototype.data.local.entity.FarmAssetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmAssetDao {
    @Upsert
    suspend fun upsert(asset: FarmAssetEntity)

    @Query("DELETE FROM farm_assets WHERE assetId = :assetId")
    suspend fun delete(assetId: String)

    @Query("SELECT * FROM farm_assets WHERE farmerId = :farmerId")
    fun getByFarmer(farmerId: String): Flow<List<FarmAssetEntity>>

    @Query("SELECT * FROM farm_assets WHERE dirty = 1")
    suspend fun getDirty(): List<FarmAssetEntity>
}
