package com.rostry.prototype.ui.farm

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.rostry.prototype.data.repo.FarmRepository
import com.rostry.prototype.data.repo.SyncRepository
import com.rostry.prototype.domain.model.DailyLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class FarmViewModel @Inject constructor(
    private val farmRepository: FarmRepository,
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FarmUiState())
    val uiState: StateFlow<FarmUiState> = _uiState.asStateFlow()

    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    init {
        loadAssets()
        loadTodayLog()
    }

    fun loadAssets() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            farmRepository.getAssets(userId).collect { assets ->
                _uiState.value = _uiState.value.copy(assets = assets, isLoading = false)
            }
        }
    }

    fun loadTodayLog() {
        viewModelScope.launch {
            val log = farmRepository.getTodayLog(userId)
            _uiState.value = _uiState.value.copy(todayLog = log)
        }
    }

    fun createDailyLog(
        feedKg: Double,
        mortalityCount: Int,
        notes: String,
        photoUri: Uri?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val log = DailyLog(
                farmerId = userId,
                logDate = cal.timeInMillis,
                feedKg = feedKg,
                mortalityCount = mortalityCount,
                photoUrl = photoUri?.toString(),
                notes = notes.ifBlank { null },
                createdAt = System.currentTimeMillis()
            )
            val result = farmRepository.createDailyLog(log)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, todayLog = log)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Failed to save daily log")
                }
            )
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            syncRepository.syncAll().fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        lastSyncAt = System.currentTimeMillis()
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Sync failed"
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
