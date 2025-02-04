package de.kitshn.ui.component.model.shopping.recipeMealplan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntryRecipeMealplan
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
fun HorizontalRecipeMealPlanCard(
    recipeMealplan: TandoorShoppingListEntryRecipeMealplan,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick
    ) {
        Box {
            ListItem(
                overlineContent = if(recipeMealplan.mealplan_from_date != null) {
                    {
                        Text(
                            text = recipeMealplan.mealplan_from_date.parseTandoorDate()
                                .toHumanReadableDateLabel()
                        )
                    }
                } else {
                    null
                },
                headlineContent = {
                    Text(
                        text = recipeMealplan.recipe_name,
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
                        if(recipeMealplan.servings > 0.0) FilterChip(
                            onClick = { },
                            label = {
                                Text(
                                    text = pluralStringResource(
                                        resource = Res.plurals.common_plural_portion,
                                        quantity = recipeMealplan.servings.roundToInt(),
                                        recipeMealplan.servings.formatAmount(fractional = true)
                                    )
                                )
                            },
                            selected = true
                        )

                        if(recipeMealplan.mealplan_type != null) FilterChip(
                            onClick = { },
                            label = {
                                Text(
                                    text = recipeMealplan.mealplan_type
                                )
                            },
                            selected = true
                        )
                    }
                }
            )
        }
    }
}