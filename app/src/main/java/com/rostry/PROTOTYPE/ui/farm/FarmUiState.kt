package com.rostry.prototype.ui.farm

import com.rostry.prototype.domain.model.DailyLog
import com.rostry.prototype.domain.model.FarmAsset

data class FarmUiState(
    val assets: List<FarmAsset> = emptyList(),
    val todayLog: DailyLog? = null,
    val pendingSyncCount: Int = 0,
    val lastSyncAt: Long? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
