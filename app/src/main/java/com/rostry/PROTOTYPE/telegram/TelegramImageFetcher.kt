package com.rostry.prototype.telegram

import android.content.Context
import android.util.Log
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.request.Options
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.CacheControl

class TelegramImageFetcher(
    private val data: String,
    private val api: TelegramApi,
    private val context: Context,
) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        val fileId = data.removePrefix("tg://").substringBefore("@")
        if (fileId.isBlank()) {
            Log.e(TAG, "Invalid tg URI: $data")
            return null
        }

        val url = api.resolveUrl(fileId).getOrNull()
        if (url == null) {
            Log.e(TAG, "Failed to resolve URL for fileId: $fileId")
            return null
        }

        return withContext(Dispatchers.IO) {
            try {
                val request = okhttp3.Request.Builder()
                    .url(url)
                    .cacheControl(CacheControl.FORCE_NETWORK)
                    .build()
                val response = api.client.newCall(request).execute()
                val body = response.body ?: return@withContext null
                SourceResult(
                    source = ImageSource(body.source(), context),
                    mimeType = body.contentType()?.toString(),
                    dataSource = DataSource.NETWORK
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch image from resolved URL", e)
                null
            }
        }
    }

    class Factory(
        private val api: TelegramApi,
    ) : Fetcher.Factory<String> {
        override fun create(
            data: String,
            options: Options,
            imageLoader: coil.ImageLoader,
        ): Fetcher? {
            if (!data.startsWith("tg://")) return null
            return TelegramImageFetcher(data, api, options.context)
        }
    }

    companion object {
        private const val TAG = "TelegramImageFetcher"
    }
}
