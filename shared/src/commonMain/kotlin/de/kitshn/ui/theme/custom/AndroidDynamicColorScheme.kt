package de.kitshn.ui.theme.custom

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

expect fun isAndroidDynamicColorSchemeAvailable(): Boolean

@Composable
expect fun generateAndroidDynamicColorSchemePreview(): List<Color>

@Composable
expect fun generateAndroidDynamicColorScheme(dark: Boolean): ColorScheme?