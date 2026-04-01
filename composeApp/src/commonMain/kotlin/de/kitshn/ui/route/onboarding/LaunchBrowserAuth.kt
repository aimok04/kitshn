package de.kitshn.ui.route.onboarding

import androidx.compose.runtime.Composable

/**
 * Platform-specific browser auth launcher.
 *
 * On Android: launches a Chrome Custom Tab to [instanceUrl].
 * On other platforms: calls [onFallbackToWebView] so the caller can
 * navigate to the in-app WebView route instead.
 *
 * @param launch  set to true to trigger the launch
 * @param instanceUrl  the Tandoor instance URL to authenticate against
 * @param onLaunched  called after the Custom Tab has been launched (Android)
 * @param onFallbackToWebView  called on platforms that don't support Custom Tabs
 */
@Composable
expect fun LaunchBrowserAuthEffect(
    launch: Boolean,
    instanceUrl: String,
    onLaunched: () -> Unit,
    onFallbackToWebView: () -> Unit
)
