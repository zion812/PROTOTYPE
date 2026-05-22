package com.rostry.prototype.telegram

import coil.ImageLoader
import coil.bitmap.BitmapPool
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.HttpResult
import coil.request.Options
import coil.size.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl

class TelegramImageFetcher(
    private val api: TelegramApi,
) : Fetcher<String> {

    override suspend fun fetch(
        pool: BitmapPool,
        data: String,
        size: Size,
        options: Options,
    ): FetchResult? {
        val url = withContext(Dispatchers.IO) {
            api.resolveUrl(data).getOrNull()
        } ?: return null
        return HttpResult(HttpUrl.get(url))
    }

    class Factory(
        private val api: TelegramApi,
    ) : Fetcher.Factory<String> {
        override fun create(
            data: String,
            options: Options,
            imageLoader: ImageLoader,
        ): Fetcher<String>? {
            if (!data.startsWith("tg://")) return null
            return TelegramImageFetcher(api)
        }
    }
}
