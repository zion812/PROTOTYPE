package com.rostry.prototype.ui.onboarding

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.rostry.prototype.data.repo.FarmRepository
import com.rostry.prototype.data.repo.UserRepository
import com.rostry.prototype.domain.model.FarmAsset
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val farmName: String = "",
    val birdName: String = "",
    val breed: String = "",
    val photoUri: Uri? = null,
    val isSaving: Boolean = false,
    val farmNameError: String? = null,
    val birdNameError: String? = null,
    val birdSaved: Boolean = false,
    val saveError: String? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val farmRepository: FarmRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    private val userId: Long
        get() = FirebaseAuth.getInstance().currentUser?.uid?.hashCode()?.toLong() ?: 0L

    private val firebaseUid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    fun updateFarmName(name: String) {
        _state.value = _state.value.copy(farmName = name, farmNameError = null)
    }

    fun updateBirdName(name: String) {
        _state.value = _state.value.copy(birdName = name, birdNameError = null)
    }

    fun updateBreed(breed: String) {
        _state.value = _state.value.copy(breed = breed)
    }

    fun updatePhotoUri(uri: Uri?) {
        _state.value = _state.value.copy(photoUri = uri)
    }

    fun clearError() {
        _state.value = _state.value.copy(saveError = null)
    }

    fun saveFarmName(onSuccess: () -> Unit) {
        val name = _state.value.farmName.trim()
        if (name.isBlank()) {
            _state.value = _state.value.copy(farmNameError = "Farm name cannot be empty")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, saveError = null)

            val result = userRepository.updateFarmName(userId, name)
            result.fold(
                onSuccess = {
                    val firebaseUser = FirebaseAuth.getInstance().currentUser
                    if (firebaseUser != null) {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()
                        firebaseUser.updateProfile(profileUpdates)

                        val userMap = hashMapOf(
                            "farmName" to name,
                            "userId" to firebaseUid,
                            "displayName" to (firebaseUser.displayName ?: ""),
                            "email" to (firebaseUser.email ?: "")
                        )
                        firestore.collection("users").document(firebaseUid).set(userMap)
                    }

                    _state.value = _state.value.copy(isSaving = false)
                    onSuccess()
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isSaving = false,
                        saveError = e.message ?: "Failed to save farm name"
                    )
                }
            )
        }
    }

    fun saveBird(onSuccess: () -> Unit) {
        val name = _state.value.birdName.trim()
        if (name.isBlank()) {
            _state.value = _state.value.copy(birdNameError = "Bird name cannot be empty")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, saveError = null)

            val asset = FarmAsset(
                assetId = System.currentTimeMillis(),
                farmerId = userId,
                name = name,
                breed = _state.value.breed.trim(),
                imageUrl = _state.value.photoUri?.toString() ?: "",
                createdAt = System.currentTimeMillis(),
                dirty = true
            )

            val result = farmRepository.createAsset(asset)
            result.fold(
                onSuccess = {
                    _state.value = _state.value.copy(
                        isSaving = false,
                        birdSaved = true,
                        birdName = "",
                        breed = "",
                        photoUri = null
                    )
                    onSuccess()
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isSaving = false,
                        saveError = e.message ?: "Failed to save bird"
                    )
                }
            )
        }
    }

    fun resetBirdSaved() {
        _state.value = _state.value.copy(birdSaved = false)
    }
}
