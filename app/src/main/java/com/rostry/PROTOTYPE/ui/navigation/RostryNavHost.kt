package com.rostry.prototype.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.rostry.prototype.ui.auth.AuthNavWrapper
import com.rostry.prototype.ui.debug.DebugScreen
import com.rostry.prototype.ui.farm.DailyLogScreen
import com.rostry.prototype.ui.farm.FarmDashboardScreen
import com.rostry.prototype.ui.market.MarketplaceBrowseScreen
import com.rostry.prototype.ui.onboarding.AddBirdScreen
import com.rostry.prototype.ui.onboarding.FarmSetupScreen

@Composable
fun RostryNavHost(
    navController: NavHostController,
    startDestination: String = Routes.AUTH,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
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
                        navController.navigate(Routes.ADD_BIRD)
                    }
                )
            }
        }

        composable(Routes.ADD_BIRD) {
            AddBirdScreen(
                onNavigateToDashboard = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DASHBOARD) {
            FarmDashboardScreen(
                onAddDailyLog = {
                    navController.navigate(Routes.DAILY_LOG)
                },
                onAddBird = {
                    navController.navigate(Routes.ADD_BIRD)
                },
                onOpenDebug = {
                    navController.navigate(Routes.DEBUG)
                }
            )
        }

        composable(Routes.DEBUG) {
            DebugScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.DAILY_LOG) {
            DailyLogScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.MARKETPLACE) {
            MarketplaceBrowseScreen()
        }
    }
}
