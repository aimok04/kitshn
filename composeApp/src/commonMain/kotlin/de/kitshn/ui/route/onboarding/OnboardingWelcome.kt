package de.kitshn.ui.route.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MediumFloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.TandoorRequestState
import de.kitshn.ui.component.onboarding.KitshnLogoAnimationWrapper
import de.kitshn.ui.randomBackgroundShape
import de.kitshn.ui.route.RouteParameters
import de.kitshn.ui.theme.KitshnYellowBright
import de.kitshn.ui.theme.KitshnYellowDark
import de.kitshn.ui.theme.Typography
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_next
import kitshn.composeapp.generated.resources.common_welcome
import kitshn.composeapp.generated.resources.onboarding_welcome
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RouteOnboardingWelcome(
    p: RouteParameters
) {
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        TandoorRequestState().wrapRequest {
            val user = p.vm.tandoorClient?.user?.get()
            if(user != null) p.vm.uiState.userDisplayName = user.display_name
        }
    }

    Scaffold(
        floatingActionButton = {
            MediumFloatingActionButton(onClick = {
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
                    stringResource(Res.string.action_next)
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
                    KitshnLogoAnimationWrapper { modifier, _ ->
                        Box(
                            modifier
                                .size(164.dp)
                                .background(
                                    LoadingIndicatorDefaults.containedContainerColor,
                                    remember {
                                        MaterialShapes.randomBackgroundShape()
                                    }.toShape(0)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                modifier = modifier,
                                text = "\uD83D\uDE03",
                                style = Typography().displayLarge
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Column {
                        FlowRow {
                            Text(
                                text = stringResource(Res.string.common_welcome),
                                style = Typography().displaySmall
                            )

                            Spacer(Modifier.width(8.dp))

                            Text(
                                text = p.vm.uiState.userDisplayName.ifBlank {
                                    p.vm.tandoorClient?.credentials?.username ?: ""
                                },
                                style = Typography().displaySmall.copy(
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
                            text = stringResource(Res.string.onboarding_welcome)
                        )
                    }
                }
            }
        }
    }
}