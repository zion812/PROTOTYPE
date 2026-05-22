package com.rostry.prototype.ui.onboarding

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.rostry.prototype.data.local.entity.FarmAssetEntity
import com.rostry.prototype.data.repo.FarmRepository
import com.rostry.prototype.data.repo.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class OnboardingUiState(
    val farmName: String = "",
    val location: String = "",
    val birdName: String = "",
    val breed: String = "",
    val birdType: String = "",
    val photoUri: Uri? = null,
    val isSaving: Boolean = false,
    val farmNameError: String? = null,
    val birdNameError: String? = null,
    val birdSaved: Boolean = false,
    val saveError: String? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val application: Application,
    private val userRepository: UserRepository,
    private val farmRepository: FarmRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val firebaseUid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    fun updateFarmName(name: String) {
        _state.value = _state.value.copy(farmName = name, farmNameError = null)
    }

    fun updateLocation(location: String) {
        _state.value = _state.value.copy(location = location)
    }

    fun updateBirdName(name: String) {
        _state.value = _state.value.copy(birdName = name, birdNameError = null)
    }

    fun updateBreed(breed: String) {
        _state.value = _state.value.copy(breed = breed)
    }

    fun updateBirdType(birdType: String) {
        _state.value = _state.value.copy(birdType = birdType)
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
                            "location" to _state.value.location.trim(),
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

            val imageUrl = _state.value.photoUri?.let { copyToInternalStorage(it) }

            val entity = FarmAssetEntity(
                farmerId = userId,
                name = name,
                breed = _state.value.breed.trim(),
                birdType = _state.value.birdType,
                imageUrl = imageUrl,
                createdAt = System.currentTimeMillis()
            )

            farmRepository.createFarmAsset(entity)
            _state.value = _state.value.copy(
                isSaving = false,
                birdSaved = true,
                birdName = "",
                breed = "",
                birdType = "",
                photoUri = null
            )
            onSuccess()
        }
    }

    fun resetBirdSaved() {
        _state.value = _state.value.copy(birdSaved = false)
    }

    private fun copyToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = application.contentResolver.openInputStream(uri)
                ?: return null
            val dir = File(application.filesDir, "bird_photos")
            dir.mkdirs()
            val file = File(dir, "bird_${System.currentTimeMillis()}.jpg")
            inputStream.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            "file://${file.absolutePath}"
        } catch (e: Exception) {
            null
        }
    }
}
