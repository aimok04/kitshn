package de.kitshn.ui.component.shopping.chips

import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorRequestStateState
import de.kitshn.api.tandoor.model.shopping.TandoorSupermarket
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.cache.ShoppingSupermarketCache
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
    state: AdditionalShoppingSettingsChipRowState,
    cache: ShoppingSupermarketCache
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

        val supermarkets = remember { mutableStateListOf<TandoorSupermarket>() }
        LaunchedEffect(Unit) {
            requestState.wrapRequest {
                val mSupermarkets = client.supermarket.fetchAll()
                cache.update(mSupermarkets)

                supermarkets.clear()
                supermarkets.addAll(mSupermarkets)

                delay(500)
            }
        }

        LaunchedEffect(requestState.state) {
            if(requestState.state == TandoorRequestStateState.SUCCESS) return@LaunchedEffect
            if(requestState.state == TandoorRequestStateState.LOADING) delay(2000)

            if(supermarkets.isNotEmpty()) return@LaunchedEffect
            cache.retrieve()?.let { supermarkets.addAll(it) }
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
                LazyColumn(
                    Modifier
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    if(requestState.state == TandoorRequestStateState.LOADING && supermarkets.isEmpty()) {
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
                    } else {
                        items(supermarkets.size) {
                            ListItem(
                                modifier = Modifier.clickable {
                                    showDialog = false
                                    state.supermarket = supermarkets[it]

                                    state.update()
                                },
                                headlineContent = {
                                    Text(text = supermarkets[it].name)
                                },
                                trailingContent = {
                                    if(state.supermarket?.id != supermarkets[it].id) return@ListItem
                                    Icon(
                                        Icons.Rounded.Check,
                                        stringResource(Res.string.common_selected)
                                    )
                                }
                            )
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