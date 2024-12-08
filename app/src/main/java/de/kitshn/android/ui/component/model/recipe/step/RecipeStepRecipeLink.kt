package de.kitshn.android.ui.component.model.recipe.step

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.kitshn.android.api.tandoor.model.TandoorStep
import de.kitshn.android.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.android.api.tandoor.rememberTandoorRequestState
import de.kitshn.android.ui.TandoorRequestErrorHandler
import de.kitshn.android.ui.component.model.recipe.HorizontalRecipeCardLink

@Composable
fun RecipeStepRecipeLink(
    modifier: Modifier = Modifier,
    step: TandoorStep,
    onClick: (recipe: TandoorRecipe) -> Unit
) {
    var recipe by remember { mutableStateOf<TandoorRecipe?>(null) }

    val fetchStepRecipeRequestState = rememberTandoorRequestState()
    LaunchedEffect(step) {
        fetchStepRecipeRequestState.wrapRequest {
            recipe = step.fetchStepRecipe()
        }
    }

    if(recipe == null) return

    HorizontalRecipeCardLink(
        modifier = modifier.fillMaxWidth(),
        recipeOverview = recipe!!.toOverview(),
        onClick = { onClick(recipe!!) }
    )

    TandoorRequestErrorHandler(fetchStepRecipeRequestState)
}