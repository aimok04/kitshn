package de.kitshn.android.ui.view.recipe.details

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Reorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.kitshn.android.KitshnViewModel
import de.kitshn.android.R
import de.kitshn.android.api.tandoor.model.recipe.TandoorRecipeOverview
import de.kitshn.android.ui.component.model.recipe.button.RecipeFavoriteButton

@Composable
fun RecipeDetailsDropdown(
    vm: KitshnViewModel,
    recipeOverview: TandoorRecipeOverview,
    expanded: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddToRecipeBook: () -> Unit,
    onAddToMealPlan: () -> Unit,
    onAllocateIngredients: () -> Unit,
    onDismiss: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        shape = RoundedCornerShape(16.dp),
        onDismissRequest = {
            onDismiss()
        }
    ) {
        RecipeDetailsDropdownContent(
            vm = vm,
            recipeOverview = recipeOverview,
            onEdit = onEdit,
            onDelete = onDelete,
            onAddToRecipeBook = onAddToRecipeBook,
            onAddToMealPlan = onAddToMealPlan,
            onAllocateIngredients = onAllocateIngredients,
            onDismiss = onDismiss
        )
    }
}

@Composable
fun ColumnScope.RecipeDetailsDropdownContent(
    vm: KitshnViewModel,
    recipeOverview: TandoorRecipeOverview,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddToRecipeBook: () -> Unit,
    onAddToMealPlan: () -> Unit,
    onAllocateIngredients: () -> Unit,
    onDismiss: () -> Unit
) {
    Row(
        Modifier.align(Alignment.CenterHorizontally)
    ) {
        RecipeFavoriteButton(
            recipeOverview = recipeOverview,
            favoritesViewModel = vm.favorites
        )

        IconButton(
            onClick = {
                onDismiss()
                onEdit()
            }
        ) {
            Icon(Icons.Rounded.Edit, stringResource(id = R.string.action_edit))
        }

        IconButton(
            onClick = {
                onDismiss()
                onDelete()
            }
        ) {
            Icon(Icons.Rounded.Delete, stringResource(id = R.string.action_delete))
        }
    }

    DropdownMenuItem(
        leadingIcon = {
            Icon(Icons.Rounded.Book, stringResource(id = R.string.action_add_to_recipe_books))
        },
        text = {
            Text(stringResource(id = R.string.action_add_to_recipe_books))
        },
        onClick = {
            onDismiss()
            onAddToRecipeBook()
        }
    )

    DropdownMenuItem(
        leadingIcon = {
            Icon(Icons.Rounded.DateRange, stringResource(R.string.action_add_to_meal_plan))
        },
        text = {
            Text(stringResource(R.string.action_add_to_meal_plan))
        },
        onClick = {
            onDismiss()
            onAddToMealPlan()
        }
    )

    DropdownMenuItem(
        leadingIcon = {
            Icon(Icons.Rounded.Reorder, stringResource(R.string.common_allocate_ingredients))
        },
        text = {
            Text(stringResource(R.string.common_allocate_ingredients))
        },
        onClick = {
            onDismiss()
            onAllocateIngredients()
        }
    )
}