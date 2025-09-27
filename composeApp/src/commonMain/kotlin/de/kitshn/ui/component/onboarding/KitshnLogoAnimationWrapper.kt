@file:OptIn(ExperimentalTime::class)

package de.kitshn.ui.component.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
fun KitshnLogoAnimationWrapper(
    onCompleted: () -> Unit = { },
    content: @Composable (modifier: Modifier) -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current

    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1.8f, tween(1250))
        onCompleted()
    }

    var timeout by remember { mutableLongStateOf(0L) }
    LaunchedEffect(progress.value) {
        if(progress.value > 1.0f) return@LaunchedEffect

        val diff = Clock.System.now().toEpochMilliseconds() - timeout
        if(diff < 50L) return@LaunchedEffect

        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
        timeout = Clock.System.now().toEpochMilliseconds()
    }

    var done by remember { mutableStateOf(false) }
    LaunchedEffect(progress.value) {
        if(done) return@LaunchedEffect
        if(progress.value < 1.2f) return@LaunchedEffect

        hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
        done = true
    }

    val animFactor = if(progress.value > 1.4f) (1f + (1.8f - progress.value)) else progress.value

    content(
        Modifier
            .scale(animFactor)
            .rotate(180 + (180 * animFactor))
    )
}