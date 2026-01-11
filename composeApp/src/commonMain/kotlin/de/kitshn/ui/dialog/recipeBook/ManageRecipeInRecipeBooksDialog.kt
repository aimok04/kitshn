package de.kitshn.ui.dialog.recipeBook

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.TandoorRecipeBook
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.removeIf
import de.kitshn.ui.TandoorRequestErrorHandler
import de.kitshn.ui.component.alert.FullSizeAlertPane
import de.kitshn.ui.component.input.AlwaysDockedSearchBar
import de.kitshn.ui.component.input.iosKeyboardWorkaround.InputFieldWithIOSKeyboardWorkaround
import de.kitshn.ui.component.model.recipebook.HorizontalRecipeBookCard
import de.kitshn.ui.modifier.fullWidthAlertDialogPadding
import de.kitshn.ui.view.home.search.HOME_SEARCH_PAGING_SIZE
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_apply
import kitshn.composeapp.generated.resources.action_manage_recipe_books
import kitshn.composeapp.generated.resources.manage_recipe_books_empty
import kitshn.composeapp.generated.resources.search_recipe_books
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun rememberManageRecipeInRecipeBooksDialogState(): ManageRecipeInRecipeBooksDialogState {
    return remember {
        ManageRecipeInRecipeBooksDialogState()
    }
}

class ManageRecipeInRecipeBooksDialogState(
    val shown: MutableState<Boolean> = mutableStateOf(false)
) {
    val recipeId = mutableStateOf<Int?>(null)
    val selectedRecipeBooks = mutableStateListOf<TandoorRecipeBook>()
    val defaultSelectedRecipeBooks = mutableStateListOf<TandoorRecipeBook>()

    fun open(recipeId: Int) {
        this.recipeId.value = recipeId
        this.shown.value = true
    }

    fun dismiss() {
        this.shown.value = false
    }
}

@Composable
fun ManageRecipeInRecipeBooksDialog(
    client: TandoorClient,
    favoritesRecipeBookId: Int,
    state: ManageRecipeInRecipeBooksDialogState
) {
    val coroutineScope = rememberCoroutineScope()
    if(!state.shown.value) return

    val fetchRequestState = rememberTandoorRequestState()
    LaunchedEffect(state.recipeId, state.shown) {
        if(!state.shown.value) return@LaunchedEffect

        fetchRequestState.wrapRequest {
            state.selectedRecipeBooks.clear()
            state.selectedRecipeBooks.addAll(
                client.recipeBook.listAll().filterNot { it.id == favoritesRecipeBookId }.filter {
                    it.listAllEntries()?.firstOrNull { entry ->
                        entry.recipe == state.recipeId.value
                    } != null
                }
            )

            state.defaultSelectedRecipeBooks.clear()
            state.defaultSelectedRecipeBooks.addAll(state.selectedRecipeBooks)
        }
    }

    fun submit() {
        state.defaultSelectedRecipeBooks.filter { book ->
            state.selectedRecipeBooks.firstOrNull { it.id == book.id } == null
        }.forEach {
            coroutineScope.launch {
                it.entryByRecipeId[state.recipeId.value]?.delete()
            }
        }

        state.selectedRecipeBooks.filter { book ->
            state.defaultSelectedRecipeBooks.firstOrNull { it.id == book.id } == null
        }.forEach {
            coroutineScope.launch {
                state.recipeId.value?.let { id ->
                    it.createEntry(id)
                }
            }
        }
    }

    AlertDialog(
        modifier = Modifier.fullWidthAlertDialogPadding(),
        onDismissRequest = {
            state.dismiss()
        },
        icon = {
            Icon(Icons.Rounded.Book, stringResource(Res.string.action_manage_recipe_books))
        },
        title = {
            Text(stringResource(Res.string.action_manage_recipe_books))
        },
        text = {
            BoxWithConstraints {
                Column {
                    Box(
                        Modifier.height(
                            (this@BoxWithConstraints.maxHeight - 32.dp) / 2f
                        ),
                    ) {
                        RecipeBookSearchBar(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(),
                            client = client,

                            favoritesRecipeBookId = favoritesRecipeBookId,
                            selectedRecipeBooks = state.selectedRecipeBooks
                        ) { recipeBook, _, value ->
                            if(value) {
                                state.selectedRecipeBooks.add(0, recipeBook)
                            } else {
                                state.selectedRecipeBooks.removeIf { it.id == recipeBook.id }
                            }
                        }
                    }

                    HorizontalDivider(
                        Modifier.padding(top = 16.dp, bottom = 16.dp)
                    )

                    Box(
                        Modifier.fillMaxHeight()
                    ) {
                        if(state.selectedRecipeBooks.size == 0) {
                            FullSizeAlertPane(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = stringResource(Res.string.manage_recipe_books_empty),
                                text = stringResource(Res.string.manage_recipe_books_empty)
                            )
                        } else {
                            LazyColumn(
                                Modifier.clip(RoundedCornerShape(16.dp)),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(state.selectedRecipeBooks.size) {
                                    val book = state.selectedRecipeBooks[it]

                                    RecipeBookCheckedListItem(
                                        checked = true,
                                        recipeBook = book
                                    ) {
                                        state.selectedRecipeBooks.remove(book)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                state.dismiss()
                submit()
            }) {
                Text(stringResource(Res.string.action_apply))
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    )

    TandoorRequestErrorHandler(fetchRequestState)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeBookSearchBar(
    modifier: Modifier = Modifier,
    client: TandoorClient,
    selectedRecipeBooks: List<TandoorRecipeBook>,
    favoritesRecipeBookId: Int,
    onCheckedChange: (recipeBook: TandoorRecipeBook, recipeBookId: Int, value: Boolean) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    var query by rememberSaveable { mutableStateOf("") }
    var search by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(query) {
        delay(250)
        search = query
    }

    val searchRequestState = rememberTandoorRequestState()

    val searchResults = remember { mutableStateListOf<TandoorRecipeBook>() }
    LaunchedEffect(search) {
        searchRequestState.wrapRequest {
            client.recipeBook.list(
                query = search,
                pageSize = HOME_SEARCH_PAGING_SIZE,
            )
        }?.let {
            searchResults.clear()
            searchResults.addAll(it.results.filterNot { it.id == favoritesRecipeBookId })
        }
    }

    val selectedRecipeBookIds = remember { mutableStateListOf<Int>() }
    LaunchedEffect(selectedRecipeBooks.toList()) {
        selectedRecipeBookIds.clear()
        selectedRecipeBookIds.addAll(selectedRecipeBooks.map { it.id })
    }

    AlwaysDockedSearchBar(
        modifier = modifier,
        colors = SearchBarDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            dividerColor = Color.Transparent
        ),
        inputField = {
            SearchBarDefaults.InputFieldWithIOSKeyboardWorkaround(
                query = query,
                onQueryChange = { query = it },
                onSearch = {
                    keyboardController?.hide()
                    search = it
                },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Search,
                        stringResource(Res.string.search_recipe_books)
                    )
                },
                placeholder = { Text(stringResource(Res.string.search_recipe_books)) },
                expanded = true,
                onExpandedChange = { }
            )
        }
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(searchResults.size) {
                val recipeBook = searchResults[it]

                RecipeBookCheckedListItem(
                    checked = selectedRecipeBookIds.contains(recipeBook.id),
                    recipeBook = recipeBook
                ) { checked ->
                    keyboardController?.hide()
                    onCheckedChange(recipeBook, recipeBook.id, checked)
                }
            }
        }
    }
}

@Composable
fun RecipeBookCheckedListItem(
    modifier: Modifier = Modifier,
    checked: Boolean,
    recipeBook: TandoorRecipeBook,
    onCheckedChange: (value: Boolean) -> Unit
) {
    HorizontalRecipeBookCard(
        modifier = modifier,
        recipeBook = recipeBook,
        leadingContent = {
            Checkbox(
                modifier = Modifier.padding(end = 4.dp),
                checked = checked,
                onCheckedChange = {
                    onCheckedChange(it)
                }
            )
        },
        onClick = {
            onCheckedChange(!checked)
        }
    )
}