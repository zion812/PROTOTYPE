package com.rostry.prototype.telegram

import com.rostry.prototype.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelegramApi @Inject constructor(
    val client: OkHttpClient
) {
    private val urlCache = ConcurrentHashMap<String, String>()

    suspend fun uploadPhoto(channelId: String, file: File): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val mediaType = "image/jpeg".toMediaType()
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("chat_id", channelId)
                .addFormDataPart("photo", file.name, file.asRequestBody(mediaType))
                .build()

            val request = Request.Builder()
                .url("$BASE_URL/bot$TOKEN/sendPhoto")
                .post(requestBody)
                .build()

            val json = executeAndParse(request)
            val result = json.optJSONObject("result") ?: throw IOException("Missing result in response")
            val photoArray = result.optJSONArray("photo") ?: throw IOException("Missing photo array in response")
            val largest = photoArray.optJSONObject(photoArray.length() - 1) ?: throw IOException("Empty photo array")
            val fileId = largest.optString("file_id", "") ?: throw IOException("Missing file_id in photo")
            check(fileId.isNotBlank()) { "Blank file_id in photo" }

            "tg://$fileId@$channelId"
        }
    }

    suspend fun getMe(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("$BASE_URL/bot$TOKEN/getMe")
                .build()
            executeAndParse(request).toString()
        }
    }

    suspend fun resolveUrl(fileId: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            urlCache[fileId]?.let { return@runCatching it }

            val request = Request.Builder()
                .url("$BASE_URL/bot$TOKEN/getFile?file_id=$fileId")
                .build()

            val json = executeAndParse(request)
            val result = json.optJSONObject("result") ?: throw IOException("Missing result in response")
            val filePath = result.optString("file_path", "") ?: throw IOException("Missing file_path in result")
            check(filePath.isNotBlank()) { "Blank file_path in result" }

            val url = "$BASE_URL/file/bot$TOKEN/$filePath"
            urlCache[fileId] = url
            url
        }
    }

    private fun executeAndParse(request: Request): JSONObject {
        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: throw IOException("Empty response body")
        val json = JSONObject(body)
        if (!json.optBoolean("ok", false)) {
            throw IOException("Telegram API error: ${json.optString("description", "unknown")}")
        }
        return json
    }

    companion object {
        private const val BASE_URL = "https://api.telegram.org"
        private val TOKEN: String get() = BuildConfig.TELEGRAM_BOT_TOKEN
    }
}
