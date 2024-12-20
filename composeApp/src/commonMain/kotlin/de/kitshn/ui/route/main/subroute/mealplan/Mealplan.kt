package de.kitshn.ui.route.main.subroute.mealplan

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import de.kitshn.api.tandoor.model.TandoorMealPlan
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.ui.TandoorRequestErrorHandler
import de.kitshn.ui.component.alert.LoadingErrorAlertPaneWrapper
import de.kitshn.ui.dialog.mealplan.MealPlanCreationAndEditDialog
import de.kitshn.ui.dialog.mealplan.MealPlanDetailsDialog
import de.kitshn.ui.dialog.mealplan.rememberMealPlanCreationDialogState
import de.kitshn.ui.dialog.mealplan.rememberMealPlanDetailsDialogState
import de.kitshn.ui.dialog.mealplan.rememberMealPlanEditDialogState
import de.kitshn.ui.route.RouteParameters
import de.kitshn.ui.selectionMode.rememberSelectionModeState
import de.kitshn.ui.state.ErrorLoadingSuccessState
import de.kitshn.ui.state.rememberErrorLoadingSuccessState
import de.kitshn.ui.view.ViewParameters
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteMainSubrouteMealplan(
    p: RouteParameters
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    var pageLoadingState by rememberErrorLoadingSuccessState()
    val selectionModeState = rememberSelectionModeState<Int>()

    val detailsDialogState = rememberMealPlanDetailsDialogState()
    val creationDialogState =
        rememberMealPlanCreationDialogState(key = "RouteMainSubrouteMealplan/mealPlanCreationDialogState")
    val editDialogState =
        rememberMealPlanEditDialogState(key = "RouteMainSubrouteMealplan/mealPlanEditDialogState")

    val mainFetchRequestState = rememberTandoorRequestState()
    mainFetchRequestState.LoadingStateAdapter { pageLoadingState = it }

    val moveRequestState = rememberTandoorRequestState()
    val deleteRequestState = rememberTandoorRequestState()

    val shownItems = 7

    var startDate by remember { mutableStateOf(
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date) }
    val endDate = startDate.plus((shownItems - 1), DateTimeUnit.DAY)

    val mealPlanList = remember { mutableStateListOf<TandoorMealPlan>() }
    var lastMealPlanUpdate by remember { mutableLongStateOf(0L) }

    LaunchedEffect(lastMealPlanUpdate, startDate) {
        pageLoadingState = ErrorLoadingSuccessState.LOADING

        mainFetchRequestState.wrapRequest {
            p.vm.tandoorClient?.mealPlan?.fetch(
                startDate,
                startDate.plus(shownItems.toLong(), DateTimeUnit.DAY)
            )?.let {
                pageLoadingState = ErrorLoadingSuccessState.SUCCESS

                mealPlanList.clear()
                mealPlanList.addAll(
                    it
                )
            }
        }
    }

    Scaffold(
        topBar = {
            if(p.vm.tandoorClient == null) return@Scaffold
            RouteMainSubrouteMealplanTopAppBar(
                client = p.vm.tandoorClient!!,
                selectionModeState = selectionModeState,
                scrollBehavior = scrollBehavior,
                mealPlanEditDialogState = editDialogState,
                mealPlanMoveRequestState = moveRequestState,
                mealPlanDeleteRequestState = deleteRequestState
            ) { lastMealPlanUpdate = Clock.System.now().toEpochMilliseconds() }
        }
    ) {
        LoadingErrorAlertPaneWrapper(
            loadingState = pageLoadingState
        ) {
            RouteMainSubrouteMealplanScaffoldContent(
                pv = it,
                scrollBehavior = scrollBehavior,
                startDate = startDate,
                endDate = endDate,
                list = mealPlanList,
                shownItems = shownItems,
                pageLoadingState = pageLoadingState,
                selectionModeState = selectionModeState,
                detailsDialogState = detailsDialogState,
                creationDialogState = creationDialogState
            ) { date ->
                coroutineScope.launch {
                    pageLoadingState = ErrorLoadingSuccessState.LOADING
                    delay(300)
                    startDate = date
                }
            }
        }
    }

    if(p.vm.tandoorClient != null) {
        MealPlanCreationAndEditDialog(
            client = p.vm.tandoorClient!!,
            creationState = creationDialogState,
            editState = editDialogState
        ) { lastMealPlanUpdate = Clock.System.now().toEpochMilliseconds() }
    }

    MealPlanDetailsDialog(
        p = ViewParameters(
            vm = p.vm,
            back = p.onBack
        ),
        state = detailsDialogState,
        reopenOnLaunchKey = "RouteMainSubrouteMealplan/mealPlanDetailsBottomSheet",
        onUpdateList = { lastMealPlanUpdate = Clock.System.now().toEpochMilliseconds() }
    ) {
        editDialogState.open(it)
    }

    TandoorRequestErrorHandler(state = moveRequestState)
    TandoorRequestErrorHandler(state = deleteRequestState)

    TandoorRequestErrorHandler(state = mainFetchRequestState)
}