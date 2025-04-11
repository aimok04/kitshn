package de.kitshn.ui.route.alerts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Dangerous
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import de.kitshn.Platforms
import de.kitshn.platformDetails
import de.kitshn.ui.route.RouteParameters
import de.kitshn.ui.theme.Typography
import kitshn.composeApp.BuildConfig
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_sign_out
import kitshn.composeapp.generated.resources.error_outdated_v1_client_android
import kitshn.composeapp.generated.resources.error_outdated_v1_client_description
import kitshn.composeapp.generated.resources.error_outdated_v1_client_ios
import kitshn.composeapp.generated.resources.error_outdated_v1_client_title
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteAlertOutdatedV1Client(
    p: RouteParameters
) {
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { }
            )
        }
    ) { pv ->
        Box(
            Modifier
                .padding(pv)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LazyColumn(
                Modifier
                    .fillMaxWidth(0.7f)
                    .widthIn(max = 600.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Icon(
                        modifier = Modifier
                            .width(64.dp)
                            .height(64.dp),
                        imageVector = Icons.Rounded.Dangerous,
                        contentDescription = stringResource(Res.string.error_outdated_v1_client_title),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = stringResource(Res.string.error_outdated_v1_client_title),
                        style = Typography().displaySmall
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = stringResource(Res.string.error_outdated_v1_client_description)
                    )

                    Spacer(Modifier.height(16.dp))

                    Markdown(
                        modifier = Modifier,
                        content = when(platformDetails.platform) {
                            Platforms.ANDROID -> stringResource(
                                Res.string.error_outdated_v1_client_android,
                                BuildConfig.BETA_URL_GOOGLE,
                                BuildConfig.BETA_URL_GITHUB
                            )

                            Platforms.IOS -> stringResource(
                                Res.string.error_outdated_v1_client_ios,
                                BuildConfig.BETA_URL_TESTFLIGHT
                            )

                            else -> ""
                        },
                        typography = markdownTypography(
                            link = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                textDecoration = TextDecoration.Underline,
                                color = MaterialTheme.colorScheme.primary
                            )
                        ),
                        colors = markdownColor(
                            linkText = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(Modifier.height(64.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                p.vm.settings.setOnboardingCompleted(false)
                                p.vm.settings.saveTandoorCredentials(null)

                                delay(100)
                                p.vm.resetApp()
                            }
                        }
                    ) {
                        Text(text = stringResource(Res.string.action_sign_out))
                    }
                }
            }
        }
    }
}