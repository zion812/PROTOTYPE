package com.rostry.prototype.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.rostry.prototype.data.local.entity.DailyLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyLogDao {
    @Insert
    suspend fun insert(log: DailyLogEntity)

    @Upsert
    suspend fun upsert(log: DailyLogEntity)

    @Query("SELECT * FROM daily_logs WHERE farmerId = :farmerId")
    fun getByFarmer(farmerId: String): Flow<List<DailyLogEntity>>

    @Query("SELECT * FROM daily_logs WHERE farmerId = :farmerId AND logDate >= :dayStart AND logDate < :dayEnd LIMIT 1")
    fun getForDate(farmerId: String, dayStart: Long, dayEnd: Long): Flow<DailyLogEntity?>

    @Query("SELECT * FROM daily_logs WHERE dirty = 1")
    suspend fun getDirty(): List<DailyLogEntity>
}
