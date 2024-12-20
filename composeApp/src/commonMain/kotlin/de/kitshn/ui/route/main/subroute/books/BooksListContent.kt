package de.kitshn.ui.route.main.subroute.books

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Draw
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.model.TandoorRecipeBook
import de.kitshn.ui.component.LoadingGradientWrapper
import de.kitshn.ui.component.alert.FullSizeAlertPane
import de.kitshn.ui.component.model.recipebook.HorizontalRecipeBookCard
import de.kitshn.ui.selectionMode.SelectionModeState
import de.kitshn.ui.state.ErrorLoadingSuccessState
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.recipe_books_empty
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteMainSubrouteBooksListContent(
    books: List<TandoorRecipeBook>,
    favoritesBook: TandoorRecipeBook?,

    pageLoadingState: ErrorLoadingSuccessState,
    selectionModeState: SelectionModeState<Int>,

    scrollBehavior: TopAppBarScrollBehavior,
    lazyListState: LazyListState,

    pv: PaddingValues,
    background: Color,
    onSelect: (id: String) -> Unit
) {
    if(pageLoadingState == ErrorLoadingSuccessState.SUCCESS && books.isEmpty()) {
        Box(
            Modifier
                .padding(pv)
                .fillMaxSize()
        ) {
            FullSizeAlertPane(
                imageVector = Icons.Rounded.Draw,
                contentDescription = stringResource(Res.string.recipe_books_empty),
                text = stringResource(Res.string.recipe_books_empty)
            )
        }

        return
    }

    LoadingGradientWrapper(
        loadingState = pageLoadingState,
        backgroundColor = background
    ) {
        LazyColumn(
            Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(pv)
                .fillMaxSize(),
            state = lazyListState,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if(pageLoadingState == ErrorLoadingSuccessState.LOADING) {
                item {
                    HorizontalRecipeBookCard(
                        loadingState = pageLoadingState
                    )
                }

                item {
                    HorizontalRecipeBookCard(
                        loadingState = pageLoadingState
                    )
                }

                items(15) {
                    HorizontalRecipeBookCard(
                        loadingState = pageLoadingState
                    )
                }
                return@LazyColumn
            }

            if(favoritesBook != null) {
                item {
                    HorizontalRecipeBookCard(
                        recipeBook = favoritesBook,
                        isFavoritesBook = true,
                        selectionState = selectionModeState
                    ) {
                        onSelect(it.id.toString())
                    }
                }

                item {
                    HorizontalDivider(
                        Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )
                }
            }

            items(books.size, key = { books[it].id }) { index ->
                val book = books[index]

                HorizontalRecipeBookCard(
                    recipeBook = book,
                    selectionState = selectionModeState
                ) {
                    onSelect(it.id.toString())
                }
            }
        }
    }
}