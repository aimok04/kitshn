package de.kitshn.android.ui.dialog.mealplan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.kitshn.android.R
import de.kitshn.android.api.tandoor.model.TandoorMealPlan
import de.kitshn.android.api.tandoor.rememberTandoorRequestState
import de.kitshn.android.ui.TandoorRequestErrorHandler
import de.kitshn.android.ui.component.icons.IconWithState
import de.kitshn.android.ui.component.model.mealplan.MealPlanDetailsCard
import de.kitshn.android.ui.dialog.recipe.RecipeLinkBottomSheet
import de.kitshn.android.ui.dialog.recipe.rememberRecipeLinkBottomSheetState
import de.kitshn.android.ui.state.foreverRememberNotSavable
import de.kitshn.android.ui.theme.Typography
import de.kitshn.android.ui.view.ViewParameters
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun rememberMealPlanDetailsBottomSheetState(): MealPlanDetailsBottomSheetState {
    return remember {
        MealPlanDetailsBottomSheetState()
    }
}

class MealPlanDetailsBottomSheetState(
    val shown: MutableState<Boolean> = mutableStateOf(false),
    val linkContent: MutableState<TandoorMealPlan?> = mutableStateOf(null)
) {
    fun open(linkContent: TandoorMealPlan) {
        this.linkContent.value = linkContent
        this.shown.value = true
    }

    fun dismiss() {
        this.shown.value = false
        this.linkContent.value = null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanDetailsBottomSheet(
    p: ViewParameters,
    state: MealPlanDetailsBottomSheetState,
    reopenOnLaunchKey: String? = null,
    onUpdateList: () -> Unit,
    onEdit: (mealPlan: TandoorMealPlan) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val requestMealPlanDeleteState = rememberTandoorRequestState()

    if(reopenOnLaunchKey != null) {
        var reopenOnLaunch by foreverRememberNotSavable<TandoorMealPlan?>(key = reopenOnLaunchKey)
        DisposableEffect(Unit) {
            onDispose {
                reopenOnLaunch = state.shown.value.run {
                    if(!this)
                        null
                    else
                        state.linkContent.value
                }
            }
        }

        LaunchedEffect(Unit) {
            if(reopenOnLaunch == null) return@LaunchedEffect
            state.open(reopenOnLaunch!!)

            reopenOnLaunch = null
        }
    }

    if(state.linkContent.value == null) return
    val mealPlan = state.linkContent.value!!

    val dragHandle = @Composable {
        Box(
            Modifier.fillMaxWidth()
        ) {
            BottomSheetDefaults.DragHandle(
                Modifier.align(Alignment.TopCenter)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = mealPlan.meal_type_name,
                    style = Typography.headlineSmall
                )

                Row {
                    IconButton(
                        onClick = {
                            onEdit(mealPlan)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = stringResource(R.string.action_edit)
                        )
                    }

                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                val data = requestMealPlanDeleteState.wrapRequest {
                                    mealPlan.delete()
                                }

                                if(data == null) return@launch

                                state.dismiss()
                                requestMealPlanDeleteState.reset()

                                onUpdateList()
                            }
                        }
                    ) {
                        IconWithState(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = stringResource(R.string.action_delete_from_meal_plan),
                            state = requestMealPlanDeleteState.state.toIconWithState()
                        )
                    }
                }
            }
        }
    }

    if(mealPlan.recipe != null) {
        val recipeLinkBottomSheetState = rememberRecipeLinkBottomSheetState()

        LaunchedEffect(state.shown.value) {
            if(state.shown.value) {
                recipeLinkBottomSheetState.open(
                    linkContent = mealPlan.recipe,
                    overrideServings = mealPlan.servings.roundToInt()
                )
            } else {
                recipeLinkBottomSheetState.dismiss()
            }
        }

        LaunchedEffect(recipeLinkBottomSheetState.shown.value) {
            state.shown.value = recipeLinkBottomSheetState.shown.value
        }

        RecipeLinkBottomSheet(
            p = p,
            state = recipeLinkBottomSheetState,
            dragHandle = dragHandle,
            leadingContent = {
                MealPlanDetailsCard(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    mealPlan = mealPlan
                )
            }
        ) {
            state.dismiss()
        }
    } else {
        val density = LocalDensity.current
        val modalBottomSheetState = rememberModalBottomSheetState()

        LaunchedEffect(
            state.shown.value
        ) {
            if(state.shown.value) {
                modalBottomSheetState.show()
            } else {
                modalBottomSheetState.hide()
            }
        }

        ModalBottomSheet(
            modifier = Modifier.padding(
                top = with(density) {
                    WindowInsets.statusBars
                        .getTop(density)
                        .toDp() * 2
                }
            ),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            onDismissRequest = {
                state.dismiss()
            },
            sheetState = modalBottomSheetState,
            dragHandle = dragHandle
        ) {
            MealPlanDetailsCard(
                modifier = Modifier.padding(16.dp),
                mealPlan = mealPlan
            )
        }
    }

    TandoorRequestErrorHandler(state = requestMealPlanDeleteState)
}