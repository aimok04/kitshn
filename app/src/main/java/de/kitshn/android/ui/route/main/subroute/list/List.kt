package de.kitshn.android.ui.route.main.subroute.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.kitshn.android.R
import de.kitshn.android.api.tandoor.TandoorRequestState
import de.kitshn.android.api.tandoor.TandoorRequestStateState
import de.kitshn.android.api.tandoor.route.TandoorRecipeQueryParameters
import de.kitshn.android.reachedBottom
import de.kitshn.android.ui.component.LoadingGradientWrapper
import de.kitshn.android.ui.component.alert.LoadingErrorAlertPaneWrapper
import de.kitshn.android.ui.component.buttons.BackButton
import de.kitshn.android.ui.component.model.recipe.HorizontalRecipeCardLink
import de.kitshn.android.ui.layout.KitshnRecipeListRecipeDetailPaneScaffold
import de.kitshn.android.ui.route.RouteParameters
import de.kitshn.android.ui.selectionMode.model.RecipeSelectionModeTopAppBar
import de.kitshn.android.ui.selectionMode.rememberSelectionModeState
import de.kitshn.android.ui.state.ErrorLoadingSuccessState
import de.kitshn.android.ui.state.foreverRememberMutableStateList
import de.kitshn.android.ui.state.foreverRememberNotSavable
import de.kitshn.android.ui.state.rememberErrorLoadingSuccessState
import de.kitshn.android.ui.view.home.search.HOME_SEARCH_PAGING_SIZE

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteMainSubrouteList(
    p: RouteParameters
) {
    val client = p.vm.tandoorClient ?: return

    val selectionModeState = rememberSelectionModeState<Int>()

    var pageLoadingState by rememberErrorLoadingSuccessState()

    val listRequestState = remember { TandoorRequestState() }
    val extendedListRequestState = remember { TandoorRequestState() }

    val resultIds = foreverRememberMutableStateList<Int>(key = "RouteMainSubrouteList/resultIds")
    var currentPage by foreverRememberNotSavable(
        key = "RouteMainSubrouteList/currentPage",
        initialValue = 1
    )
    var nextPageExists by foreverRememberNotSavable(
        key = "RouteMainSubrouteList/nextPageExists",
        initialValue = true
    )

    LaunchedEffect(Unit) {
        if(resultIds.size > 0) {
            pageLoadingState = ErrorLoadingSuccessState.SUCCESS
            return@LaunchedEffect
        }

        currentPage = 1

        listRequestState.wrapRequest {
            client.recipe.list(
                parameters = TandoorRecipeQueryParameters(),
                pageSize = HOME_SEARCH_PAGING_SIZE,
                page = currentPage
            )
        }?.let {
            nextPageExists = it.next != null

            resultIds.clear()
            it.results.forEach { recipe -> resultIds.add(recipe.id) }

            pageLoadingState = ErrorLoadingSuccessState.SUCCESS
        }

        if(listRequestState.state == TandoorRequestStateState.ERROR)
            pageLoadingState = ErrorLoadingSuccessState.ERROR
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val gridState = rememberLazyGridState()
    val reachedBottom by remember { derivedStateOf { gridState.reachedBottom(buffer = 5) } }

    LaunchedEffect(reachedBottom) {
        if(extendedListRequestState.state == TandoorRequestStateState.LOADING) return@LaunchedEffect
        if(!nextPageExists) return@LaunchedEffect

        currentPage++
        extendedListRequestState.wrapRequest {
            client.recipe.list(
                parameters = TandoorRecipeQueryParameters(),
                pageSize = HOME_SEARCH_PAGING_SIZE,
                page = currentPage
            )
        }?.let {
            nextPageExists = it.next != null
            it.results.forEach { recipe -> resultIds.add(recipe.id) }
        }
    }

    KitshnRecipeListRecipeDetailPaneScaffold(
        vm = p.vm,
        key = "RouteMainSubrouteList",
        topBar = {
            RecipeSelectionModeTopAppBar(
                vm = p.vm,
                topAppBar = {
                    TopAppBar(
                        navigationIcon = {
                            BackButton(onBack = { p.vm.mainSubNavHostController?.popBackStack() })
                        },
                        title = {
                            Text(text = stringResource(R.string.navigation_list))
                        },
                        colors = it,
                        scrollBehavior = scrollBehavior
                    )
                },
                state = selectionModeState
            )
        },
        onClickKeyword = {
            p.vm.searchKeyword(it.id)
        }
    ) { pv, _, _, background, onSelect ->
        LoadingErrorAlertPaneWrapper(loadingState = pageLoadingState) {
            LoadingGradientWrapper(
                Modifier
                    .padding(pv),
                loadingState = pageLoadingState,
                backgroundColor = background
            ) {
                LazyVerticalGrid(
                    modifier = Modifier
                        .nestedScroll(scrollBehavior.nestedScrollConnection),

                    state = gridState,

                    contentPadding = PaddingValues(16.dp),
                    userScrollEnabled = pageLoadingState != ErrorLoadingSuccessState.LOADING,

                    columns = GridCells.Adaptive(minSize = 300.dp),

                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(resultIds.size) {
                        val recipeOverview =
                            client.container.recipeOverview[resultIds[it]]

                        if(recipeOverview != null) HorizontalRecipeCardLink(
                            recipeOverview = recipeOverview,
                            selectionState = selectionModeState
                        ) { r -> onSelect(r.id.toString()) }
                    }
                }
            }
        }
    }
}