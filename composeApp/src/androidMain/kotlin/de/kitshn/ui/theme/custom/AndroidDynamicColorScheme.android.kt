package de.kitshn.ui.theme.custom

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

actual fun isAndroidDynamicColorSchemeAvailable(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
actual fun generateAndroidDynamicColorSchemePreview(): List<Color> {
    val context = LocalContext.current

    return listOf(
        dynamicDarkColorScheme(context).primary,
        dynamicLightColorScheme(context).primary
    )
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
actual fun generateAndroidDynamicColorScheme(dark: Boolean): ColorScheme? {
    val context = LocalContext.current

    return if(dark) {
        dynamicDarkColorScheme(context)
    } else {
        dynamicLightColorScheme(context)
    }
}