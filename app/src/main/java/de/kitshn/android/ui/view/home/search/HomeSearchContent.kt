package de.kitshn.android.ui.view.home.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DinnerDining
import androidx.compose.material.icons.rounded.NoMeals
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.kitshn.android.R
import de.kitshn.android.api.tandoor.TandoorClient
import de.kitshn.android.api.tandoor.TandoorRequestStateState
import de.kitshn.android.api.tandoor.model.recipe.TandoorRecipeOverview
import de.kitshn.android.api.tandoor.route.TandoorRecipeQueryParameters
import de.kitshn.android.foodToIdList
import de.kitshn.android.keywordToIdList
import de.kitshn.android.reachedBottom
import de.kitshn.android.ui.TandoorRequestErrorHandler
import de.kitshn.android.ui.component.alert.FullSizeAlertPane
import de.kitshn.android.ui.component.model.recipe.HorizontalRecipeCardLink
import de.kitshn.android.ui.component.search.AdditionalSearchSettingsChipRow
import de.kitshn.android.ui.component.search.rememberAdditionalSearchSettingsChipRowState
import de.kitshn.android.ui.selectionMode.SelectionModeState
import de.kitshn.android.ui.state.foreverRememberNotSavable

const val HOME_SEARCH_PAGING_SIZE = 36

@Composable
fun ViewHomeSearchContent(
    client: TandoorClient,
    state: HomeSearchState,
    selectionModeState: SelectionModeState<Int>? = null,
    onClick: (recipe: TandoorRecipeOverview) -> Unit
) {
    val additionalSearchSettingsChipRowState by foreverRememberNotSavable(
        key = "ViewHomeSearchContent/additionalSearchSettingsChipRowState",
        initialValue = rememberAdditionalSearchSettingsChipRowState()
    )

    var firstUpdate by remember { mutableStateOf(true) }

    // overwrite to defaults
    LaunchedEffect(state.defaultValuesApplied) {
        if(state.defaultValuesApplied) return@LaunchedEffect

        state.defaultValuesApplied = true
        firstUpdate = false

        additionalSearchSettingsChipRowState.new = state.defaultValues.new
        additionalSearchSettingsChipRowState.random = state.defaultValues.random
        additionalSearchSettingsChipRowState.minimumRating = state.defaultValues.minimumRating
        additionalSearchSettingsChipRowState.sortOrder = state.defaultValues.sortOrder

        additionalSearchSettingsChipRowState.selectedKeywords.clear()
        additionalSearchSettingsChipRowState.selectedKeywords.addAll(state.defaultValues.keywords)

        additionalSearchSettingsChipRowState.selectedFoods.clear()
        additionalSearchSettingsChipRowState.selectedFoods.addAll(state.defaultValues.foods)
    }

    fun collectQueryParameters(): TandoorRecipeQueryParameters {
        return TandoorRecipeQueryParameters(
            query = state.query,
            new = additionalSearchSettingsChipRowState.new,
            random = additionalSearchSettingsChipRowState.random,
            keywords = additionalSearchSettingsChipRowState.selectedKeywords.keywordToIdList(),
            keywordsAnd = additionalSearchSettingsChipRowState.keywordsAnd,
            foods = additionalSearchSettingsChipRowState.selectedFoods.foodToIdList(),
            foodsAnd = additionalSearchSettingsChipRowState.foodsAnd,
            rating = additionalSearchSettingsChipRowState.minimumRating,
            sortOrder = additionalSearchSettingsChipRowState.sortOrder
        )
    }

    LaunchedEffect(state.query, additionalSearchSettingsChipRowState.updateState) {
        if(firstUpdate) {
            firstUpdate = false
            return@LaunchedEffect
        }

        state.currentPage = 1

        state.searchRequestState.wrapRequest {
            client.recipe.list(
                parameters = collectQueryParameters(),
                pageSize = HOME_SEARCH_PAGING_SIZE,
                page = state.currentPage
            )
        }?.let {
            state.nextPageExists = it.next != null

            state.searchResultIds.clear()
            it.results.forEach { recipe -> state.searchResultIds.add(recipe.id) }
        }
    }

    val searchLazyListState by foreverRememberNotSavable(
        key = "ViewHomeSearchContent/lazyList/${state.query}/${additionalSearchSettingsChipRowState.updateState}",
        initialValue = LazyListState()
    )
    val reachedBottom by remember { derivedStateOf { searchLazyListState.reachedBottom(buffer = 3) } }

    LaunchedEffect(reachedBottom) {
        if(state.searchRequestState.state != TandoorRequestStateState.SUCCESS) return@LaunchedEffect
        if(state.extendedSearchRequestState.state == TandoorRequestStateState.LOADING) return@LaunchedEffect
        if(!state.nextPageExists) return@LaunchedEffect

        state.currentPage++
        state.extendedSearchRequestState.wrapRequest {
            client.recipe.list(
                parameters = collectQueryParameters(),
                pageSize = HOME_SEARCH_PAGING_SIZE,
                page = state.currentPage
            )
        }?.let {
            state.nextPageExists = it.next != null
            it.results.forEach { recipe -> state.searchResultIds.add(recipe.id) }
        }
    }

    Column(
        Modifier.fillMaxSize()
    ) {
        AdditionalSearchSettingsChipRow(
            client = client,
            state = additionalSearchSettingsChipRowState
        )

        HorizontalDivider()

        if(state.searchRequestState.state == TandoorRequestStateState.LOADING
            || state.extendedSearchRequestState.state == TandoorRequestStateState.LOADING
        ) {

            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            HorizontalDivider()
        }

        if(state.searchRequestState.state == TandoorRequestStateState.SUCCESS) {
            if(state.searchResultIds.size > 0) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = searchLazyListState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.searchResultIds.size, key = { state.searchResultIds[it] }) {
                        val recipeOverview =
                            client.container.recipeOverview[state.searchResultIds[it]]

                        if(recipeOverview != null) HorizontalRecipeCardLink(
                            recipeOverview = recipeOverview,
                            selectionState = selectionModeState
                        ) { ro -> onClick(ro) }
                    }
                }
            } else {
                FullSizeAlertPane(
                    imageVector = Icons.Rounded.NoMeals,
                    contentDescription = stringResource(R.string.home_search_empty),
                    text = stringResource(R.string.home_search_empty)
                )
            }
        } else if(state.searchRequestState.state == TandoorRequestStateState.IDLE) {
            FullSizeAlertPane(
                imageVector = Icons.Rounded.DinnerDining,
                contentDescription = stringResource(R.string.home_search_empty_query),
                text = stringResource(R.string.home_search_empty_query)
            )
        }
    }

    TandoorRequestErrorHandler(state = state.searchRequestState)
    TandoorRequestErrorHandler(state = state.extendedSearchRequestState)
}