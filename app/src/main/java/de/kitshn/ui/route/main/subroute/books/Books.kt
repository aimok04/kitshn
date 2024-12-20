package de.kitshn.ui.route.main.subroute.books

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import de.kitshn.R
import de.kitshn.api.tandoor.model.TandoorRecipeBook
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.isScrollingUp
import de.kitshn.ui.TandoorRequestErrorHandler
import de.kitshn.ui.component.alert.LoadingErrorAlertPaneWrapper
import de.kitshn.ui.dialog.recipe.RecipeLinkDialog
import de.kitshn.ui.dialog.recipe.rememberRecipeLinkDialogState
import de.kitshn.ui.dialog.recipeBook.RecipeBookCreationAndEditDialog
import de.kitshn.ui.dialog.recipeBook.rememberRecipeBookCreationDialogState
import de.kitshn.ui.dialog.recipeBook.rememberRecipeBookEditDialogState
import de.kitshn.ui.layout.KitshnListDetailPaneScaffold
import de.kitshn.ui.route.RouteParameters
import de.kitshn.ui.selectionMode.rememberSelectionModeState
import de.kitshn.ui.state.ErrorLoadingSuccessState
import de.kitshn.ui.state.rememberErrorLoadingSuccessState
import de.kitshn.ui.state.rememberForeverLazyListState
import de.kitshn.ui.view.ViewParameters
import de.kitshn.ui.view.books.ViewBooksDetails

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteMainSubrouteBooks(
    p: RouteParameters
) {
    val client = p.vm.tandoorClient ?: return

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val lazyListState = rememberForeverLazyListState(key = "RouteMainSubrouteBooks/lazyListState")

    var pageLoadingState by rememberErrorLoadingSuccessState()
    val selectionModeState = rememberSelectionModeState<Int>()

    val creationDialogState =
        rememberRecipeBookCreationDialogState(key = "RouteMainSubrouteBooks/creationDialogState")
    val editDialogState =
        rememberRecipeBookEditDialogState(key = "RouteMainSubrouteBooks/editDialogState")
    val recipeLinkDialogState = rememberRecipeLinkDialogState()

    val mainFetchRequestState = rememberTandoorRequestState()
    mainFetchRequestState.LoadingStateAdapter { pageLoadingState = it }

    val deleteRequestState = rememberTandoorRequestState()

    val books = remember { mutableStateListOf<TandoorRecipeBook>() }
    var favoritesBook by remember { mutableStateOf<TandoorRecipeBook?>(null) }

    var lastUpdate by remember { mutableLongStateOf(0L) }
    LaunchedEffect(lastUpdate) {
        pageLoadingState = ErrorLoadingSuccessState.LOADING

        mainFetchRequestState.wrapRequest {
            books.clear()
            books.addAll(p.vm.tandoorClient!!.recipeBook.list())

            val favoriteRecipeBookId = try {
                p.vm.favorites.getFavoritesRecipeBookId()
            } catch(e: Error) {
                e.printStackTrace()

                -1
            }

            favoritesBook = books.firstOrNull { it.id == favoriteRecipeBookId }
            books.remove(favoritesBook)

            pageLoadingState = ErrorLoadingSuccessState.SUCCESS
        }
    }

    KitshnListDetailPaneScaffold(
        key = "RouteMainSubrouteBooks",
        topBar = {
            RouteMainSubrouteBooksTopAppBar(
                client = client,

                colors = it,
                scrollBehavior = scrollBehavior,

                favoritesRecipeBookId = p.vm.favorites.getFavoritesRecipeBookIdSync(),

                selectionModeState = selectionModeState,
                editDialogState = editDialogState,
                deleteRequestState = deleteRequestState
            ) {
                lastUpdate = System.currentTimeMillis()
            }
        },
        listContent = { pv, _, _, background, onSelect ->
            LoadingErrorAlertPaneWrapper(
                loadingState = pageLoadingState
            ) {
                RouteMainSubrouteBooksListContent(
                    books = books,
                    favoritesBook = favoritesBook,

                    pageLoadingState = pageLoadingState,
                    selectionModeState = selectionModeState,

                    scrollBehavior = scrollBehavior,
                    lazyListState = lazyListState,

                    pv = pv,
                    background = background,
                    onSelect = onSelect
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                expanded = lazyListState.isScrollingUp(),
                icon = {
                    Icon(Icons.Rounded.Add, stringResource(id = R.string.action_add))
                },
                text = {
                    Text(stringResource(id = R.string.action_add))
                },
                onClick = {
                    creationDialogState.open()
                }
            )
        }
    ) { selectId, _, _, _, _, back ->
        client.container.recipeBook[selectId.toInt()]?.let { recipeBook ->
            ViewBooksDetails(
                p = ViewParameters(p.vm, back),
                book = recipeBook,
                isFavoriteBook = p.vm.favorites.getFavoritesRecipeBookIdSync() == recipeBook.id,
                onClickKeyword = { keyword ->
                    back?.let { it() }
                    p.vm.searchKeyword(keyword.id)
                }
            ) {
                recipeLinkDialogState.open(it)
            }
        }
    }

    RecipeLinkDialog(
        p = ViewParameters(
            vm = p.vm,
            back = null
        ),
        state = recipeLinkDialogState
    )

    if(p.vm.tandoorClient != null) {
        RecipeBookCreationAndEditDialog(
            client = p.vm.tandoorClient!!,
            creationState = creationDialogState,
            editState = editDialogState
        ) { lastUpdate = System.currentTimeMillis() }
    }

    TandoorRequestErrorHandler(state = deleteRequestState)
}