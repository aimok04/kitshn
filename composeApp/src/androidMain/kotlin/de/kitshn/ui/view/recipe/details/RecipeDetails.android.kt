package de.kitshn.ui.view.recipe.details

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity

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