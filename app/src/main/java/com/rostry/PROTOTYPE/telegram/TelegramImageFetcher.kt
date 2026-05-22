package com.rostry.prototype.telegram

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.Options
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

class TelegramImageFetcher(
    private val data: String,
    private val options: Options,
    private val api: TelegramApi,
) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        val url = withContext(Dispatchers.IO) {
            api.resolveUrl(data).getOrNull()
        } ?: return null
        return withContext(Dispatchers.IO) {
            val httpUrl = url.toHttpUrl()
            val request = Request.Builder().url(httpUrl).build()
            val response = OkHttpClient().newCall(request).execute()
            val bytes = response.body?.bytes() ?: return@withContext null
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            DrawableResult(
                drawable = BitmapDrawable(options.context.resources, bitmap),
                isSampled = false,
                dataSource = DataSource.NETWORK,
            )
        }
    }

    class Factory(
        private val api: TelegramApi,
    ) : Fetcher.Factory<String> {
        override fun create(
            data: String,
            options: Options,
            imageLoader: ImageLoader,
        ): Fetcher? {
            if (!data.startsWith("tg://")) return null
            return TelegramImageFetcher(data, options, api)
        }
    }
}
