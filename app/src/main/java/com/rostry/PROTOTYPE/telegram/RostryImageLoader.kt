package com.rostry.prototype.telegram

import android.content.Context
import coil.ImageLoader
import coil.request.CachePolicy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalImageLoader = staticCompositionLocalOf<ImageLoader> {
    error("No ImageLoader provided. Wrap your composable with ImageLoaderProvider.")
}

object RostryImageLoader {

    @Volatile
    private var instance: ImageLoader? = null

    fun getInstance(context: Context, telegramApi: TelegramApi): ImageLoader {
        return instance ?: synchronized(this) {
            instance ?: buildImageLoader(context, telegramApi).also { instance = it }
        }
    }

    private fun buildImageLoader(context: Context, telegramApi: TelegramApi): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .components {
                add(TelegramImageFetcher.Factory(telegramApi))
            }
            .build()
    }

    fun resetForTest() {
        instance = null
    }
}

@Composable
fun ImageLoaderProvider(
    imageLoader: ImageLoader,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalImageLoader provides imageLoader) {
        content()
    }
}
