package de.kitshn.ui.route.main.subroute.mealplan

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoveDown
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorRequestState
import de.kitshn.toLocalDate
import de.kitshn.ui.component.icons.IconWithState
import de.kitshn.ui.dialog.mealplan.MealPlanEditDialogState
import de.kitshn.ui.selectionMode.SelectionModeState
import de.kitshn.ui.selectionMode.component.SelectionModeTopAppBar
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_delete
import kitshn.composeapp.generated.resources.action_edit
import kitshn.composeapp.generated.resources.action_move
import kitshn.composeapp.generated.resources.navigation_meal_plan
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteMainSubrouteMealplanTopAppBar(
    client: TandoorClient,
    selectionModeState: SelectionModeState<Int>,
    scrollBehavior: TopAppBarScrollBehavior,

    mealPlanEditDialogState: MealPlanEditDialogState,
    mealPlanMoveRequestState: TandoorRequestState,
    mealPlanDeleteRequestState: TandoorRequestState,

    onUpdatePlan: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    SelectionModeTopAppBar(
        topAppBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.navigation_meal_plan)) },
                scrollBehavior = scrollBehavior
            )
        },
        state = selectionModeState,
        actions = {
            val datePickerState = rememberDatePickerState()

            if(selectionModeState.selectedItems.size == 1) IconButton(onClick = {
                coroutineScope.launch {
                    val mealPlan = client.container.mealPlan[
                        selectionModeState.selectedItems[0]
                    ] ?: return@launch

                    selectionModeState.disable()
                    mealPlanEditDialogState.open(mealPlan)
                }
            }) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = stringResource(Res.string.action_edit)
                )
            }

            var showDateMoveDialog by remember { mutableStateOf(false) }
            if(showDateMoveDialog) DatePickerDialog(
                onDismissRequest = { showDateMoveDialog = false },
                confirmButton = {
                    Button(onClick = {
                        val date =
                            datePickerState.selectedDateMillis?.toLocalDate() ?: return@Button

                        coroutineScope.launch {
                            selectionModeState.selectedItems.forEach {
                                val mealPlan = client.container.mealPlan[it] ?: return@forEach
                                mealPlanMoveRequestState.wrapRequest {
                                    mealPlan.partialUpdate(
                                        from_date = date,
                                        to_date = date
                                    )
                                }
                            }

                            onUpdatePlan()
                            selectionModeState.disable()

                            delay(100)
                            mealPlanMoveRequestState.reset()
                        }
                    }) {
                        Text(stringResource(Res.string.action_move))
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }

            IconButton(onClick = {
                showDateMoveDialog = true
            }) {
                IconWithState(
                    imageVector = Icons.Rounded.MoveDown,
                    contentDescription = stringResource(Res.string.action_move),
                    state = mealPlanMoveRequestState.state.toIconWithState()
                )
            }

            IconButton(onClick = {
                coroutineScope.launch {
                    selectionModeState.selectedItems.forEach {
                        val mealPlan = client.container.mealPlan[it] ?: return@forEach
                        mealPlanDeleteRequestState.wrapRequest { mealPlan.delete() }
                    }

                    onUpdatePlan()
                    selectionModeState.disable()

                    delay(100)
                    mealPlanDeleteRequestState.reset()
                }
            }) {
                IconWithState(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = stringResource(Res.string.action_delete),
                    state = mealPlanDeleteRequestState.state.toIconWithState()
                )
            }
        }
    )
}