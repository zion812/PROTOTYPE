package com.rostry.prototype.ui.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rostry.prototype.sync.SyncManager
import com.rostry.prototype.sync.SyncState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val syncManager: SyncManager
) : ViewModel() {

    val syncState: StateFlow<SyncState> = syncManager.syncState

    fun syncNow() {
        viewModelScope.launch {
            syncManager.syncAll()
        }
    }
}
