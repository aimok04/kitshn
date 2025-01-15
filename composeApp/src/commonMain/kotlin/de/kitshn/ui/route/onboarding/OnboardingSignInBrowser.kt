package de.kitshn.ui.route.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import de.kitshn.Platforms
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorCredentials
import de.kitshn.platformDetails
import de.kitshn.ui.component.alert.FullSizeAlertPane
import de.kitshn.ui.component.buttons.BackButton
import de.kitshn.ui.component.buttons.BackButtonType
import de.kitshn.ui.route.RouteParameters
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.error
import kitshn.composeapp.generated.resources.onboarding_sign_in_ios_social_login_unsupported
import kitshn.composeapp.generated.resources.onboarding_sign_in_title
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalEncodingApi::class)
@Composable
fun RouteOnboardingSignInBrowser(
    p: RouteParameters
) {
    val coroutineScope = rememberCoroutineScope()

    val instanceUrlEncoded = p.bse.arguments?.getString("instanceUrl")
    if(instanceUrlEncoded == null) {
        FullSizeAlertPane(
            imageVector = Icons.Rounded.ErrorOutline,
            contentDescription = stringResource(Res.string.error),
            text = stringResource(Res.string.error)
        )

        return
    }

    val instanceUrl = Base64.decode(instanceUrlEncoded).decodeToString()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.onboarding_sign_in_title)) },
                navigationIcon = {
                    BackButton(
                        onBack = { p.vm.navHostController?.popBackStack() },
                        type = BackButtonType.CLOSE
                    )
                }
            )
        }
    ) {
        var initialized by remember { mutableStateOf(false) }
        InitializeWebView { initialized = true }

        if(initialized) {
            Column(
                Modifier
                    .padding(it)
                    .clip(RoundedCornerShape(32.dp))
                    .fillMaxSize()
            ) {
                if(platformDetails.platform == Platforms.JVM) Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(16.dp),
                    text = "This feature might be broken on the current operating system.",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )

                val webViewNavigator = rememberWebViewNavigator()
                val webViewState = rememberWebViewState(url = instanceUrl)
                webViewState.webSettings.apply {
                    isJavaScriptEnabled = true
                    androidWebSettings.safeBrowsingEnabled = false
                }

                LaunchedEffect(Unit) {
                    webViewState.cookieManager.removeAllCookies()
                }

                LaunchedEffect(webViewState.loadingState) {
                    // needed for iOS because app gets denied (reason: https://developer.apple.com/app-store/review/guidelines/#login-services and https://developer.apple.com/app-store/review/guidelines/#data-collection-and-storage)
                    if(platformDetails.platform == Platforms.IOS) {
                        if(webViewState.lastLoadedUrl?.contains("accounts/signup") == true) {
                            webViewNavigator.loadUrl(url = instanceUrl)
                        } else {
                            webViewNavigator.evaluateJavaScript(
                                "document.querySelector(`a[href=\"/accounts/signup/\"]`).remove(); document.querySelector(\".socialaccount_providers\").parentElement.innerText = \"${
                                    getString(
                                        Res.string.onboarding_sign_in_ios_social_login_unsupported
                                    )
                                }\";"
                            )
                        }
                    }

                    if(webViewState.loadingState !is LoadingState.Finished) return@LaunchedEffect
                    delay(300)

                    // check if user is authenticated
                    val cookies = webViewState.cookieManager.getCookies(instanceUrl)
                        .joinToString(separator = " ")
                    val credentials = TandoorCredentials(
                        instanceUrl = instanceUrl,
                        cookie = cookies
                    )

                    val client = TandoorClient(credentials)

                    coroutineScope.launch {
                        val result = client
                            .testConnection(ignoreAuth = false)

                        if(!result) return@launch

                        p.vm.tandoorClient = client

                        val user = p.vm.tandoorClient!!.user.get()
                        if(user != null) {
                            credentials.username = user.display_name
                            p.vm.settings.saveTandoorCredentials(credentials)

                            p.vm.navHostController?.navigate("onboarding/welcome")
                        }
                    }
                }

                WebView(
                    modifier = Modifier.fillMaxSize(),
                    state = webViewState,
                    navigator = webViewNavigator
                )
            }
        }
    }
}

// needed for jvm implementation
@Composable
expect fun InitializeWebView(
    onInitialized: () -> Unit
)