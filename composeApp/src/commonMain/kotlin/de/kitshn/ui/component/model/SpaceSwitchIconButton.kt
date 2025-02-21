package de.kitshn.ui.component.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.ui.dialog.SpaceSwitchDialog
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_switch_space
import kitshn.composeapp.generated.resources.common_learn_more
import kitshn.composeapp.generated.resources.common_not_supported
import kitshn.composeapp.generated.resources.common_okay
import kitshn.composeapp.generated.resources.error_space_switching_not_supported_with_api_token
import org.jetbrains.compose.resources.stringResource

@Composable
fun SpaceSwitchIconButton(
    client: TandoorClient?,
    onRefresh: () -> Unit
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    val isSupported =
        client?.credentials?.cookie != null || (client?.credentials?.password ?: "").isNotBlank()

    IconButton(
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = if(isSupported) {
                IconButtonDefaults.iconButtonColors().contentColor
            } else {
                IconButtonDefaults.iconButtonColors().disabledContentColor
            }
        ),
        onClick = { showDialog = true }
    ) {
        Icon(Icons.Rounded.SwapHoriz, stringResource(Res.string.action_switch_space))
    }

    if(showDialog) if(isSupported) {
        SpaceSwitchDialog(
            client = client,
            onRefresh = onRefresh,
            onDismiss = {
                showDialog = false
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
            },
            icon = { Icon(Icons.Rounded.Block, stringResource(Res.string.common_not_supported)) },
            title = { Text(stringResource(Res.string.common_not_supported)) },
            text = {
                Text(buildAnnotatedString {
                    append(stringResource(Res.string.error_space_switching_not_supported_with_api_token))
                    append("\n\n")
                    append("${stringResource(Res.string.common_learn_more)} — ")

                    withStyle(
                        SpanStyle(
                            textDecoration = TextDecoration.Underline,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    ) {
                        withLink(LinkAnnotation.Url("https://github.com/aimok04/kitshn/issues/59")) {
                            append("issue/#59")
                        }
                    }
                })
            },
            confirmButton = {
                Button(
                    onClick = { showDialog = false }
                ) {
                    Text(stringResource(Res.string.common_okay))
                }
            }
        )
    }
}