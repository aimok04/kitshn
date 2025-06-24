package de.kitshn.ui.component.model.mealplan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.model.TandoorMealPlan
import de.kitshn.toHumanReadableDateLabel
import de.kitshn.ui.modifier.loadingPlaceHolder
import de.kitshn.ui.selectionMode.SelectionModeState
import de.kitshn.ui.state.ErrorLoadingSuccessState
import de.kitshn.ui.theme.Typography
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_add
import kitshn.composeapp.generated.resources.lorem_ipsum_title
import kitshn.composeapp.generated.resources.meal_plan_no_recipes_for_this_day
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
fun MealPlanDayCard(
    modifier: Modifier = Modifier,

    day: LocalDate?,
    mealPlanItems: List<TandoorMealPlan>?,

    loadingState: ErrorLoadingSuccessState = ErrorLoadingSuccessState.SUCCESS,
    selectionState: SelectionModeState<Int>? = null,

    onClick: (mealPlan: TandoorMealPlan) -> Unit,
    onClickCreate: () -> Unit
) {
    Card(
        modifier = modifier
            .widthIn(min = 150.dp)
            .fillMaxWidth(),
        onClick = { }
    ) {
        Column(
            Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    modifier = Modifier.loadingPlaceHolder(loadingState),
                    text = day?.toHumanReadableDateLabel()
                        ?: stringResource(Res.string.lorem_ipsum_title),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = Typography().titleLarge
                )

                FilledTonalIconButton(onClick = onClickCreate) {
                    Icon(Icons.Rounded.Add, stringResource(Res.string.action_add))
                }
            }

            Spacer(Modifier.height(8.dp))

            Box(
                Modifier.loadingPlaceHolder(loadingState)
            ) {
                if(mealPlanItems == null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                    ) { }
                } else {
                    if(mealPlanItems.isEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .loadingPlaceHolder(loadingState),
                            colors = CardDefaults.elevatedCardColors()
                        ) {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                text = stringResource(Res.string.meal_plan_no_recipes_for_this_day),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        mealPlanItems.forEach {
                            HorizontalMealPlanCard(
                                mealPlan = it,
                                selectionState = selectionState,
                                onClick = { onClick(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}