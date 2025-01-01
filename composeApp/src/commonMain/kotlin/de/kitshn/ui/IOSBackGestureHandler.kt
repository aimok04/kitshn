package de.kitshn.ui

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import de.kitshn.Platforms
import de.kitshn.platformDetails

private const val GESTURE_ACCEPTED_START = 80f
private const val GESTURE_MINIMUM_FINISH = 300f

/**
 * only applies on iOS
 */
@Composable
fun IOSBackGestureHandler(
    isEnabled: Boolean,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    if(platformDetails.platform != Platforms.IOS) {
        Box(
            Modifier.fillMaxSize()
        ) {
            content()
        }
    } else {
        val density = LocalDensity.current

        var offsetStart by remember { mutableStateOf(-1f) }
        var offsetFinish by remember { mutableStateOf(-1f) }

        var mIsEnabled by remember { mutableStateOf(false) }
        LaunchedEffect(isEnabled) { mIsEnabled = isEnabled }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            if(offset.x <= GESTURE_ACCEPTED_START) offsetStart = offset.x
                        },

                        onDragEnd = {
                            val isOnBackGestureActivated = mIsEnabled
                                    && offsetStart in 0f..GESTURE_ACCEPTED_START
                                    && offsetFinish > GESTURE_MINIMUM_FINISH

                            if(isOnBackGestureActivated) onBack()

                            offsetStart = -1f
                            offsetFinish = -1f
                        },

                        onHorizontalDrag = { change, _ -> offsetFinish = change.position.x }
                    )
                }.run {
                    if(!mIsEnabled) return@run this
                    if(offsetStart !in 0f..GESTURE_ACCEPTED_START) return@run this

                    val coerceInOffset = offsetFinish.coerceIn(0f, GESTURE_MINIMUM_FINISH)
                    val factor = (coerceInOffset / GESTURE_MINIMUM_FINISH)

                    offset(
                        x = with(density) { coerceInOffset.toDp() }
                    ).scale(
                        1f - (0.1f * factor)
                    ).alpha(
                        1f - (0.3f * factor)
                    ).clip(
                        RoundedCornerShape(16.dp * factor)
                    )
                }
        ) {
            content()
        }
    }
}