package de.kitshn.android.ui.component.icons

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

enum class IconWithStateState {
    DEFAULT,
    LOADING,
    ERROR,
    SUCCESS,
}

@Composable
fun IconWithState(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    progressIndicatorSize: Dp = 22.dp,
    progressIndicatorTint: Color = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
    tint: Color = LocalContentColor.current,
    state: IconWithStateState
) {
    var actualState by remember { mutableStateOf(state) }
    LaunchedEffect(state) {
        actualState = state

        if(state != IconWithStateState.ERROR && state != IconWithStateState.SUCCESS) return@LaunchedEffect

        delay(1000)
        actualState = IconWithStateState.DEFAULT
    }

    if(actualState == IconWithStateState.LOADING) {
        CircularProgressIndicator(
            modifier.size(progressIndicatorSize),
            strokeCap = StrokeCap.Round,
            color = progressIndicatorTint
        )
        return
    }

    Icon(
        imageVector = when(actualState) {
            IconWithStateState.ERROR -> Icons.Rounded.Close
            IconWithStateState.SUCCESS -> Icons.Rounded.Check
            else -> imageVector
        },
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint
    )
}