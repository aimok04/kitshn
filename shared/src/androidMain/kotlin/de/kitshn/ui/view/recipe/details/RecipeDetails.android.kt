package de.kitshn.ui.view.recipe.details

import android.os.Build
import android.view.RoundedCorner
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.areStatusBarsVisible
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
actual fun StatusBarBackground() {
    val density = LocalDensity.current

    if(WindowInsets.areStatusBarsVisible) Box(
        Modifier
            .height(with(density) {
                WindowInsets.statusBars
                    .getTop(density)
                    .toDp()
            })
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.35f))
    )
}

@Composable
actual fun getImageRoundness(): Dp {
    val activity = LocalActivity.current
    val density = LocalDensity.current

    return remember {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            with(density) {
                activity?.window?.decorView?.rootWindowInsets?.getRoundedCorner(
                    RoundedCorner.POSITION_TOP_LEFT
                )?.radius?.toDp()
            } ?: 24.dp
        } else {
            24.dp
        }
    }
}