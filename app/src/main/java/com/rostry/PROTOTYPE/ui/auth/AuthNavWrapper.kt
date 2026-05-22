package com.rostry.prototype.ui.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AuthNavWrapper(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var authChecked by remember { mutableStateOf(false) }
    var loggedIn by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        loggedIn = FirebaseAuth.getInstance().currentUser != null
        authChecked = true
    }

    if (!authChecked) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (loggedIn) {
        LaunchedEffect(Unit) {
            val firebaseUser = FirebaseAuth.getInstance().currentUser ?: return@LaunchedEffect
            val userId = firebaseUser.uid
            val needsOnboarding = viewModel.checkNeedsOnboarding(userId)
            if (needsOnboarding) {
                onNavigateToOnboarding()
            } else {
                onNavigateToDashboard()
            }
        }
        return
    }

    GoogleSignInScreen(
        onNavigateToOnboarding = onNavigateToOnboarding,
        onNavigateToDashboard = onNavigateToDashboard
    )
}
