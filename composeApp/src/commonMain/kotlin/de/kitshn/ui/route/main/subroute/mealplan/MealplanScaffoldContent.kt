package de.kitshn.ui.route.main.subroute.mealplan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.TandoorMealPlan
import de.kitshn.parseTandoorDate
import de.kitshn.toHumanReadableDateLabel
import de.kitshn.toLocalDate
import de.kitshn.ui.component.LoadingGradientWrapper
import de.kitshn.ui.component.model.mealplan.MealPlanDayCard
import de.kitshn.ui.dialog.mealplan.MealPlanCreationAndEditDefaultValues
import de.kitshn.ui.dialog.mealplan.MealPlanCreationDialogState
import de.kitshn.ui.dialog.mealplan.MealPlanDetailsDialogState
import de.kitshn.ui.selectionMode.SelectionModeState
import de.kitshn.ui.state.ErrorLoadingSuccessState
import de.kitshn.ui.theme.Typography
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_minus_one_week
import kitshn.composeapp.generated.resources.action_plus_one_week
import kitshn.composeapp.generated.resources.common_okay
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteMainSubrouteMealplanScaffoldContent(
    client: TandoorClient,

    pv: PaddingValues,
    scrollBehavior: TopAppBarScrollBehavior,

    startDate: LocalDate,
    endDate: LocalDate,

    shownItems: Int = 7,

    list: List<TandoorMealPlan>,

    pageLoadingState: ErrorLoadingSuccessState,
    selectionModeState: SelectionModeState<Int>,

    detailsDialogState: MealPlanDetailsDialogState,
    creationDialogState: MealPlanCreationDialogState,

    onChangeMealPlanStartDate: (day: LocalDate) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var showDatePickerDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if(showDatePickerDialog) DatePickerDialog(
        onDismissRequest = { showDatePickerDialog = false },
        confirmButton = {
            Button(
                onClick = {
                    if(datePickerState.selectedDateMillis == null) return@Button

                    showDatePickerDialog = false
                    onChangeMealPlanStartDate(
                        datePickerState.selectedDateMillis!!.toLocalDate(
                            TimeZone.UTC
                        )
                    )
                }
            ) {
                Text(stringResource(Res.string.common_okay))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }

    LoadingGradientWrapper(
        Modifier.padding(pv),
        loadingState = pageLoadingState
    ) {
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            columns = GridCells.Adaptive(300.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            item(
                span = { GridItemSpan(maxCurrentLineSpan) }
            ) {
                Row(
                    Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SmallFloatingActionButton(
                        onClick = {
                            onChangeMealPlanStartDate(startDate.minus(7, DateTimeUnit.DAY))
                        },
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 0.dp
                        )
                    ) {
                        Icon(Icons.Rounded.Remove, stringResource(Res.string.action_minus_one_week))
                    }

                    Spacer(Modifier.width(4.dp))

                    AssistChip(
                        onClick = {
                            showDatePickerDialog = true
                        },
                        label = {
                            Text(
                                modifier = Modifier.padding(12.dp),
                                style = Typography().labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                text = "${startDate.toHumanReadableDateLabel()} — ${endDate.toHumanReadableDateLabel()}"
                            )
                        }
                    )

                    Spacer(Modifier.width(4.dp))

                    SmallFloatingActionButton(
                        onClick = {
                            onChangeMealPlanStartDate(startDate.plus(7, DateTimeUnit.DAY))
                        },
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 0.dp
                        )
                    ) {
                        Icon(Icons.Rounded.Add, stringResource(Res.string.action_plus_one_week))
                    }
                }
            }

            items(
                count = shownItems,
                span = { index ->
                    if((index + 1) == shownItems) {
                        GridItemSpan(maxCurrentLineSpan)
                    } else {
                        GridItemSpan(1)
                    }
                }
            ) { index ->
                val day = startDate.plus(index.toLong(), DateTimeUnit.DAY)

                Column {
                    MealPlanDayCard(
                        day = day,
                        mealPlanItems = list.toMutableList().filter { mealPlan ->
                            mealPlan.from_date.parseTandoorDate() == day || mealPlan.to_date.parseTandoorDate() == day
                        }.sortedBy { mealPlan ->
                            mealPlan.meal_type.order
                        },

                        loadingState = pageLoadingState,
                        selectionState = selectionModeState,

                        onClick = { mealPlan ->
                            detailsDialogState.open(
                                linkContent = mealPlan
                            )
                        }
                    ) {
                        coroutineScope.launch {
                            val userPreference = client.userPreference.fetch()

                            creationDialogState.open(
                                MealPlanCreationAndEditDefaultValues(
                                    startDate = day,
                                    shared = userPreference.plan_share
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}