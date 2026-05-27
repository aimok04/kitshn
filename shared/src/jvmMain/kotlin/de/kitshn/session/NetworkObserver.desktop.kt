package de.kitshn.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

actual class NetworkObserver {
    private val _isConnected = MutableStateFlow(true)
    actual val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
}
