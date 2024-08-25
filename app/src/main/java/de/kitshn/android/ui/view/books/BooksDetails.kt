package de.kitshn.android.ui.view.books

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.kitshn.android.R
import de.kitshn.android.api.tandoor.model.TandoorKeywordOverview
import de.kitshn.android.api.tandoor.model.TandoorRecipeBook
import de.kitshn.android.api.tandoor.model.TandoorRecipeBookEntry
import de.kitshn.android.api.tandoor.model.recipe.TandoorRecipeOverview
import de.kitshn.android.api.tandoor.rememberTandoorRequestState
import de.kitshn.android.isScrollingUp
import de.kitshn.android.ui.TandoorRequestErrorHandler
import de.kitshn.android.ui.component.alert.FullSizeAlertPane
import de.kitshn.android.ui.component.model.recipe.RecipeCard
import de.kitshn.android.ui.dialog.recipeBook.RecipeBookCreationAndEditDialog
import de.kitshn.android.ui.dialog.recipeBook.rememberRecipeBookCreationDialogState
import de.kitshn.android.ui.dialog.recipeBook.rememberRecipeBookEditDialogState
import de.kitshn.android.ui.dialog.select.SelectRecipeDialog
import de.kitshn.android.ui.dialog.select.rememberSelectRecipeDialogState
import de.kitshn.android.ui.selectionMode.rememberSelectionModeState
import de.kitshn.android.ui.view.ViewParameters
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewBooksDetails(
    p: ViewParameters,
    book: TandoorRecipeBook,
    isFavoriteBook: Boolean,
    onClickKeyword: (keyword: TandoorKeywordOverview) -> Unit = {},
    onClick: (recipeOverview: TandoorRecipeOverview) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val lazyStaggeredGridState = rememberLazyStaggeredGridState()

    val selectionModeState = rememberSelectionModeState<Int>()

    val createEntryRequestState = rememberTandoorRequestState()
    val deleteRequestState = rememberTandoorRequestState()

    val selectRecipeDialogState = rememberSelectRecipeDialogState()
    val creationDialogState =
        rememberRecipeBookCreationDialogState(key = "RouteMainSubrouteBooks/creationDialogState")
    val editDialogState =
        rememberRecipeBookEditDialogState(key = "RouteMainSubrouteBooks/editDialogState")

    LaunchedEffect(Unit) { book.listEntries() }

    val recipes = remember { mutableStateListOf<TandoorRecipeBookEntry>() }
    LaunchedEffect(book.entries.toList()) {
        recipes.clear()
        recipes.addAll(book.entries)

        try {
            val favoritesRecipeBookId = p.vm.favorites.getFavoritesRecipeBookId()
            if(book.id == favoritesRecipeBookId) recipes.reverse()
        } catch(e: Error) {
            e.printStackTrace()
        }
    }

    Scaffold(
        topBar = {
            ViewBooksDetailsTopAppBar(
                book = book,
                isFavoriteBook = isFavoriteBook,

                scrollBehavior = scrollBehavior,

                selectionModeState = selectionModeState,
                editDialogState = editDialogState,
                deleteRequestState = deleteRequestState,

                onBack = p.back
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                expanded = lazyStaggeredGridState.isScrollingUp(),
                icon = {
                    Icon(Icons.Rounded.Add, stringResource(id = R.string.action_add))
                },
                text = {
                    Text(stringResource(id = R.string.action_add))
                },
                onClick = {
                    selectRecipeDialogState.open()
                }
            )
        }
    ) { pv ->
        if(book.entries.size == 0) {
            Box(
                Modifier
                    .padding(pv)
                    .fillMaxSize()
            ) {
                FullSizeAlertPane(
                    imageVector = Icons.Rounded.Receipt,
                    contentDescription = stringResource(R.string.recipe_book_empty),
                    text = stringResource(R.string.recipe_book_empty)
                )
            }
        } else {
            LazyVerticalStaggeredGrid(
                modifier = Modifier
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(pv),
                state = lazyStaggeredGridState,
                columns = StaggeredGridCells.Adaptive(minSize = 200.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalItemSpacing = 8.dp,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if(book.description.isNotBlank() && !isFavoriteBook) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Text(text = book.description)
                    }

                    item(span = StaggeredGridItemSpan.FullLine) {
                        Spacer(Modifier.height(16.dp))
                    }
                }

                items(recipes.size, key = { recipes[it].id }) { index ->
                    val entry = recipes[index]

                    RecipeCard(
                        recipeOverview = entry.recipe_content,
                        selectionState = selectionModeState,
                        onClickKeyword = onClickKeyword,
                        onClick = { onClick(entry.recipe_content) }
                    )
                }
            }
        }
    }

    if(p.vm.tandoorClient != null) {
        RecipeBookCreationAndEditDialog(
            client = p.vm.tandoorClient!!,
            creationState = creationDialogState,
            editState = editDialogState
        ) { p.back?.let { it() } }

        SelectRecipeDialog(
            client = p.vm.tandoorClient!!,
            state = selectRecipeDialogState
        ) {
            coroutineScope.launch {
                createEntryRequestState.wrapRequest { book.createEntry(it.id) }

                delay(100)
                createEntryRequestState.reset()
            }
        }
    }

    TandoorRequestErrorHandler(state = createEntryRequestState)
    TandoorRequestErrorHandler(state = deleteRequestState)
}