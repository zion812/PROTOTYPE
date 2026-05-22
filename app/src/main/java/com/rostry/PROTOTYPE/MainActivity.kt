package com.rostry.prototype

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.rostry.prototype.telegram.ImageLoaderProvider
import com.rostry.prototype.telegram.LocalImageLoader
import com.rostry.prototype.ui.RootScreen
import com.rostry.prototype.ui.theme.RostryPrototypeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = LocalContext.current.applicationContext as PrototypeApplication
            val imageLoader = remember { app.imageLoader }

            RostryPrototypeTheme {
                ImageLoaderProvider(imageLoader = imageLoader) {
                    RootScreen()
                }
            }
        }
    }
}
