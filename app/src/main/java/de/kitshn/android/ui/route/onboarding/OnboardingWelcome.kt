package de.kitshn.android.ui.route.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.kitshn.android.R
import de.kitshn.android.ui.component.onboarding.KitshnLogoAnimationWrapper
import de.kitshn.android.ui.route.RouteParameters
import de.kitshn.android.ui.theme.KitshnYellowBright
import de.kitshn.android.ui.theme.KitshnYellowDark
import de.kitshn.android.ui.theme.Typography
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RouteOnboardingWelcome(
    p: RouteParameters
) {
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                coroutineScope.launch {
                    p.vm.settings.setOnboardingCompleted(true)
                }

                p.vm.navHostController?.navigate("main") {
                    popUpTo("main") {
                        inclusive = true
                    }
                }
            }) {
                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    stringResource(id = R.string.action_next)
                )
            }
        }
    ) {
        LazyColumn(
            Modifier
                .padding(it)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Column(
                    Modifier
                        .padding(24.dp)
                        .widthIn(100.dp, 400.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    KitshnLogoAnimationWrapper { modifier, tint ->
                        Text(
                            modifier = modifier,
                            text = "\uD83D\uDE03",
                            style = Typography.displayLarge
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Column {
                        FlowRow {
                            Text(
                                text = stringResource(R.string.common_welcome),
                                style = Typography.displaySmall
                            )

                            Spacer(Modifier.width(8.dp))

                            Text(
                                text = p.vm.tandoorClient?.credentials?.username ?: "",
                                style = Typography.displaySmall.copy(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            KitshnYellowBright,
                                            KitshnYellowDark
                                        )
                                    )
                                )
                            )
                        }

                        Text(
                            text = stringResource(R.string.onboarding_welcome)
                        )
                    }
                }
            }
        }
    }
}