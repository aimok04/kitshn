package de.kitshn.ui.route.onboarding

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import de.kitshn.R
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorCredentials
import de.kitshn.ui.component.alert.FullSizeAlertPane
import de.kitshn.ui.component.buttons.BackButton
import de.kitshn.ui.component.buttons.BackButtonType
import de.kitshn.ui.route.RouteParameters
import kotlinx.coroutines.launch
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalEncodingApi::class)
@Composable
fun RouteOnboardingSignInBrowser(
    p: RouteParameters
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val instanceUrlEncoded = p.bse.arguments?.getString("instanceUrl")
    if(instanceUrlEncoded == null) {
        FullSizeAlertPane(
            imageVector = Icons.Rounded.ErrorOutline,
            contentDescription = stringResource(R.string.error),
            text = stringResource(R.string.error)
        )

        return
    }

    val instanceUrl = Base64.decode(instanceUrlEncoded).decodeToString()

    LaunchedEffect(Unit) { CookieManager.getInstance().removeAllCookies { } }

    val webViewState = rememberWebViewState(url = instanceUrl)
    val client = remember {
        object : AccompanistWebViewClient() {
            override fun onPageFinished(view: WebView, url: String?) {
                super.onPageFinished(view, url)

                try {
                    // check if user is authenticated
                    val cookies: String = CookieManager.getInstance().getCookie(instanceUrl)

                    val credentials = TandoorCredentials(
                        instanceUrl = instanceUrl,
                        cookie = cookies
                    )

                    val client = TandoorClient(context, credentials)

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
                } catch(e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.onboarding_sign_in_title)) },
                navigationIcon = {
                    BackButton(
                        onBack = { p.vm.navHostController?.popBackStack() },
                        type = BackButtonType.CLOSE
                    )
                }
            )
        }
    ) {
        Box(
            Modifier
                .padding(it)
                .clip(RoundedCornerShape(32.dp))
                .fillMaxSize()
        ) {
            WebView(
                modifier = Modifier.fillMaxSize(),
                state = webViewState,
                onCreated = { webview ->
                    webview.clearCache(true)

                    val settings = webview.settings
                    settings.javaScriptCanOpenWindowsAutomatically = true
                    settings.setSupportMultipleWindows(true)
                    settings.javaScriptEnabled = true
                    settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
                },
                client = client
            )
        }
    }
}