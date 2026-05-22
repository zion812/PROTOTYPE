package com.rostry.prototype.sync

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.rostry.prototype.BuildConfig
import com.rostry.prototype.data.local.dao.DailyLogDao
import com.rostry.prototype.data.local.dao.FarmAssetDao
import com.rostry.prototype.data.local.dao.OutboxDao
import com.rostry.prototype.data.local.entity.DailyLogEntity
import com.rostry.prototype.data.local.entity.FarmAssetEntity
import com.rostry.prototype.data.local.entity.OutboxEntity
import com.rostry.prototype.domain.model.ENTITY_TYPE_DAILY_LOG
import com.rostry.prototype.domain.model.ENTITY_TYPE_FARM_ASSET
import com.rostry.prototype.telegram.TelegramApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

sealed interface SyncState {
    data object Idle : SyncState
    data object Syncing : SyncState
    data class Done(val count: Int) : SyncState
    data class Error(val msg: String) : SyncState
}

@Singleton
class SyncManager @Inject constructor(
    private val outboxDao: OutboxDao,
    private val farmAssetDao: FarmAssetDao,
    private val dailyLogDao: DailyLogDao,
    private val telegramApi: TelegramApi,
    private val firestore: FirebaseFirestore,
    private val gson: Gson
) {
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    suspend fun syncAll() {
        _syncState.value = SyncState.Syncing
        val pendingItems = outboxDao.getPending()
        var successCount = 0
        for (item in pendingItems) {
            try {
                when (item.entityType) {
                    ENTITY_TYPE_FARM_ASSET -> processFarmAsset(item)
                    ENTITY_TYPE_DAILY_LOG -> processDailyLog(item)
                }
                successCount++
            } catch (e: Exception) {
                Log.e(TAG, "Sync failed for ${item.entityType} ${item.entityId}", e)
                outboxDao.markFailed(item.outboxId)
            }
        }
        _syncState.value = SyncState.Done(successCount)
    }

    private suspend fun processFarmAsset(item: OutboxEntity) {
        val asset = gson.fromJson(item.payloadJson, FarmAssetEntity::class.java)
        var imageUrl = asset.imageUrl ?: ""
        if (imageUrl.startsWith("file://")) {
            val file = File(imageUrl.removePrefix("file://"))
            try {
                val tgRef = telegramApi.uploadPhoto(BuildConfig.TELEGRAM_CHANNEL_ID, file).getOrThrow()
                imageUrl = tgRef
            } catch (e: Exception) {
                Log.w(TAG, "Telegram upload failed for ${asset.assetId}, pushing without image", e)
                imageUrl = ""
            }
        }
        val updated = asset.copy(imageUrl = imageUrl.ifEmpty { null }, dirty = false)
        try {
            firestore.collection("farm_assets")
                .document(asset.assetId)
                .set(updated)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Firestore push failed for asset ${asset.assetId}", e)
            outboxDao.markFailed(item.outboxId)
            return
        }
        farmAssetDao.upsert(updated)
        outboxDao.markCompleted(item.outboxId)
    }

    private suspend fun processDailyLog(item: OutboxEntity) {
        val log = gson.fromJson(item.payloadJson, DailyLogEntity::class.java)
        var photoUrl = log.photoUrl ?: ""
        if (photoUrl.startsWith("file://")) {
            val file = File(photoUrl.removePrefix("file://"))
            try {
                val tgRef = telegramApi.uploadPhoto(BuildConfig.TELEGRAM_CHANNEL_ID, file).getOrThrow()
                photoUrl = tgRef
            } catch (e: Exception) {
                Log.w(TAG, "Telegram upload failed for log ${log.logId}, pushing without photo", e)
                photoUrl = ""
            }
        }
        val updated = log.copy(photoUrl = photoUrl.ifEmpty { null }, dirty = false)
        try {
            firestore.collection("daily_logs")
                .document(log.logId)
                .set(updated)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Firestore push failed for log ${log.logId}", e)
            outboxDao.markFailed(item.outboxId)
            return
        }
        dailyLogDao.upsert(updated)
        outboxDao.markCompleted(item.outboxId)
    }

    companion object {
        private const val TAG = "SyncManager"
    }
}
