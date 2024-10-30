package de.kitshn.android.ui.component.model.ingredient

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.kitshn.android.api.tandoor.model.TandoorIngredient
import de.kitshn.android.ui.modifier.loadingPlaceHolder
import de.kitshn.android.ui.state.ErrorLoadingSuccessState

@Composable
fun IngredientItem(
    modifier: Modifier = Modifier,

    ingredient: TandoorIngredient? = null,
    servingsFactor: Double = 1.0,

    trailingContent: @Composable () -> Unit = {},

    colors: ListItemColors = ListItemDefaults.colors(),

    minAmountWidth: Dp = 0.dp,
    minUnitWidth: Dp = 0.dp,

    showFractionalValues: Boolean,

    loadingState: ErrorLoadingSuccessState = ErrorLoadingSuccessState.SUCCESS
) {
    val amount = (ingredient?.amount ?: 1.0) * servingsFactor

    ListItem(
        modifier = modifier.loadingPlaceHolder(loadingState),
        colors = colors,
        trailingContent = trailingContent,
        leadingContent = {
            if(ingredient == null) return@ListItem

            Row(
                Modifier
                    .widthIn(minAmountWidth + minUnitWidth)
            ) {
                Row(
                    Modifier
                        .widthIn(minAmountWidth)
                ) {
                    if(!ingredient.no_amount && amount > 0.0) Text(
                        text = ingredient.formatAmount(amount, fractional = showFractionalValues)
                    )

                    Spacer(Modifier.width(8.dp))
                }

                Row(
                    Modifier
                        .widthIn(minUnitWidth)
                ) {
                    if(!ingredient.no_amount && ingredient.unit != null) Text(
                        text = ingredient.getUnitLabel(amount)
                    )

                    Spacer(Modifier.width(8.dp))
                }
            }
        },
        headlineContent = {
            if(ingredient == null) return@ListItem

            Text(
                text = ingredient.getLabel(amount)
            )
        },
        supportingContent = if((ingredient?.note ?: "").isNotBlank()) {
            {
                Text(
                    text = ingredient?.note ?: ""
                )
            }
        } else null
    )
}