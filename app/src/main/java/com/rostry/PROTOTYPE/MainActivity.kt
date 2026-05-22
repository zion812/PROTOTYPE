package com.rostry.prototype

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import coil.Coil
import coil.ImageLoader
import com.google.firebase.FirebaseApp
import com.rostry.prototype.telegram.TelegramApi
import com.rostry.prototype.telegram.TelegramImageFetcher
import com.rostry.prototype.ui.RootScreen
import com.rostry.prototype.ui.theme.RostryPrototypeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeCoil()
        FirebaseApp.initializeApp(this)
        handleDeepLink(intent)

        enableEdgeToEdge()
        setContent {
            RostryPrototypeTheme {
                RootScreen()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun initializeCoil() {
        val telegramApi = TelegramApi()
        val imageLoader = ImageLoader.Builder(this)
            .components {
                add(TelegramImageFetcher.Factory(telegramApi))
            }
            .build()
        Coil.setImageLoader(imageLoader)
    }

    private fun handleDeepLink(intent: Intent?) {
        intent?.data?.let { uri ->
            when (uri.path) {
                "/dashboard" -> { /* deep link to dashboard */ }
                "/marketplace" -> { /* deep link to marketplace */ }
                else -> { /* unhandled deep link */ }
            }
        }
    }
}
