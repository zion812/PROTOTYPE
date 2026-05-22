package com.rostry.prototype

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import coil.Coil
import coil.ImageLoader
import com.rostry.prototype.telegram.TelegramApi
import com.rostry.prototype.telegram.TelegramImageFetcher
import com.rostry.prototype.ui.auth.AuthNavWrapper
import com.rostry.prototype.ui.navigation.Routes
import com.rostry.prototype.ui.onboarding.AddBirdScreen
import com.rostry.prototype.ui.onboarding.FarmSetupScreen
import com.rostry.prototype.ui.theme.RostryPrototypeTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val telegramApi = TelegramApi()
        val imageLoader = ImageLoader.Builder(this)
            .components {
                add(TelegramImageFetcher.Factory(telegramApi))
            }
            .build()
        Coil.setImageLoader(imageLoader)

        enableEdgeToEdge()
        setContent {
            RostryPrototypeTheme {
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Routes.AUTH,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable(Routes.AUTH) {
                            AuthNavWrapper(
                                onNavigateToOnboarding = {
                                    navController.navigate(Routes.ONBOARDING) {
                                        popUpTo(Routes.AUTH) { inclusive = true }
                                    }
                                },
                                onNavigateToDashboard = {
                                    navController.navigate(Routes.DASHBOARD) {
                                        popUpTo(Routes.AUTH) { inclusive = true }
                                    }
                                }
                            )
                        }

                        navigation(
                            startDestination = Routes.FARM_SETUP,
                            route = Routes.ONBOARDING
                        ) {
                            composable(Routes.FARM_SETUP) {
                                FarmSetupScreen(
                                    onContinue = {
                                        navController.navigate(Routes.ADD_BIRD) {
                                            popUpTo(Routes.FARM_SETUP) { inclusive = true }
                                        }
                                    }
                                )
                            }

                            composable(Routes.ADD_BIRD) {
                                AddBirdScreen(
                                    onNavigateToDashboard = {
                                        navController.navigate(Routes.DASHBOARD) {
                                            popUpTo(Routes.AUTH) { inclusive = true }
                                        }
                                    }
                                )
                            }
                        }

                        composable(Routes.DASHBOARD) {
                            Text("Dashboard")
                        }
                    }
                }
            }
        }
    }
}
