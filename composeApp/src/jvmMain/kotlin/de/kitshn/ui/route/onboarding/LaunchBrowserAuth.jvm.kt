package de.kitshn.ui.route.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun LaunchBrowserAuthEffect(
    launch: Boolean,
    instanceUrl: String,
    onLaunched: () -> Unit,
    onFallbackToWebView: () -> Unit
) {
    LaunchedEffect(launch) {
        if (!launch) return@LaunchedEffect
        onFallbackToWebView()
    }
}
