package com.rostry.prototype.ui.farm

import com.rostry.prototype.data.local.entity.DailyLogEntity
import com.rostry.prototype.data.local.entity.FarmAssetEntity

data class FarmUiState(
    val assets: List<FarmAssetEntity> = emptyList(),
    val todayLogs: List<DailyLogEntity> = emptyList(),
    val pendingSyncCount: Int = 0,
    val lastSyncAt: Long? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
