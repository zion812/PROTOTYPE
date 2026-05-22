package com.rostry.prototype.data.repo

import com.google.gson.Gson
import com.rostry.prototype.data.local.dao.DailyLogDao
import com.rostry.prototype.data.local.dao.FarmAssetDao
import com.rostry.prototype.data.local.dao.OutboxDao
import com.rostry.prototype.data.local.entity.DailyLogEntity
import com.rostry.prototype.data.local.entity.FarmAssetEntity
import com.rostry.prototype.data.local.entity.OutboxEntity
import com.rostry.prototype.domain.model.DailyLog
import com.rostry.prototype.domain.model.FarmAsset
import com.rostry.prototype.domain.model.STATUS_PENDING
import com.rostry.prototype.domain.model.ENTITY_TYPE_FARM_ASSET
import com.rostry.prototype.domain.model.ENTITY_TYPE_DAILY_LOG
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FarmRepository @Inject constructor(
    private val farmAssetDao: FarmAssetDao,
    private val dailyLogDao: DailyLogDao,
    private val outboxDao: OutboxDao,
    private val gson: Gson
) {
    suspend fun createAsset(asset: FarmAsset): Result<String> = runCatching {
        val entity = asset.toEntity().copy(dirty = true)
        farmAssetDao.upsert(entity)

        val outbox = OutboxEntity(
            entityType = ENTITY_TYPE_FARM_ASSET,
            entityId = entity.assetId,
            payloadJson = gson.toJson(asset),
            status = STATUS_PENDING,
            createdAt = System.currentTimeMillis()
        )
        outboxDao.insert(outbox)

        entity.assetId.toString()
    }

    fun getAssets(farmerId: Long): Flow<List<FarmAsset>> =
        farmAssetDao.getByFarmer(farmerId).map { list -> list.map { it.toDomain() } }

    suspend fun createDailyLog(log: DailyLog): Result<String> = runCatching {
        val entity = log.toEntity().copy(dirty = true)
        dailyLogDao.upsert(entity)

        val outbox = OutboxEntity(
            entityType = ENTITY_TYPE_DAILY_LOG,
            entityId = entity.logId,
            payloadJson = gson.toJson(log),
            status = STATUS_PENDING,
            createdAt = System.currentTimeMillis()
        )
        outboxDao.insert(outbox)

        entity.logId.toString()
    }

    fun getDailyLogs(farmerId: Long): Flow<List<DailyLog>> =
        dailyLogDao.getByFarmer(farmerId).map { list -> list.map { it.toDomain() } }

    suspend fun getTodayLog(farmerId: Long): DailyLog? {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        return dailyLogDao.getByDate(farmerId, today)?.toDomain()
    }

    private fun FarmAsset.toEntity() = FarmAssetEntity(
        assetId = assetId,
        farmerId = farmerId,
        name = name,
        breed = breed,
        imageUrl = imageUrl,
        createdAt = createdAt,
        dirty = dirty
    )

    private fun FarmAssetEntity.toDomain() = FarmAsset(
        assetId = assetId,
        farmerId = farmerId,
        name = name,
        breed = breed,
        imageUrl = imageUrl,
        createdAt = createdAt,
        dirty = dirty
    )

    private fun DailyLog.toEntity() = DailyLogEntity(
        logId = logId,
        farmerId = farmerId,
        assetId = assetId,
        logDate = logDate,
        feedKg = feedKg,
        mortalityCount = mortalityCount,
        photoUrl = photoUrl,
        notes = notes,
        dirty = dirty
    )

    private fun DailyLogEntity.toDomain() = DailyLog(
        logId = logId,
        farmerId = farmerId,
        assetId = assetId,
        logDate = logDate,
        feedKg = feedKg,
        mortalityCount = mortalityCount,
        photoUrl = photoUrl,
        notes = notes,
        dirty = dirty
    )
}
