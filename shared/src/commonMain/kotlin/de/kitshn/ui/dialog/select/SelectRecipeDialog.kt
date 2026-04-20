package de.kitshn.ui.dialog.select

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.recipe.TandoorRecipeOverview
import de.kitshn.ui.dialog.RecipeSearchDialog
import kitshn.shared.generated.resources.Res
import kitshn.shared.generated.resources.search_recipes
import kitshn.shared.generated.resources.select_recipe
import org.jetbrains.compose.resources.stringResource

@Composable
fun rememberSelectRecipeDialogState(): SelectRecipeDialogState {
    return remember {
        SelectRecipeDialogState()
    }
}

class SelectRecipeDialogState(
    val shown: MutableState<Boolean> = mutableStateOf(false)
) {
    var initialSelectedId: Int? = null

    fun open(initialSelectedId: Int? = null) {
        this.initialSelectedId = initialSelectedId
        this.shown.value = true
    }

    fun dismiss() {
        this.shown.value = false
    }
}

@Composable
fun SelectRecipeDialog(
    client: TandoorClient,
    state: SelectRecipeDialogState,
    onSubmit: (recipeOverview: TandoorRecipeOverview) -> Unit
) {
    if(!state.shown.value) return

    RecipeSearchDialog(
        client = client,
        onDismissRequest = {
            state.dismiss()
        },
        initialSelectedId = state.initialSelectedId,
        title = stringResource(Res.string.select_recipe),
        placeholder = stringResource(Res.string.search_recipes),
        onSelect = {
            onSubmit(it)
            state.dismiss()
        }
    )
}
