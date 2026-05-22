package com.rostry.prototype

import android.app.Application
import coil.ImageLoader
import com.google.firebase.FirebaseApp
import com.rostry.prototype.telegram.RostryImageLoader
import com.rostry.prototype.telegram.TelegramApi
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class PrototypeApplication : Application() {

    @Inject
    lateinit var telegramApi: TelegramApi

    lateinit var imageLoader: ImageLoader
        private set

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        Timber.plant(Timber.DebugTree())

        imageLoader = RostryImageLoader.getInstance(this, telegramApi)
    }
}
