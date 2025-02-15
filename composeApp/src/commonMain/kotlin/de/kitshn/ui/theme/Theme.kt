package de.kitshn.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import de.kitshn.ui.theme.custom.AvailableColorSchemes

internal val LocalThemeIsDark = compositionLocalOf { mutableStateOf(true) }

@Composable
internal fun KitshnTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorScheme: AvailableColorSchemes = AvailableColorSchemes.getDefault(),
    customColorSchemeSeed: Color = Color.Yellow,
    content: @Composable () -> Unit
) {
    val isDarkState = remember(darkTheme) { mutableStateOf(darkTheme) }
    CompositionLocalProvider(
        LocalThemeIsDark provides isDarkState
    ) {
        val isDark by isDarkState
        SystemAppearance(!isDark)

        MaterialTheme(
            colorScheme = colorScheme.generate(isDark, customColorSchemeSeed)!!,
            typography = Typography(),
            content = { Surface(content = content) }
        )
    }
}

@Composable
internal expect fun SystemAppearance(isDark: Boolean)

@Composable
internal expect fun overrideColorScheme(darkTheme: Boolean, dynamicColor: Boolean): ColorScheme?

expect fun isDynamicColorSupported(): Boolean