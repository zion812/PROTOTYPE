package com.rostry.prototype.data.repo

import com.google.gson.Gson
import com.rostry.prototype.data.local.dao.DailyLogDao
import com.rostry.prototype.data.local.dao.FarmAssetDao
import com.rostry.prototype.data.local.dao.OutboxDao
import com.rostry.prototype.data.local.entity.DailyLogEntity
import com.rostry.prototype.data.local.entity.FarmAssetEntity
import com.rostry.prototype.data.local.entity.OutboxEntity
import com.rostry.prototype.domain.model.ENTITY_TYPE_DAILY_LOG
import com.rostry.prototype.domain.model.ENTITY_TYPE_FARM_ASSET
import com.rostry.prototype.domain.model.STATUS_PENDING
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FarmRepository @Inject constructor(
    private val farmAssetDao: FarmAssetDao,
    private val dailyLogDao: DailyLogDao,
    private val outboxDao: OutboxDao,
    private val gson: Gson
) {
    suspend fun createFarmAsset(asset: FarmAssetEntity) {
        farmAssetDao.upsert(asset)
        outboxDao.insert(
            OutboxEntity(
                entityType = ENTITY_TYPE_FARM_ASSET,
                entityId = asset.assetId,
                payloadJson = gson.toJson(asset),
                status = STATUS_PENDING,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun createDailyLog(log: DailyLogEntity) {
        dailyLogDao.upsert(log)
        outboxDao.insert(
            OutboxEntity(
                entityType = ENTITY_TYPE_DAILY_LOG,
                entityId = log.logId,
                payloadJson = gson.toJson(log),
                status = STATUS_PENDING,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    fun getAssets(farmerId: String): Flow<List<FarmAssetEntity>> =
        farmAssetDao.getByFarmer(farmerId)

    fun getTodayLog(farmerId: String): Flow<DailyLogEntity?> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dayStart = cal.timeInMillis
        val dayEnd = dayStart + 86_400_000L
        return dailyLogDao.getForDate(farmerId, dayStart, dayEnd)
    }
}
