package de.kitshn.ui.view.recipe.details

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Reorder
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_add_to_meal_plan
import kitshn.composeapp.generated.resources.action_add_to_shopping
import kitshn.composeapp.generated.resources.action_manage_recipe_books
import kitshn.composeapp.generated.resources.common_allocate_ingredients
import org.jetbrains.compose.resources.stringResource

@Composable
fun RecipeDetailsDropdown(
    expanded: Boolean,
    onManageRecipeBooks: () -> Unit,
    onAddToMealPlan: () -> Unit,
    onAddToShopping: () -> Unit,
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
            onManageRecipeBooks = onManageRecipeBooks,
            onAddToMealPlan = onAddToMealPlan,
            onAddToShopping = onAddToShopping,
            onAllocateIngredients = onAllocateIngredients,
            onDismiss = onDismiss
        )
    }
}

@Composable
fun RecipeDetailsDropdownContent(
    onManageRecipeBooks: () -> Unit,
    onAddToMealPlan: () -> Unit,
    onAddToShopping: () -> Unit,
    onAllocateIngredients: () -> Unit,
    onDismiss: () -> Unit
) {
    DropdownMenuItem(
        leadingIcon = {
            Icon(Icons.Rounded.Book, stringResource(Res.string.action_manage_recipe_books))
        },
        text = {
            Text(stringResource(Res.string.action_manage_recipe_books))
        },
        onClick = {
            onDismiss()
            onManageRecipeBooks()
        }
    )

    DropdownMenuItem(
        leadingIcon = {
            Icon(Icons.Rounded.DateRange, stringResource(Res.string.action_add_to_meal_plan))
        },
        text = {
            Text(stringResource(Res.string.action_add_to_meal_plan))
        },
        onClick = {
            onDismiss()
            onAddToMealPlan()
        }
    )

    DropdownMenuItem(
        leadingIcon = {
            Icon(Icons.Rounded.ShoppingCart, stringResource(Res.string.action_add_to_shopping))
        },
        text = {
            Text(stringResource(Res.string.action_add_to_shopping))
        },
        onClick = {
            onDismiss()
            onAddToShopping()
        }
    )

    DropdownMenuItem(
        leadingIcon = {
            Icon(Icons.Rounded.Reorder, stringResource(Res.string.common_allocate_ingredients))
        },
        text = {
            Text(stringResource(Res.string.common_allocate_ingredients))
        },
        onClick = {
            onDismiss()
            onAllocateIngredients()
        }
    )
}