package com.rostry.prototype.ui.auth

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseUser
import com.rostry.prototype.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Success(val user: User) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val googleAuthHelper: GoogleAuthHelper
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    val isLoggedIn: Boolean
        get() = googleAuthHelper.getCurrentUser() != null

    fun getSignInIntent(): Intent = googleAuthHelper.getSignInIntent()

    fun signInWithGoogle(data: Intent?) {
        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(data)
            if (account.isSuccessful) {
                val idToken = account.result?.idToken
                if (idToken != null) {
                    _authState.value = AuthUiState.Loading
                    viewModelScope.launch {
                        val result = googleAuthHelper.firebaseAuthWithGoogle(idToken)
                        _authState.value = result.fold(
                            onSuccess = { firebaseUser ->
                                AuthUiState.Success(firebaseUser.toDomainUser())
                            },
                            onFailure = { e ->
                                AuthUiState.Error(e.message ?: "Authentication failed")
                            }
                        )
                    }
                } else {
                    _authState.value = AuthUiState.Error("Google sign-in failed")
                }
            } else {
                _authState.value = AuthUiState.Error("Google sign-in failed")
            }
        } catch (e: Exception) {
            _authState.value = AuthUiState.Error(e.message ?: "Google sign-in failed")
        }
    }

    fun signOut() {
        googleAuthHelper.signOut()
        _authState.value = AuthUiState.Idle
    }

    fun resetState() {
        _authState.value = AuthUiState.Idle
    }
}

private fun FirebaseUser.toDomainUser(): User = User(
    userId = uid.hashCode().toLong(),
    displayName = displayName ?: "",
    email = email ?: "",
    photoUrl = photoUrl?.toString() ?: "",
    farmName = "",
    userType = "FARMER"
)
