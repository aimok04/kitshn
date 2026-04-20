package de.kitshn.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
internal actual fun SystemAppearance(isDark: Boolean) {
}

@Composable
internal actual fun overrideColorScheme(darkTheme: Boolean, dynamicColor: Boolean): ColorScheme? {
    return null
}

actual fun isDynamicColorSupported() = false