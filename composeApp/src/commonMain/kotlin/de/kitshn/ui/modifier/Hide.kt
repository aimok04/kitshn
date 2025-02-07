package de.kitshn.ui.modifier

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.hide(
    hide: Boolean
): Modifier {
    if(!hide) return this
    return height(0.dp).alpha(0f)
}