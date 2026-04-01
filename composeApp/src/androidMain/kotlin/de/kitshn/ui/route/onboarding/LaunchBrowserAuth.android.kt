package de.kitshn.ui.route.onboarding

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import co.touchlab.kermit.Logger

@Composable
actual fun LaunchBrowserAuthEffect(
    launch: Boolean,
    instanceUrl: String,
    onLaunched: () -> Unit,
    onFallbackToWebView: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(launch) {
        if (!launch) return@LaunchedEffect
        try {
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
            customTabsIntent.launchUrl(context, Uri.parse(instanceUrl))
            onLaunched()
        } catch (e: Exception) {
            Logger.e("LaunchBrowserAuth.android.kt", e)
            onFallbackToWebView()
        }
    }
}
