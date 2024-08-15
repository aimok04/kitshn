package de.kitshn.android.ui.component.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import de.kitshn.android.ui.theme.KitshnYellow

@Composable
fun KitshnLogoAnimationWrapper(
    onCompleted: () -> Unit = { },
    content: @Composable (modifier: Modifier, tint: Color) -> Unit
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1.8f, tween(1250))
        onCompleted()
    }

    val animFactor = if(progress.value > 1.4f) (1f + (1.8f - progress.value)) else progress.value
    val animTintFactor = if(progress.value > 1.4f) 1f else progress.value / 1.4f

    val ctnColor = LocalContentColor.current
    val redDiff = KitshnYellow.red - ctnColor.red
    val blueDiff = KitshnYellow.blue - ctnColor.blue
    val greenDiff = KitshnYellow.green - ctnColor.green

    val iconTint = Color(
        ctnColor.red + (redDiff * animTintFactor),
        ctnColor.green + (greenDiff * animTintFactor),
        ctnColor.blue + (blueDiff * animTintFactor)
    )

    content(
        Modifier
            .scale(animFactor)
            .rotate(180 + (180 * animFactor)), iconTint
    )
}