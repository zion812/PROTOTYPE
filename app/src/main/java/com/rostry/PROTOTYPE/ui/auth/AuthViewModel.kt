package com.rostry.prototype.ui.auth

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser
import com.rostry.prototype.data.repo.UserRepository
import com.rostry.prototype.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    private val googleAuthHelper: GoogleAuthHelper,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    val isLoggedIn: Boolean
        get() = googleAuthHelper.getCurrentUser() != null

    fun getSignInIntent(): Intent = googleAuthHelper.getSignInIntent()

    fun handleSignInResult(data: Intent?) {
        _authState.value = AuthUiState.Loading
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken

            if (idToken != null) {
                viewModelScope.launch {
                    val result = googleAuthHelper.firebaseAuthWithGoogle(idToken)
                    _authState.value = result.fold(
                        onSuccess = { firebaseUser ->
                            val userId = firebaseUser.uid
                            val existingUser = userRepository.getUser(userId).first()

                            val user = if (existingUser != null) {
                                existingUser.copy(
                                    displayName = firebaseUser.displayName ?: existingUser.displayName,
                                    photoUrl = firebaseUser.photoUrl?.toString() ?: existingUser.photoUrl,
                                    email = firebaseUser.email ?: existingUser.email
                                )
                            } else {
                                firebaseUser.toDomainUser()
                            }

                            userRepository.saveUser(user)
                            AuthUiState.Success(user)
                        },
                        onFailure = { e ->
                            AuthUiState.Error(e.message ?: "Firebase authentication failed")
                        }
                    )
                }
            } else {
                _authState.value = AuthUiState.Error("Google ID Token is null")
            }
        } catch (e: ApiException) {
            _authState.value = AuthUiState.Error("Google Sign-In failed: ${e.statusCode}")
        } catch (e: Exception) {
            _authState.value = AuthUiState.Error(e.message ?: "An unexpected error occurred")
        }
    }

    suspend fun checkNeedsOnboarding(userId: String): Boolean {
        return userRepository.getUser(userId).first()?.farmName.isNullOrBlank()
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
    userId = uid,
    displayName = displayName ?: "",
    email = email ?: "",
    photoUrl = photoUrl?.toString() ?: "",
    farmName = "",
    userType = "FARMER",
    createdAt = System.currentTimeMillis()
)
