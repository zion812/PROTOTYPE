package com.rostry.prototype.ui.farm

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.rostry.prototype.data.local.dao.OutboxDao
import com.rostry.prototype.data.repo.FarmRepository
import com.rostry.prototype.data.repo.SyncRepository
import com.rostry.prototype.domain.model.DailyLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class FarmViewModel @Inject constructor(
    private val farmRepository: FarmRepository,
    private val syncRepository: SyncRepository,
    private val outboxDao: OutboxDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(FarmUiState())
    val uiState: StateFlow<FarmUiState> = _uiState.asStateFlow()

    private val userId: Long
        get() = FirebaseAuth.getInstance().currentUser?.uid?.hashCode()?.toLong() ?: 0L

    init {
        loadAssets()
        observePendingCount()
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

    private fun observePendingCount() {
        viewModelScope.launch {
            outboxDao.observePendingCount().collect { count ->
                _uiState.value = _uiState.value.copy(pendingSyncCount = count)
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
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val log = DailyLog(
                logId = System.currentTimeMillis(),
                farmerId = userId,
                assetId = null,
                logDate = today,
                feedKg = feedKg,
                mortalityCount = mortalityCount,
                photoUrl = photoUri?.toString() ?: "",
                notes = notes,
                dirty = true
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
