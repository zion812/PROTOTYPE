package com.rostry.prototype.ui.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import timber.log.Timber

@Composable
fun GoogleSignInScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Timber.d("GoogleSignInScreen: launcher result code: ${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.handleSignInResult(result.data)
        } else {
            Timber.w("GoogleSignInScreen: sign in cancelled or failed")
        }
    }

    val currentError = (authState as? AuthUiState.Error)?.message
    LaunchedEffect(currentError) {
        currentError?.let { snackbarHostState.showSnackbar(it) }
    }

    LaunchedEffect(authState) {
        if (authState is AuthUiState.Success) {
            val user = (authState as AuthUiState.Success).user
            if (user.farmName.isBlank()) {
                onNavigateToOnboarding()
            } else {
                onNavigateToDashboard()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome to Rostry",
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            when (authState) {
                is AuthUiState.Loading -> {
                    CircularProgressIndicator()
                }

                is AuthUiState.Error -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    GoogleSignInButton(
                        enabled = true,
                        onClick = {
                            viewModel.resetState()
                            launcher.launch(viewModel.getSignInIntent())
                        }
                    )
                }

                is AuthUiState.Success -> {
                    CircularProgressIndicator()
                }

                is AuthUiState.Idle -> {
                    GoogleSignInButton(
                        enabled = true,
                        onClick = {
                            launcher.launch(viewModel.getSignInIntent())
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun GoogleSignInButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Icon(
            imageVector = Icons.Default.Email,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
        Text("Sign in with Google")
    }
}
