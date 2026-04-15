package de.kitshn.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun rememberScrollToHideConnection(
    enabled: Boolean,
    onHide: suspend () -> Unit,
    onShow: suspend () -> Unit
): NestedScrollConnection {
    val scope = rememberCoroutineScope()
    
    return remember(enabled) {
        var showJob: Job? = null
        var scrollDelta = 0f

        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (!enabled) return super.onPreScroll(available, source)

                val delta = available.y
                
                // Reset accumulation on direction change
                if ((delta > 0 && scrollDelta < 0) || (delta < 0 && scrollDelta > 0)) {
                    scrollDelta = 0f
                }
                
                scrollDelta += delta

                if (scrollDelta < -20f) { // Significant scroll down
                    showJob?.cancel()
                    scope.launch { onHide() }
                    scrollDelta = 0f
                } else if (scrollDelta > 20f) { // Significant scroll up
                    showJob?.cancel()
                    scope.launch { onShow() }
                    scrollDelta = 0f
                }

                // Show after delay if scrolling stopped during user input
                if (source == NestedScrollSource.UserInput) {
                    showJob?.cancel()
                    showJob = scope.launch {
                        delay(600)
                        onShow()
                        scrollDelta = 0f
                    }
                }

                return super.onPreScroll(available, source)
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (enabled) {
                    showJob?.cancel()
                    onShow()
                    scrollDelta = 0f
                }
                return super.onPostFling(consumed, available)
            }
        }
    }
}
