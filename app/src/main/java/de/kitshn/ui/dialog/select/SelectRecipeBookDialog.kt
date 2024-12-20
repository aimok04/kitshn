package de.kitshn.ui.dialog.select

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import de.kitshn.R
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.TandoorRecipeBook
import de.kitshn.scoreMatch
import de.kitshn.ui.component.model.recipebook.HorizontalRecipeBookCard
import kotlinx.coroutines.delay

@Composable
fun rememberSelectRecipeBookDialogState(): SelectRecipeBookDialogState {
    return remember {
        SelectRecipeBookDialogState()
    }
}

class SelectRecipeBookDialogState(
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
fun SelectRecipeBookDialog(
    client: TandoorClient,
    favoritesRecipeBookId: Int,
    state: SelectRecipeBookDialogState,
    onSubmit: (recipeBook: TandoorRecipeBook) -> Unit
) {
    if(!state.shown.value) return

    val submit: (recipeBook: TandoorRecipeBook?) -> Unit = {
        it?.let { it1 -> onSubmit(it1) }
        state.dismiss()
    }

    AlertDialog(
        modifier = Modifier.padding(16.dp),
        onDismissRequest = {
            state.dismiss()
        },
        icon = {
            Icon(Icons.Rounded.Receipt, stringResource(R.string.select_recipe_book))
        },
        title = {
            Text(stringResource(R.string.select_recipe_book))
        },
        text = {
            RecipeBookSearchBar(
                client = client,
                favoritesRecipeBookId = favoritesRecipeBookId,
                onSelect = submit
            )
        },
        confirmButton = {
            FilledTonalButton(onClick = {
                submit(null)
            }) {
                Text(stringResource(id = R.string.action_abort))
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeBookSearchBar(
    client: TandoorClient,
    favoritesRecipeBookId: Int,
    onSelect: (recipeBook: TandoorRecipeBook) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    var query by rememberSaveable { mutableStateOf("") }
    var search by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(query) {
        delay(250)
        search = query
    }

    // fetch recipeBooks, if empty
    LaunchedEffect(Unit) {
        if(client.container.recipeBook.size != 0) return@LaunchedEffect
        client.recipeBook.list()
    }

    val searchResults = remember { mutableStateListOf<TandoorRecipeBook>() }
    LaunchedEffect(client.container.recipeBook.toList(), search) {
        searchResults.addAll(
            client.container.recipeBook.values.sortedBy { it.name.scoreMatch(search) }
        )

        searchResults.removeIf { it.id == favoritesRecipeBookId }
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
                        stringResource(R.string.search_recipe_books)
                    )
                },
                placeholder = { Text(stringResource(R.string.search_recipe_books)) },
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
                val recipeBook = searchResults[it]

                HorizontalRecipeBookCard(
                    recipeBook = recipeBook,
                    onClick = onSelect
                )
            }
        }
    }
}