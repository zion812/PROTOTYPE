package com.rostry.prototype.data.repo

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.rostry.prototype.BuildConfig
import com.rostry.prototype.data.local.dao.DailyLogDao
import com.rostry.prototype.data.local.dao.FarmAssetDao
import com.rostry.prototype.data.local.dao.OutboxDao
import com.rostry.prototype.data.local.entity.OutboxEntity
import com.rostry.prototype.domain.model.DailyLog
import com.rostry.prototype.domain.model.ENTITY_TYPE_DAILY_LOG
import com.rostry.prototype.domain.model.ENTITY_TYPE_FARM_ASSET
import com.rostry.prototype.domain.model.FarmAsset
import com.rostry.prototype.telegram.TelegramApi
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    private val outboxDao: OutboxDao,
    private val farmAssetDao: FarmAssetDao,
    private val dailyLogDao: DailyLogDao,
    private val telegramApi: TelegramApi,
    private val firestore: FirebaseFirestore,
    private val gson: Gson
) {
    suspend fun syncAll(): Result<Unit> = runCatching {
        val pendingItems = outboxDao.getPending()
        Log.d(TAG, "syncAll started, ${pendingItems.size} pending items")
        for (item in pendingItems) {
            try {
                processOutboxItem(item)
                Log.d(TAG, "Outbox item ${item.outboxId} synced successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync outbox item ${item.outboxId}", e)
            }
        }
        Log.d(TAG, "syncAll completed")
    }

    private suspend fun processOutboxItem(item: OutboxEntity) {
        when (item.entityType) {
            ENTITY_TYPE_FARM_ASSET -> syncFarmAsset(item)
            ENTITY_TYPE_DAILY_LOG -> syncDailyLog(item)
        }
    }

    private suspend fun syncFarmAsset(item: OutboxEntity) {
        val asset = gson.fromJson(item.payloadJson, FarmAsset::class.java)
        Log.d(TAG, "Syncing FarmAsset ${asset.assetId}: ${asset.name}")

        var imageUrl = asset.imageUrl
        if (imageUrl.startsWith("file://")) {
            val file = File(imageUrl.removePrefix("file://"))
            Log.d(TAG, "Uploading image for FarmAsset ${asset.assetId}")
            val tgRef = telegramApi.uploadPhoto(BuildConfig.TELEGRAM_CHANNEL_ID, file)
                .getOrThrow()
            imageUrl = telegramApi.resolveUrl(tgRef).getOrThrow()
            Log.d(TAG, "Image resolved to: $imageUrl")
        }

        val updatedAsset = asset.copy(imageUrl = imageUrl, dirty = false)

        val data = gson.toFirestoreMap(updatedAsset)
        firestore.collection("farm_assets")
            .document(updatedAsset.assetId.toString())
            .set(data)
            .await()
        Log.d(TAG, "FarmAsset ${asset.assetId} pushed to Firestore")

        val localEntity = farmAssetDao.getById(asset.assetId)
        if (localEntity != null) {
            farmAssetDao.update(localEntity.copy(imageUrl = imageUrl, dirty = false))
            Log.d(TAG, "Local FarmAsset ${asset.assetId} updated with resolved URL")
        }
        outboxDao.markCompleted(item.outboxId)
    }

    private suspend fun syncDailyLog(item: OutboxEntity) {
        val log = gson.fromJson(item.payloadJson, DailyLog::class.java)
        Log.d(TAG, "Syncing DailyLog ${log.logId}")

        var photoUrl = log.photoUrl
        if (photoUrl.startsWith("file://")) {
            val file = File(photoUrl.removePrefix("file://"))
            Log.d(TAG, "Uploading photo for DailyLog ${log.logId}")
            val tgRef = telegramApi.uploadPhoto(BuildConfig.TELEGRAM_CHANNEL_ID, file)
                .getOrThrow()
            photoUrl = telegramApi.resolveUrl(tgRef).getOrThrow()
            Log.d(TAG, "Photo resolved to: $photoUrl")
        }

        val updatedLog = log.copy(photoUrl = photoUrl, dirty = false)

        val data = gson.toFirestoreMap(updatedLog)
        firestore.collection("daily_logs")
            .document(updatedLog.logId.toString())
            .set(data)
            .await()
        Log.d(TAG, "DailyLog ${log.logId} pushed to Firestore")

        val localEntity = dailyLogDao.getById(log.logId)
        if (localEntity != null) {
            dailyLogDao.upsert(localEntity.copy(photoUrl = photoUrl, dirty = false))
            Log.d(TAG, "Local DailyLog ${log.logId} updated with resolved URL")
        }
        outboxDao.markCompleted(item.outboxId)
    }

    private fun Gson.toFirestoreMap(data: Any): Map<String, Any?> {
        val jsonObject = toJsonTree(data).asJsonObject
        return jsonObject.entrySet().associate { (key, value) ->
            key to value.toFirestoreValue()
        }
    }

    private fun JsonElement.toFirestoreValue(): Any? = when {
        isJsonNull -> null
        isJsonPrimitive -> {
            val p = asJsonPrimitive
            when {
                p.isBoolean -> p.asBoolean
                p.isNumber -> {
                    val d = p.asDouble
                    val l = d.toLong()
                    if (d == l.toDouble()) l else d
                }
                p.isString -> p.asString
                else -> p.asString
            }
        }
        isJsonArray -> asJsonArray.map { it.toFirestoreValue() }
        isJsonObject -> asJsonObject.entrySet().associate { it.key to it.value.toFirestoreValue() }
        else -> null
    }

    companion object {
        private const val TAG = "SyncRepository"
    }
}
