package de.kitshn.ui.dialog.select

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Flare
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import co.touchlab.kermit.Logger
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorRequestStateState
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.api.tandoor.route.TandoorAIProvider
import de.kitshn.ui.component.alert.LoadingErrorAlertPaneWrapper
import de.kitshn.ui.component.input.AlwaysDockedSearchBar
import de.kitshn.ui.component.input.iosKeyboardWorkaround.InputFieldWithIOSKeyboardWorkaround
import de.kitshn.ui.modifier.fullWidthAlertDialogPadding
import de.kitshn.ui.modifier.loadingPlaceHolder
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_abort
import kitshn.composeapp.generated.resources.common_ai_provider
import kitshn.composeapp.generated.resources.common_selected
import kitshn.composeapp.generated.resources.lorem_ipsum_short
import kitshn.composeapp.generated.resources.search_providers
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import org.jetbrains.compose.resources.stringResource

@Composable
fun rememberSelectAIProviderDialogState(): SelectAIProviderDialogState {
    return remember {
        SelectAIProviderDialogState()
    }
}

class SelectAIProviderDialogState(
    val shown: MutableState<Boolean> = mutableStateOf(false)
) {
    var aiProvider by mutableStateOf<TandoorAIProvider?>(null)

    fun open(aiProvider: TandoorAIProvider?) {
        this.aiProvider = aiProvider
        this.shown.value = true
    }

    fun dismiss() {
        this.shown.value = false
    }
}

@Composable
fun SelectAIProviderDialog(
    client: TandoorClient,
    state: SelectAIProviderDialogState,
    onSubmit: (aiProvider: TandoorAIProvider?) -> Unit
) {
    if(!state.shown.value) return

    val submit: (aiProvider: TandoorAIProvider?) -> Unit = {
        it?.let { it1 -> onSubmit(it1) }
        state.dismiss()
    }

    AlertDialog(
        modifier = Modifier.fullWidthAlertDialogPadding(),
        onDismissRequest = {
            state.dismiss()
        },
        icon = {
            Icon(Icons.Rounded.Flare, stringResource(Res.string.common_ai_provider))
        },
        title = {
            Text(stringResource(Res.string.common_ai_provider))
        },
        text = {
            AIProviderSearchBar(
                client = client,
                selected = state.aiProvider,
                onSelect = submit
            )
        },
        confirmButton = {
            FilledTonalButton(onClick = {
                submit(null)
            }) {
                Text(stringResource(Res.string.action_abort))
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIProviderSearchBar(
    client: TandoorClient,
    selected: TandoorAIProvider?,
    onSelect: (aiProvider: TandoorAIProvider?) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    val keyboardController = LocalSoftwareKeyboardController.current
    val fetchRequestState = rememberTandoorRequestState()

    var query by rememberSaveable { mutableStateOf("") }

    val aiProviders = remember { mutableStateListOf<TandoorAIProvider>() }
    val searchResults = remember { mutableStateListOf<TandoorAIProvider>() }

    LaunchedEffect(aiProviders.toList(), query) {
        searchResults.clear()
        searchResults.addAll(
            aiProviders.filter { it.name.lowercase().contains(query.lowercase()) }
        )
    }

    LaunchedEffect(Unit) {
        fetchRequestState.wrapRequest {
            aiProviders.addAll(client.aiProvider.fetch())
        }
    }

    AlwaysDockedSearchBar(
        colors = SearchBarDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            dividerColor = Color.Transparent
        ),
        inputField = {
            SearchBarDefaults.InputFieldWithIOSKeyboardWorkaround(
                modifier = Modifier.focusRequester(focusRequester),
                query = query,
                onQueryChange = { query = it },
                onSearch = { keyboardController?.hide() },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Search,
                        stringResource(Res.string.search_providers)
                    )
                },
                placeholder = { Text(stringResource(Res.string.search_providers)) },
                expanded = true,
                onExpandedChange = { }
            )
        }
    ) {
        LoadingErrorAlertPaneWrapper(
            modifier = Modifier.padding(16.dp),
            alertPaneModifier = Modifier.fillMaxWidth(),
            loadingState = fetchRequestState.state.toErrorLoadingSuccessState()
        ) {
            LazyColumn(
                Modifier
                    .clip(RoundedCornerShape(16.dp))
            ) {
                when(fetchRequestState.state) {
                    TandoorRequestStateState.LOADING -> {
                        items(5) {
                            ListItem(
                                headlineContent = {
                                    Text(
                                        text = stringResource(Res.string.lorem_ipsum_short),
                                        modifier = Modifier.loadingPlaceHolder(fetchRequestState.state.toErrorLoadingSuccessState())
                                    )
                                }
                            )
                        }
                    }

                    else -> {
                        items(searchResults.size, key = { searchResults[it].id }) {
                            val aiProvider = searchResults[it]

                            ListItem(
                                modifier = Modifier.clickable {
                                    onSelect(aiProvider)
                                },
                                headlineContent = {
                                    Text(text = aiProvider.name)
                                },
                                trailingContent = {
                                    if(aiProvider.id != selected?.id) return@ListItem

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
    }

    LaunchedEffect(Unit) {
        delay(200)
        this.coroutineContext.job.invokeOnCompletion {
            try {
                focusRequester.requestFocus()
            } catch(e: Exception) {
                Logger.e("SelectAIProviderDialog.kt", e)
            }
        }
    }
}