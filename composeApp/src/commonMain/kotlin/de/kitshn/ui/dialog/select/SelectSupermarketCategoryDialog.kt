package de.kitshn.ui.dialog.select

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorRequestStateState
import de.kitshn.api.tandoor.model.shopping.TandoorSupermarketCategory
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.ui.component.alert.LoadingErrorAlertPaneWrapper
import de.kitshn.ui.component.input.AlwaysDockedSearchBar
import de.kitshn.ui.modifier.fullWidthAlertDialogPadding
import de.kitshn.ui.modifier.loadingPlaceHolder
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_abort
import kitshn.composeapp.generated.resources.action_add
import kitshn.composeapp.generated.resources.common_category
import kitshn.composeapp.generated.resources.common_create_argument
import kitshn.composeapp.generated.resources.common_selected
import kitshn.composeapp.generated.resources.lorem_ipsum_short
import kitshn.composeapp.generated.resources.search_categories
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

@Composable
fun rememberSelectSupermarketCategoryDialogState(): SelectSupermarketCategoryDialogState {
    return remember {
        SelectSupermarketCategoryDialogState()
    }
}

class SelectSupermarketCategoryDialogState(
    val shown: MutableState<Boolean> = mutableStateOf(false)
) {
    var category by mutableStateOf<TandoorSupermarketCategory?>(null)

    fun open(category: TandoorSupermarketCategory?) {
        this.category = category
        this.shown.value = true
    }

    fun dismiss() {
        this.shown.value = false
    }
}

@Composable
fun SelectSupermarketCategoryDialog(
    client: TandoorClient,
    state: SelectSupermarketCategoryDialogState,
    onSubmit: (category: TandoorSupermarketCategory?) -> Unit
) {
    if(!state.shown.value) return

    val submit: (category: TandoorSupermarketCategory?) -> Unit = {
        it?.let { it1 -> onSubmit(it1) }
        state.dismiss()
    }

    AlertDialog(
        modifier = Modifier.fullWidthAlertDialogPadding(),
        onDismissRequest = {
            state.dismiss()
        },
        icon = {
            Icon(Icons.Rounded.Category, stringResource(Res.string.common_category))
        },
        title = {
            Text(stringResource(Res.string.common_category))
        },
        text = {
            SupermarketCategorySearchBar(
                client = client,
                selected = state.category,
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
fun SupermarketCategorySearchBar(
    client: TandoorClient,
    selected: TandoorSupermarketCategory?,
    onSelect: (category: TandoorSupermarketCategory?) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    val keyboardController = LocalSoftwareKeyboardController.current
    val fetchRequestState = rememberTandoorRequestState()

    var query by rememberSaveable { mutableStateOf("") }

    val categories = remember { mutableStateListOf<TandoorSupermarketCategory>() }
    val searchResults = remember { mutableStateListOf<TandoorSupermarketCategory>() }

    LaunchedEffect(categories.toList(), query) {
        searchResults.clear()
        searchResults.addAll(
            categories.filter { it.name.lowercase().contains(query.lowercase()) }
        )
    }

    LaunchedEffect(Unit) {
        fetchRequestState.wrapRequest {
            categories.addAll(client.supermarket.fetchCategories())
        }
    }

    AlwaysDockedSearchBar(
        colors = SearchBarDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            dividerColor = Color.Transparent
        ),
        inputField = {
            SearchBarDefaults.InputField(
                modifier = Modifier.focusRequester(focusRequester),
                query = query,
                onQueryChange = { query = it },
                onSearch = { keyboardController?.hide() },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Search,
                        stringResource(Res.string.search_categories)
                    )
                },
                placeholder = { Text(stringResource(Res.string.search_categories)) },
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
                        if(query.isNotBlank() && searchResults.find {
                                it.name.lowercase() == query.trimEnd(
                                    ' '
                                ).lowercase()
                            } == null) {
                            item {
                                ListItem(
                                    modifier = Modifier.clickable {
                                        onSelect(
                                            TandoorSupermarketCategory(
                                                name = query.trimEnd(' '),
                                                id = null
                                            )
                                        )
                                    },
                                    leadingContent = {
                                        IconButton(
                                            onClick = {
                                                onSelect(
                                                    TandoorSupermarketCategory(
                                                        name = query.trimEnd(' '),
                                                        id = null
                                                    )
                                                )
                                            }
                                        ) {
                                            Icon(
                                                Icons.Rounded.Add,
                                                stringResource(Res.string.action_add)
                                            )
                                        }
                                    },
                                    headlineContent = {
                                        Text(
                                            stringResource(
                                                Res.string.common_create_argument,
                                                query
                                            )
                                        )
                                    }
                                )
                            }
                        }

                        items(searchResults.size, key = { searchResults[it].id ?: -1 }) {
                            val category = searchResults[it]

                            ListItem(
                                modifier = Modifier.clickable {
                                    onSelect(category)
                                },
                                headlineContent = {
                                    Text(text = category.name)
                                },
                                trailingContent = {
                                    if(category.id != selected?.id) return@ListItem

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
        focusRequester.requestFocus()
    }
}