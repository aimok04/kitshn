package de.kitshn.ui.component.model.ingredient

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun IngredientItem(
    modifier: Modifier = Modifier,

    ingredient: TandoorIngredient? = null,
    servingsFactor: Double = 1.0,

    trailingContent: @Composable () -> Unit = {},

    colors: ListItemColors = ListItemDefaults.colors(),

    index: Int,
    count: Int,

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

    SegmentedListItem(
        modifier = modifier
            .alpha(if(showTickedOff) 0.7f else 1f)
            .padding(top = 1.dp, bottom = 1.dp)
            .loadingPlaceHolder(loadingState),

        onClick = {
            if(foodHasRecipe) {
                onOpenRecipe(ingredient.food.recipe)
            } else if(enableTickingOff) {
                ingredient?.tickedOff = !ingredient.tickedOff
            }
        },

        shapes = ListItemDefaults.segmentedShapes(
            index = index,
            count = count,
            defaultShapes = ListItemDefaults.shapes(
                shape = when(count == 1) {
                    true -> RoundedCornerShape(16.dp)
                    false -> null
                }
            )
        ),

        colors = colors,
        trailingContent = trailingContent,
        leadingContent = {
            if(ingredient == null) return@SegmentedListItem

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
        content = {
            if(ingredient == null) return@SegmentedListItem

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