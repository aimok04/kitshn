package de.kitshn.ui.component.input

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal actual fun getScreenHeight(): Dp {
    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current
    return with(density) {
        windowInfo.containerSize.height.toDp()
    }
}