package de.kitshn.ui.component.model.shopping.recipeMealplan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntryListRecipeData
import de.kitshn.formatAmount
import de.kitshn.parseTandoorDate
import de.kitshn.toHumanReadableDateLabel
import de.kitshn.ui.theme.Typography
import de.kitshn.ui.theme.playfairDisplay
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.common_plural_portion
import org.jetbrains.compose.resources.pluralStringResource
import kotlin.math.roundToInt

@Composable
fun HorizontalListRecipeDataCard(
    data: TandoorShoppingListEntryListRecipeData,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick
    ) {
        Box {
            ListItem(
                overlineContent = if(data.meal_plan_data?.from_date != null) {
                    {
                        Text(
                            text = data.meal_plan_data.from_date.parseTandoorDate()
                                .toHumanReadableDateLabel()
                        )
                    }
                } else {
                    null
                },
                headlineContent = {
                    Text(
                        text = data.recipe_data.name,
                        style = Typography().bodyLarge.copy(
                            fontFamily = playfairDisplay()
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if(data.servings > 0.0) FilterChip(
                            onClick = { },
                            label = {
                                Text(
                                    text = pluralStringResource(
                                        resource = Res.plurals.common_plural_portion,
                                        quantity = data.servings.roundToInt(),
                                        data.servings.formatAmount(fractional = true)
                                    )
                                )
                            },
                            selected = true
                        )

                        data.meal_plan_data?.meal_type?.let { mealType ->
                            FilterChip(
                                onClick = { },
                                label = { Text(text = mealType.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedLabelColor = mealType.color,
                                    selectedContainerColor = mealType.color.copy(alpha = 0.2f)
                                ),
                                selected = true
                            )
                        }
                    }
                }
            )
        }
    }
}