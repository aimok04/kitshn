package de.kitshn.session

import kotlinx.coroutines.flow.StateFlow

/**
 * Platform connectivity signal. Layered with [TandoorClient.lastCallSucceeded] to
 * decide `session.isOnline`: this tells us "the device has a route to the internet",
 * the call signal tells us "Tandoor actually answered."
 */
expect class NetworkObserver {
    val isConnected: StateFlow<Boolean>
}
