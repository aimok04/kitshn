package de.kitshn.ui.route.main.subroute.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.TandoorRequestState
import de.kitshn.api.tandoor.TandoorRequestStateState
import de.kitshn.api.tandoor.route.TandoorRecipeQueryParameters
import de.kitshn.isScrollingUp
import de.kitshn.reachedBottom
import de.kitshn.ui.component.LoadingGradientWrapper
import de.kitshn.ui.component.alert.LoadingErrorAlertPaneWrapper
import de.kitshn.ui.component.model.recipe.RecipeCard
import de.kitshn.ui.route.RouteParameters
import de.kitshn.ui.selectionMode.SelectionModeState
import de.kitshn.ui.state.ErrorLoadingSuccessState
import de.kitshn.ui.state.foreverRememberMutableStateList
import de.kitshn.ui.state.foreverRememberNotSavable
import de.kitshn.ui.state.rememberErrorLoadingSuccessState
import de.kitshn.ui.view.home.search.HOME_SEARCH_PAGING_SIZE
import de.kitshn.ui.view.home.search.HomeSearchState
import de.kitshn.ui.view.recipe.details.RecipeServingsAmountSaveMap
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTraditionalLayout(
    p: RouteParameters,

    scrollBehavior: TopAppBarScrollBehavior,
    selectionModeState: SelectionModeState<Int>,
    homeSearchState: HomeSearchState,

    onIsScrollingUpChanged: (isScrollingUp: Boolean) -> Unit,

    wrap: @Composable (
        @Composable (
            pv: PaddingValues,
            supportsMultiplePages: Boolean,
            background: Color,

            onSelect: (String) -> Unit
        ) -> Unit
    ) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

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

    LaunchedEffect(p.vm.tandoorClient) {
        if(p.vm.tandoorClient == null) return@LaunchedEffect

        if(resultIds.size > 0) {
            pageLoadingState = ErrorLoadingSuccessState.SUCCESS
            return@LaunchedEffect
        }

        currentPage = 1

        listRequestState.wrapRequest {
            p.vm.tandoorClient!!.recipe.list(
                parameters = TandoorRecipeQueryParameters(
                    new = true
                ),
                pageSize = HOME_SEARCH_PAGING_SIZE,
                page = currentPage
            )
        }?.let {
            currentPage++

            nextPageExists = it.next != null

            resultIds.clear()
            it.results.forEach { recipe -> resultIds.add(recipe.id) }

            pageLoadingState = ErrorLoadingSuccessState.SUCCESS
        }

        if(listRequestState.state == TandoorRequestStateState.ERROR)
            pageLoadingState = ErrorLoadingSuccessState.ERROR
    }

    val gridState = rememberLazyGridState()
    val reachedBottom by remember { derivedStateOf { gridState.reachedBottom() } }

    var fetchNewItems by remember { mutableStateOf(false) }
    LaunchedEffect(reachedBottom) {
        if(reachedBottom) {
            fetchNewItems = true
        }
    }

    LaunchedEffect(fetchNewItems, nextPageExists) {
        while(fetchNewItems && nextPageExists) {
            if(listRequestState.state != TandoorRequestStateState.SUCCESS) {
                delay(500)
                continue
            }

            extendedListRequestState.wrapRequest {
                p.vm.tandoorClient!!.recipe.list(
                    parameters = TandoorRecipeQueryParameters(),
                    pageSize = HOME_SEARCH_PAGING_SIZE,
                    page = currentPage
                )
            }?.let {
                currentPage++

                nextPageExists = it.next != null
                it.results.forEach { recipe -> resultIds.add(recipe.id) }

                if(!reachedBottom) fetchNewItems = false
            }

            delay(500)
        }
    }

    val isScrollingUp = gridState.isScrollingUp()
    LaunchedEffect(isScrollingUp) { onIsScrollingUpChanged(isScrollingUp) }

    wrap { pv, _, background, onSelect ->
        LoadingErrorAlertPaneWrapper(loadingState = pageLoadingState) {
            LoadingGradientWrapper(
                Modifier
                    .padding(pv)
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                loadingState = pageLoadingState,
                backgroundColor = background
            ) {
                LazyVerticalGrid(
                    modifier = Modifier
                        .nestedScroll(scrollBehavior.nestedScrollConnection),

                    state = gridState,

                    contentPadding = PaddingValues(16.dp),
                    userScrollEnabled = pageLoadingState != ErrorLoadingSuccessState.LOADING,

                    columns = GridCells.Adaptive(minSize = 250.dp),

                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item(
                        span = {
                            GridItemSpan(maxCurrentLineSpan)
                        }
                    ) {
                        Spacer(Modifier.height(16.dp))

                        p.vm.tandoorClient?.let {
                            RouteMainSubrouteHomeMealPlanPromotionSection(
                                client = it,
                                loadingState = pageLoadingState
                            ) { recipeOverview, servings ->
                                RecipeServingsAmountSaveMap[recipeOverview.id] =
                                    servings.roundToInt()
                                onSelect(recipeOverview.id.toString())
                            }
                        }
                    }

                    if(resultIds.size == 0 && pageLoadingState != ErrorLoadingSuccessState.SUCCESS) {
                        items(20) {
                            Box {
                                RecipeCard(
                                    Modifier.height(300.dp)
                                        .fillMaxWidth(),
                                    alternateCoverSize = true,
                                    loadingState = pageLoadingState,
                                    onClickKeyword = { }
                                ) { }
                            }
                        }
                    } else {
                        items(resultIds.size) {
                            val recipeOverview =
                                p.vm.tandoorClient?.container?.recipeOverview?.get(resultIds[it])

                            if(recipeOverview != null) Box {
                                RecipeCard(
                                    Modifier.height(300.dp)
                                        .fillMaxWidth(),
                                    alternateCoverSize = true,
                                    fillChipRow = true,
                                    recipeOverview = recipeOverview,
                                    loadingState = pageLoadingState,
                                    selectionState = selectionModeState,
                                    onClickKeyword = {
                                        coroutineScope.launch {
                                            homeSearchState.openWithKeyword(
                                                p.vm.tandoorClient!!,
                                                it
                                            )
                                        }
                                    },
                                    onClick = { recipe -> onSelect(recipe.id.toString()) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}