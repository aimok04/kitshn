package de.kitshn.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import de.kitshn.api.funding.FundingApiClient
import de.kitshn.api.funding.FundingEvent
import kitshn.composeApp.BuildConfig
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_close
import kitshn.composeapp.generated.resources.action_support
import kitshn.composeapp.generated.resources.funding_banner_text
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AutoFetchingFundingBanner(
    modifier: Modifier = Modifier,
    onClickSupport: () -> Unit,
    onDismiss: () -> Unit
) {
    var event by remember { mutableStateOf<FundingEvent?>(null) }

    LaunchedEffect(Unit) {
        val state = FundingApiClient(BuildConfig.FUNDING_API)
            .state()

        if(state == null) return@LaunchedEffect
        event = state.events.find { it.isBanner() }
    }

    event?.let {
        FundingBanner(
            modifier = modifier,
            event = it,
            onClickSupport = onClickSupport,
            onDismiss = onDismiss
        )
    }
}

@Composable
fun FundingBanner(
    modifier: Modifier = Modifier,
    event: FundingEvent,
    onClickSupport: () -> Unit,
    onDismiss: () -> Unit
) {
    val density = LocalDensity.current

    val coroutineScope = rememberCoroutineScope()

    val offsetXAnim = remember { Animatable(-1f) }
    var offsetX by remember { mutableStateOf(999.dp) }

    LaunchedEffect(Unit) { offsetXAnim.animateTo(0f) }

    Surface(
        modifier
            .offset(x = offsetX * offsetXAnim.value)
            .onGloballyPositioned {
                offsetX = with(density) {
                    it.size.width.toDp()
                }
            },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.tertiary,
        shadowElevation = 16.dp
    ) {
        Column(
            Modifier.padding(
                top = 16.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Markdown(
                    modifier = Modifier,
                    content = pluralStringResource(
                        Res.plurals.funding_banner_text,
                        event.remainingSubscriptions,
                        event.remainingSubscriptions
                    )
                )

                event.additionalContent?.let {
                    Text(text = event.additionalContent)
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.height(40.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    modifier = Modifier.weight(1f, true),
                    onClick = onClickSupport,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    )
                ) {
                    Text(text = stringResource(Res.string.action_support))
                }

                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            offsetXAnim.animateTo(1f)
                            onDismiss()
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    )
                ) {
                    Icon(Icons.Rounded.Close, stringResource(Res.string.action_close))
                }
            }

            Spacer(Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { event.percentage },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.tertiary,
                trackColor = MaterialTheme.colorScheme.onTertiary
            )
        }
    }
}