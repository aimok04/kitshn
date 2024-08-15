package de.kitshn.android.ui.component.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.kitshn.android.R
import de.kitshn.android.api.tandoor.TandoorClient
import de.kitshn.android.api.tandoor.model.TandoorKeywordOverview
import de.kitshn.android.api.tandoor.model.recipe.TandoorRecipeOverview
import de.kitshn.android.homepage.model.HomePageSection
import de.kitshn.android.ui.component.model.recipe.RecipeCard
import de.kitshn.android.ui.modifier.loadingPlaceHolder
import de.kitshn.android.ui.selectionMode.SelectionModeState
import de.kitshn.android.ui.state.ErrorLoadingSuccessState
import de.kitshn.android.ui.state.rememberForeverLazyListState
import de.kitshn.android.ui.theme.Typography
import kotlin.math.ceil
import kotlin.math.roundToInt

@Composable
fun HomePageSectionView(
    client: TandoorClient?,
    section: HomePageSection? = null,
    loadingState: ErrorLoadingSuccessState,
    selectionState: SelectionModeState<Int>? = null,
    minHeight: Dp,
    onHeightChanged: (height: Int) -> Unit,
    onClickKeyword: (keyword: TandoorKeywordOverview) -> Unit,
    onClickRecipe: (recipe: TandoorRecipeOverview) -> Unit
) {
    val lazyRowState =
        rememberForeverLazyListState(key = "RouteMainSubrouteHome/lazyRowState/${section.hashCode()}")

    // remove deleted recipes
    LaunchedEffect(section?.recipeIds?.toList()) {
        if(section == null || client == null) return@LaunchedEffect
        section.recipeIds.removeIf { !client.container.recipeOverview.contains(it) }
    }

    Column(
        Modifier
            .onGloballyPositioned { lc ->
                onHeightChanged(lc.size.height)
            }
            .heightIn(minHeight)
    ) {
        Text(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                .loadingPlaceHolder(loadingState),
            text = stringResource(id = section?.title ?: R.string.lorem_ipsum_title),
            style = Typography.titleLarge
        )

        LazyRow(
            state = lazyRowState
        ) {
            if(section != null) {
                val itemCount = ceil(section.recipeIds.size.toDouble() / 2.0).roundToInt()

                items(itemCount) {
                    Column {
                        val firstRecipe = client?.container?.recipeOverview?.getOrDefault(
                            section.recipeIds.getOrNull(it * 2), null
                        )
                        val secondRecipe = client?.container?.recipeOverview?.getOrDefault(
                            section.recipeIds.getOrNull((it * 2) + 1), null
                        )

                        if(firstRecipe != null) RecipeCard(
                            Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
                            recipeOverview = firstRecipe,
                            loadingState = loadingState,
                            selectionState = selectionState,
                            onClickKeyword = onClickKeyword
                        ) {
                            onClickRecipe(firstRecipe)
                        }

                        if(secondRecipe != null) RecipeCard(
                            Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
                            recipeOverview = secondRecipe,
                            loadingState = loadingState,
                            selectionState = selectionState,
                            onClickKeyword = onClickKeyword
                        ) {
                            onClickRecipe(secondRecipe)
                        }
                    }
                }

                item {
                    Spacer(Modifier.width(16.dp))
                }
            } else {
                items(6) {
                    Column {
                        RecipeCard(
                            Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
                            loadingState = loadingState,
                            onClickKeyword = onClickKeyword
                        ) { }

                        RecipeCard(
                            Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
                            loadingState = loadingState,
                            onClickKeyword = onClickKeyword
                        ) { }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}