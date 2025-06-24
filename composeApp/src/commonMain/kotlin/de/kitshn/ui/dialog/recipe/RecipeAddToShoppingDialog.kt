package de.kitshn.ui.dialog.recipe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.model.TandoorIngredient
import de.kitshn.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.ui.component.model.ingredient.IngredientsList
import de.kitshn.ui.component.model.servings.ServingsSelector
import de.kitshn.ui.dialog.AdaptiveFullscreenDialog
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_add
import kitshn.composeapp.generated.resources.action_add_to_shopping
import org.jetbrains.compose.resources.stringResource

@Composable
fun rememberRecipeAddToShoppingDialogState(): RecipeAddToShoppingDialogState {
    return remember {
        RecipeAddToShoppingDialogState()
    }
}

class RecipeAddToShoppingDialogState(
    val shown: MutableState<Boolean> = mutableStateOf(false),
    val recipe: MutableState<TandoorRecipe?> = mutableStateOf(null)
) {
    val servings = mutableDoubleStateOf(1.0)

    val ingredients = mutableStateListOf<TandoorIngredient>()
    val selectedIngredients = mutableStateListOf<TandoorIngredient>()

    fun open(recipe: TandoorRecipe, servings: Double) {
        ingredients.clear()
        selectedIngredients.clear()

        ingredients.addAll(recipe.steps.flatMap { it.ingredients })
        selectedIngredients.addAll(ingredients)

        this.recipe.value = recipe
        this.servings.value = servings
        this.shown.value = true
    }

    fun dismiss() {
        this.shown.value = false
        this.recipe.value = null
    }
}

@Composable
fun RecipeAddToShoppingDialog(
    state: RecipeAddToShoppingDialogState,
    showFractionalValues: Boolean,
    onSubmit: (ingredients: List<TandoorIngredient>, servings: Double) -> Unit
) {
    if(!state.shown.value) return

    var servingsFactor by remember { mutableStateOf(1.0) }
    LaunchedEffect(state.servings.value) {
        servingsFactor =
            state.servings.value / (state.recipe.value?.servings ?: 1).toDouble()
    }

    AdaptiveFullscreenDialog(
        onDismiss = { state.dismiss() },
        title = {
            Text(
                text = stringResource(Res.string.action_add_to_shopping)
            )
        },
        actions = {
            Button(
                onClick = {
                    state.dismiss()
                    onSubmit(state.selectedIngredients, state.servings.value)
                }
            ) {
                Text(
                    text = stringResource(Res.string.action_add)
                )
            }
        }
    ) { nsc, _, _ ->
        LazyColumn(
            Modifier.nestedScroll(nsc)
        ) {
            item {
                Box(
                    Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    ServingsSelector(
                        value = state.servings.value,
                        label = state.recipe.value?.servings_text ?: ""
                    ) { value ->
                        state.servings.value = value
                    }
                }
            }

            item {
                Box(Modifier.padding(16.dp)) {
                    IngredientsList(
                        list = state.ingredients,

                        itemModifier = {
                            Modifier
                                .alpha(
                                    if(state.selectedIngredients.contains(it)) {
                                        1f
                                    } else {
                                        0.2f
                                    }
                                )
                                .clickable {
                                    if(state.selectedIngredients.contains(it)) {
                                        state.selectedIngredients.remove(it)
                                    } else {
                                        state.selectedIngredients.add(it)
                                    }
                                }
                        },
                        itemTrailingContent = {
                            Checkbox(
                                checked = state.selectedIngredients.contains(it),
                                onCheckedChange = { value ->
                                    if(value) {
                                        state.selectedIngredients.add(it)
                                    } else {
                                        state.selectedIngredients.remove(it)
                                    }
                                }
                            )
                        },

                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),

                        factor = servingsFactor,
                        showFractionalValues = showFractionalValues,
                        onNotEnoughSpace = { }
                    )
                }
            }
        }
    }
}