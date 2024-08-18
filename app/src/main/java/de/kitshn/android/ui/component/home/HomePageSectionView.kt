package de.kitshn.android.ui.component.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import de.kitshn.android.ui.state.rememberForeverLazyGridState
import de.kitshn.android.ui.theme.Typography

@Composable
fun HomePageSectionView(
    client: TandoorClient?,
    section: HomePageSection? = null,
    loadingState: ErrorLoadingSuccessState,
    selectionState: SelectionModeState<Int>? = null,
    onClickKeyword: (keyword: TandoorKeywordOverview) -> Unit,
    onClickRecipe: (recipe: TandoorRecipeOverview) -> Unit
) {
    val lazyGridState =
        rememberForeverLazyGridState(key = "RouteMainSubrouteHome/lazyGridState/${section.hashCode()}")

    Column {
        Text(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                .loadingPlaceHolder(loadingState),
            text = stringResource(id = section?.title ?: R.string.lorem_ipsum_title),
            style = Typography.titleLarge
        )

        LazyHorizontalGrid(
            state = lazyGridState,
            rows = GridCells.Fixed(2),
            modifier = Modifier.height(642.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if(section != null) {
                items(section.recipeIds.size, key = { index ->
                    val recipeId = section.recipeIds[index]
                    val recipe = client?.container?.recipeOverview?.getOrDefault(recipeId, null)

                    recipe?.let { "recipe-$it" } ?: "index-$index"
                }) { index ->
                    val recipeId = section.recipeIds[index]
                    val recipe = client?.container?.recipeOverview?.getOrDefault(recipeId, null)
                        ?: return@items

                    Box {
                        RecipeCard(
                            Modifier.height(313.dp),
                            fillChipRow = true,
                            recipeOverview = recipe,
                            loadingState = loadingState,
                            selectionState = selectionState,
                            onClickKeyword = onClickKeyword
                        ) {
                            onClickRecipe(recipe)
                        }
                    }
                }
            } else {
                items(12) {
                    Box {
                        RecipeCard(
                            Modifier.height(313.dp),
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