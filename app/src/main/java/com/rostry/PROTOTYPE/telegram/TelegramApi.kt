package com.rostry.prototype.telegram

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.rostry.prototype.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class TelegramApi {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    fun uploadPhoto(channelId: String, file: File): Result<String> = runCatching {
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

        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: throw IOException("Empty response body")
        val json = gson.fromJson(body, JsonObject::class.java)

        check(json.get("ok")?.asBoolean == true) {
            "Telegram API error: ${json.get("description")?.asString}"
        }

        val result = json.getAsJsonObject("result")
        val photoArray = result.getAsJsonArray("photo")
        val largest = photoArray[photoArray.size() - 1].asJsonObject
        val fileId = largest.get("file_id")?.asString
            ?: throw IOException("Missing file_id in response")

        "tg://$fileId@$channelId"
    }

    fun resolveUrl(tgRef: String): Result<String> = runCatching {
        val fileId = tgRef.removePrefix("tg://").substringBefore("@")
        require(fileId.isNotBlank()) { "Invalid tgRef: $tgRef" }

        val request = Request.Builder()
            .url("$BASE_URL/bot$TOKEN/getFile?file_id=$fileId")
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: throw IOException("Empty response body")
        val json = gson.fromJson(body, JsonObject::class.java)

        check(json.get("ok")?.asBoolean == true) {
            "Telegram API error: ${json.get("description")?.asString}"
        }

        val filePath = json.getAsJsonObject("result").get("file_path")?.asString
            ?: throw IOException("Missing file_path in response")

        "$BASE_URL/file/bot$TOKEN/$filePath"
    }

    fun getMe(): Result<String> = runCatching {
        val request = Request.Builder()
            .url("$BASE_URL/bot$TOKEN/getMe")
            .build()
        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: throw IOException("Empty response body")
        val json = gson.fromJson(body, JsonObject::class.java)
        check(json.get("ok")?.asBoolean == true) {
            "Telegram API error: ${json.get("description")?.asString}"
        }
        json.getAsJsonObject("result").toString()
    }

    companion object {
        private const val BASE_URL = "https://api.telegram.org"
        private val TOKEN: String get() = BuildConfig.TELEGRAM_BOT_TOKEN
    }
}
