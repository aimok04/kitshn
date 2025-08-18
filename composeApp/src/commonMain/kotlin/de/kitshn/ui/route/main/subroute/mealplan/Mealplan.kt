package de.kitshn.ui.route.main.subroute.mealplan

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.model.TandoorMealPlan
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.handleTandoorRequestState
import de.kitshn.toHumanReadableDateLabel
import de.kitshn.toLocalDate
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
import de.kitshn.ui.theme.Typography
import de.kitshn.ui.view.ViewParameters
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_minus_one_week
import kitshn.composeapp.generated.resources.action_plus_one_week
import kitshn.composeapp.generated.resources.common_okay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RouteMainSubrouteMealplan(
    p: RouteParameters
) {
    val coroutineScope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current

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

    var startDate by remember {
        mutableStateOf(
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        )
    }
    val endDate = startDate.plus((shownItems - 1), DateTimeUnit.DAY)

    val mealPlanList = remember { mutableStateListOf<TandoorMealPlan>() }
    var lastMealPlanUpdate by remember { mutableLongStateOf(0L) }

    var initialLoad by remember { mutableStateOf(true) }

    LaunchedEffect(lastMealPlanUpdate, startDate) {
        pageLoadingState = ErrorLoadingSuccessState.LOADING

        mainFetchRequestState.wrapRequest {
            p.vm.tandoorClient?.mealPlan?.fetch(
                startDate.minus(1, DateTimeUnit.DAY),
                startDate.plus(shownItems.toLong(), DateTimeUnit.DAY)
                    .plus(1, DateTimeUnit.DAY)
            )?.let {
                pageLoadingState = ErrorLoadingSuccessState.SUCCESS

                mealPlanList.clear()
                mealPlanList.addAll(
                    it
                )
            }
        }

        if(initialLoad) {
            initialLoad = false
            return@LaunchedEffect
        }

        // update details dialog
        if(detailsDialogState.shown.value) {
            val entry = mealPlanList.find { it.id == detailsDialogState.linkContent.value?.id }
            if(entry != null) {
                detailsDialogState.dismiss()
                delay(250)
                detailsDialogState.open(entry)
            }
        }

        hapticFeedback.handleTandoorRequestState(mainFetchRequestState)
    }

    var showDatePickerDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

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
        },
        floatingActionButton = {
            HorizontalFloatingToolbar(
                colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
                expanded = true,
                content = {
                    FilledIconButton(
                        onClick = {
                            coroutineScope.launch {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)

                                pageLoadingState = ErrorLoadingSuccessState.LOADING
                                delay(300)
                                startDate = startDate.minus(7, DateTimeUnit.DAY)
                            }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Rounded.Remove, stringResource(Res.string.action_minus_one_week))
                    }

                    Spacer(Modifier.width(4.dp))

                    AssistChip(
                        onClick = {
                            showDatePickerDialog = true
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            labelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        border = null,
                        shape = CircleShape,
                        label = {
                            AnimatedContent(
                                targetState = startDate
                            ) {
                                Text(
                                    modifier = Modifier.padding(12.dp),
                                    style = Typography().labelMedium,
                                    text = "${it.toHumanReadableDateLabel()} — ${endDate.toHumanReadableDateLabel()}"
                                )
                            }
                        }
                    )

                    Spacer(Modifier.width(4.dp))

                    FilledIconButton(
                        onClick = {
                            coroutineScope.launch {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)

                                pageLoadingState = ErrorLoadingSuccessState.LOADING
                                delay(300)
                                startDate = startDate.plus(7, DateTimeUnit.DAY)
                            }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Rounded.Add, stringResource(Res.string.action_plus_one_week))
                    }
                }
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) {
        LoadingErrorAlertPaneWrapper(
            loadingState = pageLoadingState
        ) {
            if(p.vm.tandoorClient == null) return@LoadingErrorAlertPaneWrapper
            RouteMainSubrouteMealplanScaffoldContent(
                client = p.vm.tandoorClient!!,
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
            )
        }
    }

    if(p.vm.tandoorClient != null) {
        val showFractionalValues =
            p.vm.settings.getIngredientsShowFractionalValues.collectAsState(initial = true)

        MealPlanCreationAndEditDialog(
            client = p.vm.tandoorClient!!,
            creationState = creationDialogState,
            editState = editDialogState,
            showFractionalValues = showFractionalValues.value
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

    if(showDatePickerDialog) DatePickerDialog(
        onDismissRequest = { showDatePickerDialog = false },
        confirmButton = {
            Button(
                onClick = {
                    if(datePickerState.selectedDateMillis == null) return@Button

                    showDatePickerDialog = false
                    coroutineScope.launch {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)

                        pageLoadingState = ErrorLoadingSuccessState.LOADING
                        delay(300)
                        startDate = datePickerState.selectedDateMillis!!.toLocalDate(
                            TimeZone.UTC
                        )
                    }
                }
            ) {
                Text(stringResource(Res.string.common_okay))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }

    TandoorRequestErrorHandler(state = moveRequestState)
    TandoorRequestErrorHandler(state = deleteRequestState)

    TandoorRequestErrorHandler(state = mainFetchRequestState)
}