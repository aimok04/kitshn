package de.kitshn.ui.component.model.mealplan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.model.TandoorMealPlan
import de.kitshn.ui.theme.Typography
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.common_plural_portion
import org.jetbrains.compose.resources.pluralStringResource
import kotlin.math.roundToInt

@Composable
fun MealPlanDetailsCard(
    modifier: Modifier,
    mealPlan: TandoorMealPlan
) {
    if(mealPlan.title.isNotBlank() || !mealPlan.note.isNullOrBlank()) Card(
        modifier = modifier
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            SelectionContainer {
                if(mealPlan.title.isNotBlank()) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .weight(1f, true),
                            text = mealPlan.title,
                            style = Typography().titleLarge
                        )

                        if(mealPlan.recipe == null) Text(
                            modifier = Modifier
                                .weight(1f, false)
                                .padding(top = 6.dp),
                            text = pluralStringResource(
                                Res.plurals.common_plural_portion,
                                mealPlan.servings.roundToInt(),
                                mealPlan.servings.roundToInt()
                            ),
                            style = Typography().labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                }

                if(!mealPlan.note.isNullOrBlank()) Text(
                    text = mealPlan.note ?: ""
                )
            }
        }
    }
}