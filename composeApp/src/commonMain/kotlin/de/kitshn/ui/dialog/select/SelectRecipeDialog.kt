package de.kitshn.ui.dialog.select

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.recipe.TandoorRecipeOverview
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.api.tandoor.route.TandoorRecipeQueryParameters
import de.kitshn.ui.component.model.recipe.HorizontalRecipeCardLink
import de.kitshn.ui.modifier.fullWidthAlertDialogPadding
import de.kitshn.ui.view.home.search.HOME_SEARCH_PAGING_SIZE
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_abort
import kitshn.composeapp.generated.resources.search_recipes
import kitshn.composeapp.generated.resources.select_recipe
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

@Composable
fun rememberSelectRecipeDialogState(): SelectRecipeDialogState {
    return remember {
        SelectRecipeDialogState()
    }
}

class SelectRecipeDialogState(
    val shown: MutableState<Boolean> = mutableStateOf(false)
) {
    fun open() {
        this.shown.value = true
    }

    fun dismiss() {
        this.shown.value = false
    }
}

@Composable
fun SelectRecipeDialog(
    client: TandoorClient,
    state: SelectRecipeDialogState,
    onSubmit: (recipeOverview: TandoorRecipeOverview) -> Unit
) {
    if(!state.shown.value) return

    val submit: (recipeOverview: TandoorRecipeOverview?) -> Unit = {
        it?.let { it1 -> onSubmit(it1) }
        state.dismiss()
    }

    AlertDialog(
        modifier = Modifier.fullWidthAlertDialogPadding(),
        onDismissRequest = {
            state.dismiss()
        },
        icon = {
            Icon(Icons.Rounded.Receipt, stringResource(Res.string.select_recipe))
        },
        title = {
            Text(stringResource(Res.string.select_recipe))
        },
        text = {
            RecipeSearchBar(
                client = client,
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
fun RecipeSearchBar(
    client: TandoorClient,
    onSelect: (recipeOverview: TandoorRecipeOverview) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    var query by rememberSaveable { mutableStateOf("") }
    var search by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(query) {
        delay(250)
        search = query
    }

    val searchRequestState = rememberTandoorRequestState()

    val searchResults = remember { mutableStateListOf<TandoorRecipeOverview>() }
    LaunchedEffect(search) {
        searchRequestState.wrapRequest {
            client.recipe.list(
                parameters = TandoorRecipeQueryParameters(
                    query = search
                ),
                pageSize = HOME_SEARCH_PAGING_SIZE,
            )
        }?.let {
            searchResults.clear()
            searchResults.addAll(it.results)
        }
    }

    DockedSearchBar(
        colors = SearchBarDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            dividerColor = Color.Transparent
        ),
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = { query = it },
                onSearch = {
                    keyboardController?.hide()
                    search = it
                },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Search,
                        stringResource(Res.string.search_recipes)
                    )
                },
                placeholder = { Text(stringResource(Res.string.search_recipes)) },
                expanded = true,
                onExpandedChange = { }
            )
        },
        expanded = true,
        onExpandedChange = { }
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(searchResults.size, key = { searchResults[it].id }) {
                val recipeOverview = searchResults[it]

                HorizontalRecipeCardLink(
                    recipeOverview = recipeOverview,
                    onClick = onSelect
                )
            }
        }
    }
}