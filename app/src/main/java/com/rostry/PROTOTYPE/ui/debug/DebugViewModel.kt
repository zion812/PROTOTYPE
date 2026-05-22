package com.rostry.prototype.ui.debug

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.rostry.prototype.data.connectivity.ConnectivityObserver
import com.rostry.prototype.data.local.AppDatabase
import com.rostry.prototype.data.local.dao.OutboxDao
import com.rostry.prototype.data.local.entity.OutboxEntity
import com.rostry.prototype.data.repo.SyncRepository
import com.rostry.prototype.telegram.TelegramApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DebugViewModel @Inject constructor(
    private val appDatabase: AppDatabase,
    private val syncRepository: SyncRepository,
    private val outboxDao: OutboxDao,
    private val telegramApi: TelegramApi,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val _firebaseUid = MutableStateFlow("")
    val firebaseUid: StateFlow<String> = _firebaseUid.asStateFlow()

    private val _networkStatus = MutableStateFlow(false)
    val networkStatus: StateFlow<Boolean> = _networkStatus.asStateFlow()

    private val _botStatus = MutableStateFlow("Checking...")
    val botStatus: StateFlow<String> = _botStatus.asStateFlow()

    private val _outboxItems = MutableStateFlow<List<OutboxEntity>>(emptyList())
    val outboxItems: StateFlow<List<OutboxEntity>> = _outboxItems.asStateFlow()

    private val _clearResult = MutableStateFlow<String?>(null)
    val clearResult: StateFlow<String?> = _clearResult.asStateFlow()

    private val _syncResult = MutableStateFlow<String?>(null)
    val syncResult: StateFlow<String?> = _syncResult.asStateFlow()

    init {
        _firebaseUid.value = FirebaseAuth.getInstance().currentUser?.uid ?: "Not signed in"
        viewModelScope.launch {
            connectivityObserver.isOnline.collect { online ->
                _networkStatus.value = online
            }
        }
        checkBotStatus()
    }

    fun checkBotStatus() {
        _botStatus.value = "Checking..."
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                telegramApi.getMe()
            }
            _botStatus.value = result.fold(
                onSuccess = { "Connected: ${it.substringBefore("\"")}" },
                onFailure = { "Error: ${it.message}" }
            )
        }
    }

    fun loadOutbox() {
        viewModelScope.launch {
            _outboxItems.value = withContext(Dispatchers.IO) {
                outboxDao.getAll()
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            _clearResult.value = null
            _clearResult.value = withContext(Dispatchers.IO) {
                try {
                    appDatabase.clearAllTables()
                    Log.d(TAG, "All local data cleared")
                    "Local data cleared successfully"
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to clear data", e)
                    "Error: ${e.message}"
                }
            }
        }
    }

    fun forceSync() {
        viewModelScope.launch {
            _syncResult.value = null
            syncRepository.syncAll().fold(
                onSuccess = {
                    Log.d(TAG, "Sync completed successfully")
                    _syncResult.value = "Sync completed"
                },
                onFailure = { e ->
                    Log.e(TAG, "Sync failed", e)
                    _syncResult.value = "Sync failed: ${e.message}"
                }
            )
        }
    }

    fun dismissClearResult() {
        _clearResult.value = null
    }

    fun dismissSyncResult() {
        _syncResult.value = null
    }

    companion object {
        private const val TAG = "DebugViewModel"
    }
}
