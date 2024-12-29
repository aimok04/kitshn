package de.kitshn.ui.route.onboarding

import androidx.compose.runtime.Composable

// needed for jvm implementation
@Composable
actual fun InitializeWebView(
    onInitialized: () -> Unit
) {
    onInitialized()
}