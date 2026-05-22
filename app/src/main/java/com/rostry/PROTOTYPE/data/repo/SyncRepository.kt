package com.rostry.prototype.data.repo

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.await
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
        for (item in pendingItems) {
            try {
                processOutboxItem(item)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync outbox item ${item.outboxId}", e)
            }
        }
    }

    private suspend fun processOutboxItem(item: OutboxEntity) {
        when (item.entityType) {
            ENTITY_TYPE_FARM_ASSET -> syncFarmAsset(item)
            ENTITY_TYPE_DAILY_LOG -> syncDailyLog(item)
        }
    }

    private suspend fun syncFarmAsset(item: OutboxEntity) {
        val asset = gson.fromJson(item.payloadJson, FarmAsset::class.java)

        var imageUrl = asset.imageUrl
        if (imageUrl.startsWith("file://")) {
            val file = File(imageUrl.removePrefix("file://"))
            val tgRef = telegramApi.uploadPhoto(BuildConfig.TELEGRAM_CHANNEL_ID, file)
                .getOrThrow()
            imageUrl = telegramApi.resolveUrl(tgRef).getOrThrow()
        }

        val updatedAsset = asset.copy(imageUrl = imageUrl, dirty = false)

        val data = gson.toFirestoreMap(updatedAsset)
        firestore.collection("farm_assets")
            .document(updatedAsset.assetId.toString())
            .set(data)
            .await()

        val localEntity = farmAssetDao.getById(asset.assetId)
        if (localEntity != null) {
            farmAssetDao.update(localEntity.copy(imageUrl = imageUrl, dirty = false))
        }
        outboxDao.markCompleted(item.outboxId)
    }

    private suspend fun syncDailyLog(item: OutboxEntity) {
        val log = gson.fromJson(item.payloadJson, DailyLog::class.java)

        var photoUrl = log.photoUrl
        if (photoUrl.startsWith("file://")) {
            val file = File(photoUrl.removePrefix("file://"))
            val tgRef = telegramApi.uploadPhoto(BuildConfig.TELEGRAM_CHANNEL_ID, file)
                .getOrThrow()
            photoUrl = telegramApi.resolveUrl(tgRef).getOrThrow()
        }

        val updatedLog = log.copy(photoUrl = photoUrl, dirty = false)

        val data = gson.toFirestoreMap(updatedLog)
        firestore.collection("daily_logs")
            .document(updatedLog.logId.toString())
            .set(data)
            .await()

        val localEntity = dailyLogDao.getById(log.logId)
        if (localEntity != null) {
            dailyLogDao.upsert(localEntity.copy(photoUrl = photoUrl, dirty = false))
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
