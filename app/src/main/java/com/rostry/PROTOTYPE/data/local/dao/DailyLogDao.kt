package com.rostry.prototype.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rostry.prototype.data.local.entity.DailyLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyLogDao {
    @Query("SELECT * FROM daily_logs WHERE logId = :logId")
    suspend fun getById(logId: Long): DailyLogEntity?

    @Query("SELECT * FROM daily_logs WHERE farmerId = :farmerId")
    fun getByFarmer(farmerId: Long): Flow<List<DailyLogEntity>>

    @Query("SELECT * FROM daily_logs WHERE farmerId = :farmerId AND logDate = :date")
    suspend fun getByDate(farmerId: Long, date: String): DailyLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(log: DailyLogEntity)
}
