package de.kitshn.ui.route.main.subroute.mealplan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults.floatingToolbarVerticalNestedScroll
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.TandoorMealPlan
import de.kitshn.parseTandoorDate
import de.kitshn.ui.component.LoadingGradientWrapper
import de.kitshn.ui.component.model.mealplan.MealPlanDayCard
import de.kitshn.ui.dialog.mealplan.MealPlanCreationAndEditDefaultValues
import de.kitshn.ui.dialog.mealplan.MealPlanCreationDialogState
import de.kitshn.ui.dialog.mealplan.MealPlanDetailsDialogState
import de.kitshn.ui.selectionMode.SelectionModeState
import de.kitshn.ui.state.ErrorLoadingSuccessState
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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
) {
    var expandedToolbar by remember { mutableStateOf(true) }

    Box(
        Modifier.fillMaxSize()
    ) {
        val coroutineScope = rememberCoroutineScope()

        LoadingGradientWrapper(
            Modifier.padding(pv),
            loadingState = pageLoadingState
        ) {
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .floatingToolbarVerticalNestedScroll(
                        expanded = expandedToolbar,
                        onExpand = { expandedToolbar = true },
                        onCollapse = { expandedToolbar = false }
                    )
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                columns = GridCells.Adaptive(300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
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
                                mealPlan.meal_type.time
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

                item {
                    Spacer(Modifier.height(72.dp))
                }
            }
        }
    }
}