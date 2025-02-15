package de.kitshn.ui.component.shopping.chips

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorRequestStateState
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.ui.component.alert.LoadingErrorAlertPaneWrapper
import de.kitshn.ui.component.shopping.AdditionalShoppingSettingsChipRowState
import de.kitshn.ui.modifier.loadingPlaceHolder
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_remove
import kitshn.composeapp.generated.resources.common_select
import kitshn.composeapp.generated.resources.common_selected
import kitshn.composeapp.generated.resources.common_supermarket
import kitshn.composeapp.generated.resources.lorem_ipsum_short
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

@Composable
fun SupermarketSettingChip(
    client: TandoorClient,
    state: AdditionalShoppingSettingsChipRowState
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }

    FilterChip(
        selected = state.supermarket != null,
        onClick = {
            showDialog = true
        },
        label = {
            Text(text = buildAnnotatedString {
                append(stringResource(Res.string.common_supermarket))

                state.supermarket?.let {
                    append(": ${it.name}")
                }
            })
        },
        trailingIcon = {
            Icon(Icons.Rounded.ArrowDropDown, stringResource(Res.string.common_select))
        }
    )

    if(showDialog) {
        val requestState = rememberTandoorRequestState()
        LaunchedEffect(Unit) {
            requestState.wrapRequest {
                client.supermarket.fetch()
                delay(500)
            }
        }

        AlertDialog(
            modifier = Modifier.padding(top = 24.dp, bottom = 24.dp),
            onDismissRequest = {
                showDialog = false
            },
            icon = {
                Icon(Icons.Rounded.Storefront, stringResource(Res.string.common_supermarket))
            },
            title = {
                Text(text = stringResource(Res.string.common_supermarket))
            },
            text = {
                LoadingErrorAlertPaneWrapper(
                    modifier = Modifier.padding(16.dp),
                    alertPaneModifier = Modifier.fillMaxWidth(),
                    loadingState = requestState.state.toErrorLoadingSuccessState()
                ) {
                    LazyColumn(
                        Modifier
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        when(requestState.state) {
                            TandoorRequestStateState.LOADING -> {
                                items(2) {
                                    ListItem(
                                        headlineContent = {
                                            Text(
                                                text = stringResource(Res.string.lorem_ipsum_short),
                                                Modifier.loadingPlaceHolder(requestState.state.toErrorLoadingSuccessState())
                                            )
                                        },
                                    )
                                }
                            }

                            else -> {
                                items(client.container.supermarkets.size) {
                                    val supermarket = client.container.supermarkets[it]

                                    ListItem(
                                        modifier = Modifier.clickable {
                                            showDialog = false
                                            state.supermarket = supermarket

                                            state.update()
                                        },
                                        headlineContent = {
                                            Text(text = supermarket.name)
                                        },
                                        trailingContent = {
                                            if(state.supermarket?.id != supermarket.id) return@ListItem
                                            Icon(
                                                Icons.Rounded.Check,
                                                stringResource(Res.string.common_selected)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            dismissButton = {
                if(state.supermarket != null) FilledTonalButton(onClick = {
                    showDialog = false
                    state.supermarket = null

                    state.update()
                }) {
                    Text(text = stringResource(Res.string.action_remove))
                }
            },
            confirmButton = { }
        )
    }
}