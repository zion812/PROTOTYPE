package com.rostry.prototype.data.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.rostry.prototype.data.repo.SyncRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectivityObserver @Inject constructor(
    @ApplicationContext private val context: Context,
    private val syncRepository: SyncRepository
) {
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var wasOffline = false

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.d(TAG, "Network available")
            _isOnline.value = true
            if (wasOffline) {
                scope.launch {
                    Log.d(TAG, "Was offline, scheduling sync in 2s")
                    delay(2000)
                    syncRepository.syncAll()
                }
            }
            wasOffline = false
        }

        override fun onLost(network: Network) {
            Log.d(TAG, "Network lost")
            _isOnline.value = false
            wasOffline = true
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            val connected = networkCapabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            )
            _isOnline.value = connected
            if (connected && wasOffline) {
                scope.launch {
                    Log.d(TAG, "Was offline (capabilities), scheduling sync in 2s")
                    delay(2000)
                    syncRepository.syncAll()
                }
                wasOffline = false
            }
            if (!connected) wasOffline = true
        }
    }

    init {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)

        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        _isOnline.value =
            capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        wasOffline = !_isOnline.value
        Log.d(TAG, "Initial connectivity: ${_isOnline.value}")
    }

    companion object {
        private const val TAG = "ConnectivityObserver"
    }
}
