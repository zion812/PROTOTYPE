package com.rostry.prototype.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rostry.prototype.data.local.entity.FarmAssetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmAssetDao {
    @Query("SELECT * FROM farm_assets WHERE assetId = :assetId")
    suspend fun getById(assetId: Long): FarmAssetEntity?

    @Query("SELECT * FROM farm_assets WHERE farmerId = :farmerId")
    fun getByFarmer(farmerId: Long): Flow<List<FarmAssetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(asset: FarmAssetEntity)

    @Update
    suspend fun update(asset: FarmAssetEntity)
}
