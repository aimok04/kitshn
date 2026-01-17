package de.kitshn.ui.component.model.ingredient

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.model.TandoorFoodRecipe
import de.kitshn.api.tandoor.model.TandoorIngredient
import de.kitshn.ui.modifier.loadingPlaceHolder
import de.kitshn.ui.state.ErrorLoadingSuccessState

enum class IngredientItemPosition {
    TOP,
    BETWEEN,
    BOTTOM,
    SINGULAR
}

@Composable
fun IngredientItem(
    modifier: Modifier = Modifier,

    ingredient: TandoorIngredient? = null,
    servingsFactor: Double = 1.0,

    trailingContent: @Composable () -> Unit = {},

    colors: ListItemColors = ListItemDefaults.colors(),
    position: IngredientItemPosition,

    maxWidth: Dp = 0.dp,
    minAmountWidth: Dp = 0.dp,
    minUnitWidth: Dp = 0.dp,

    enableTickingOff: Boolean,

    showFractionalValues: Boolean,

    loadingState: ErrorLoadingSuccessState = ErrorLoadingSuccessState.SUCCESS,

    onOpenRecipe: (recipe: TandoorFoodRecipe) -> Unit
) {
    val amount = (ingredient?.amount ?: 1.0) * servingsFactor

    val foodHasRecipe = ingredient?.food?.recipe != null

    val showTickedOff = enableTickingOff && ingredient?.tickedOff == true

    ListItem(
        modifier = modifier
            .alpha(if(showTickedOff) 0.7f else 1f)
            .padding(top = 1.dp, bottom = 1.dp)
            .clip(
                when(position) {
                    IngredientItemPosition.TOP -> RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 4.dp
                    )

                    IngredientItemPosition.BETWEEN -> RoundedCornerShape(4.dp)
                    IngredientItemPosition.BOTTOM -> RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 4.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    )

                    IngredientItemPosition.SINGULAR -> RoundedCornerShape(16.dp)
                }
            )
            .loadingPlaceHolder(loadingState)
            .then(
                if(foodHasRecipe) {
                    Modifier.clickable {
                        onOpenRecipe(ingredient.food.recipe)
                    }
                } else if(enableTickingOff) {
                    Modifier.clickable {
                        ingredient?.tickedOff = !ingredient.tickedOff
                    }
                }else{
                    Modifier
                }
            ),
        colors = colors,
        trailingContent = trailingContent,
        leadingContent = {
            if(ingredient == null) return@ListItem

            Row(
                Modifier
                    .widthIn(minAmountWidth + minUnitWidth, max = maxWidth / 2.2f)
            ) {
                Row(
                    Modifier
                        .widthIn(minAmountWidth)
                ) {
                    if(!ingredient.no_amount && amount > 0.0) Text(
                        text = ingredient.formatAmount(amount, fractional = showFractionalValues),
                        textDecoration = if(showTickedOff) TextDecoration.LineThrough else null
                    )

                    Spacer(Modifier.width(8.dp))
                }

                Row(
                    Modifier
                        .widthIn(minUnitWidth)
                ) {
                    if(!ingredient.no_amount && ingredient.unit != null) Text(
                        text = ingredient.getUnitLabel(amount),
                        textDecoration = if(showTickedOff) TextDecoration.LineThrough else null
                    )

                    Spacer(Modifier.width(8.dp))
                }
            }
        },
        headlineContent = {
            if(ingredient == null) return@ListItem

            Text(
                text = ingredient.getLabel(amount),
                color = when(foodHasRecipe) {
                    true ->MaterialTheme.colorScheme.primary
                    false -> Color.Unspecified
                },
                fontWeight = when(foodHasRecipe) {
                    true -> FontWeight.Bold
                    false -> null
                },
                textDecoration = when(foodHasRecipe) {
                    true -> TextDecoration.Underline
                    false -> if(showTickedOff) TextDecoration.LineThrough else null
                }
            )
        },
        supportingContent = if((ingredient?.note ?: "").isNotBlank()) {
            {
                Text(
                    text = ingredient?.note ?: "",
                    textDecoration = if(showTickedOff) TextDecoration.LineThrough else null
                )
            }
        } else null
    )
}