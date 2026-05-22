package com.rostry.prototype

import android.app.Application
import coil.Coil
import coil.ImageLoader
import com.google.firebase.FirebaseApp
import com.rostry.prototype.telegram.TelegramApi
import com.rostry.prototype.telegram.TelegramImageFetcher
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class PrototypeApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
        Timber.plant(Timber.DebugTree())

        val telegramApi = TelegramApi()
        val imageLoader = ImageLoader.Builder(this)
            .components {
                add(TelegramImageFetcher.Factory(telegramApi))
            }
            .build()
        Coil.setImageLoader(imageLoader)
    }
}
