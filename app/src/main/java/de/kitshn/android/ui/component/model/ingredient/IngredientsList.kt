package de.kitshn.android.ui.component.model.ingredient

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import de.kitshn.android.api.tandoor.model.TandoorIngredient
import de.kitshn.android.ui.state.ErrorLoadingSuccessState

@Composable
fun IngredientsList(
    list: List<TandoorIngredient>,

    itemModifier: (ingredient: TandoorIngredient) -> Modifier = { Modifier },
    itemTrailingContent: @Composable (ingredient: TandoorIngredient) -> Unit = {},

    factor: Double = 1.0,
    loadingState: ErrorLoadingSuccessState = ErrorLoadingSuccessState.SUCCESS,
    colors: ListItemColors = ListItemDefaults.colors(),

    onNotEnoughSpace: () -> Unit
) {
    val density = LocalDensity.current

    var minAmountWidth by remember { mutableStateOf(0.dp) }
    var minUnitWidth by remember { mutableStateOf(0.dp) }

    val textMeasure = rememberTextMeasurer()
    LaunchedEffect(list, list.size, factor) {
        minAmountWidth = 0.dp
        minUnitWidth = 0.dp

        list.forEach { ingredient ->
            val amount = ingredient.amount * factor

            if(!ingredient.no_amount && ingredient.amount > 0.0) {
                val widthDp =
                    with(density) { textMeasure.measure(ingredient.formatAmount(amount)).size.width.toDp() }
                if(widthDp > minAmountWidth) minAmountWidth = widthDp
            }

            if(!ingredient.no_amount && ingredient.unit != null) {
                val widthDp =
                    with(density) { textMeasure.measure(ingredient.getUnitLabel(amount)).size.width.toDp() }
                if(widthDp > minUnitWidth) minUnitWidth = widthDp
            }
        }

        minAmountWidth *= 1.3f
        minUnitWidth *= 1.3f

        minAmountWidth += 8.dp
        minUnitWidth += 8.dp
    }

    BoxWithConstraints {
        // try to gain more space when amount and unit fields are to wide
        if(maxWidth - minAmountWidth - minUnitWidth < 150.dp) onNotEnoughSpace()

        Column {
            if(loadingState == ErrorLoadingSuccessState.LOADING) {
                repeat(10) {
                    if(it != 0) HorizontalDivider()

                    IngredientItem(
                        ingredient = null,
                        servingsFactor = factor,

                        colors = colors,

                        minAmountWidth = minAmountWidth,
                        minUnitWidth = minUnitWidth,

                        loadingState = loadingState
                    )
                }
            } else {
                repeat(list.size) {
                    if(it != 0) HorizontalDivider()

                    val ingredient = list[it]
                    IngredientItem(
                        modifier = itemModifier(ingredient),
                        trailingContent = { itemTrailingContent(ingredient) },

                        ingredient = ingredient,
                        servingsFactor = factor,

                        colors = colors,

                        minAmountWidth = minAmountWidth,
                        minUnitWidth = minUnitWidth,

                        loadingState = loadingState
                    )
                }
            }
        }
    }
}