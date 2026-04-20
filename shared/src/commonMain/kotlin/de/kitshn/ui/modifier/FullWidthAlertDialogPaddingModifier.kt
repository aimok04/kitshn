package de.kitshn.ui.modifier

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.fullWidthAlertDialogPadding(): Modifier {
    val additionalPadding = additionalPadding()
    val layoutDirection = LocalLayoutDirection.current

    return Modifier.padding(
        start = 24.dp + additionalPadding.calculateStartPadding(layoutDirection),
        end = 24.dp + additionalPadding.calculateEndPadding(layoutDirection),
        top = 24.dp + additionalPadding.calculateTopPadding(),
        bottom = 24.dp + additionalPadding.calculateBottomPadding()
    )
}

@Composable
internal expect fun additionalPadding(): PaddingValues