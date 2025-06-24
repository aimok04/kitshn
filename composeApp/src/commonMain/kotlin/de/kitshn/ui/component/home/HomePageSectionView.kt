package de.kitshn.ui.component.home

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
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.TandoorKeywordOverview
import de.kitshn.api.tandoor.model.recipe.TandoorRecipeOverview
import de.kitshn.homepage.model.HomePageSection
import de.kitshn.ui.component.model.recipe.RecipeCard
import de.kitshn.ui.modifier.loadingPlaceHolder
import de.kitshn.ui.selectionMode.SelectionModeState
import de.kitshn.ui.state.ErrorLoadingSuccessState
import de.kitshn.ui.state.rememberForeverLazyGridState
import de.kitshn.ui.theme.Typography
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.allStringResources
import kitshn.composeapp.generated.resources.lorem_ipsum_title
import org.jetbrains.compose.resources.stringResource

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
            text = Res.allStringResources[section?.title]?.let { stringResource(it) }
                ?: stringResource(Res.string.lorem_ipsum_title),
            style = Typography().titleLarge
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
                    val recipe = client?.container?.recipeOverview?.get(recipeId)

                    recipe?.let { "recipe-$it" } ?: "index-$index"
                }) { index ->
                    val recipeId = section.recipeIds[index]
                    val recipe = client?.container?.recipeOverview?.get(recipeId)
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