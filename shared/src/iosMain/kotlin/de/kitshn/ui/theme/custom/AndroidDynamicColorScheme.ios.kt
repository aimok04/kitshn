package de.kitshn.ui.theme.custom

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

actual fun isAndroidDynamicColorSchemeAvailable(): Boolean = false

@Composable
actual fun generateAndroidDynamicColorSchemePreview(): List<Color> = listOf()

@Composable
actual fun generateAndroidDynamicColorScheme(dark: Boolean): ColorScheme? = null