package de.kitshn.ui.modifier

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import de.kitshn.Platforms
import de.kitshn.platformDetails

@Composable
fun Modifier.hideOnAndroid(
    hide: Boolean
): Modifier {
    if(!hide) return this
    if (platformDetails.platform != Platforms.ANDROID) return this
    return height(0.dp).alpha(0f)
}