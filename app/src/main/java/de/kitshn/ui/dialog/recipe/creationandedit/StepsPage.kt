package de.kitshn.ui.dialog.recipe.creationandedit

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.kitshn.R
import de.kitshn.api.tandoor.model.TandoorStep
import de.kitshn.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.ui.component.alert.FullSizeAlertPane
import de.kitshn.ui.component.model.recipe.step.RecipeStepCard
import de.kitshn.ui.dialog.common.CommonDeletionDialog
import de.kitshn.ui.dialog.common.rememberCommonDeletionDialogState
import de.kitshn.ui.selectionMode.SelectionModeState
import de.kitshn.ui.selectionMode.values.selectionModeCardColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StepsPage(
    selectionState: SelectionModeState<Int>,
    recipe: TandoorRecipe?,
    values: RecipeCreationAndEditDialogValue,
    showFractionalValues: Boolean
) {
    val hapticFeedback = LocalHapticFeedback.current

    val coroutineScope = rememberCoroutineScope()
    val view = LocalView.current

    val stepById = remember { mutableStateMapOf<Int, TandoorStep>() }
    LaunchedEffect(recipe, values._stepsUpdate) {
        stepById.clear()
        recipe?.steps?.forEach { stepById[it.id] = it }

        values.stepsOrder.removeIf { stepById[it]?._destroyed != false }
    }

    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        values.stepsOrder.apply { add(to.index, removeAt(from.index)) }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            view.performHapticFeedback(HapticFeedbackConstants.SEGMENT_FREQUENT_TICK)
        }
    }

    val stepDeletionDialogState = rememberCommonDeletionDialogState<TandoorStep>()

    var blockRendering by remember { mutableStateOf(false) }
    LaunchedEffect(values._stepsUpdate) {
        if(values._stepsUpdate == 0L) return@LaunchedEffect

        blockRendering = true
        delay(50)
        blockRendering = false
    }

    if(blockRendering) return

    Box(
        Modifier.fillMaxSize()
    ) {
        if(values.stepsOrder.size == 0) {
            FullSizeAlertPane(
                imageVector = Icons.Rounded.QuestionMark,
                contentDescription = stringResource(R.string.recipe_edit_no_steps_added),
                text = stringResource(R.string.recipe_edit_no_steps_added)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                state = lazyListState
            ) {
                items(values.stepsOrder.size, key = { values.stepsOrder[it] }) { index ->
                    val step = stepById[values.stepsOrder[index]] ?: return@items
                    if(step._destroyed) return@items

                    val interactionSource = remember { MutableInteractionSource() }
                    ReorderableItem(state = reorderableLazyListState, key = step.id) {
                        RecipeStepCard(
                            columnModifier = Modifier.combinedClickable(
                                onClick = {
                                    if(selectionState.isSelectionModeEnabled()) {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectionState.selectToggle(step.id)
                                    }
                                },
                                onLongClick = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectionState.selectToggle(step.id)
                                }
                            ),
                            colors = CardDefaults.selectionModeCardColors(
                                selected = selectionState.selectedItems.contains(step.id),
                                defaultCardColors = CardDefaults.cardColors()
                            ),
                            interactionSource = interactionSource,
                            servingsFactor = 1.0,
                            recipe = recipe,
                            step = step,
                            stepIndex = index,
                            appendAction = {
                                Row(
                                    Modifier.padding(top = 8.dp, end = 8.dp)
                                ) {
                                    IconButton(
                                        onClick = { },
                                        modifier = Modifier.longPressDraggableHandle(
                                            onDragStarted = {
                                                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                                    view.performHapticFeedback(
                                                        HapticFeedbackConstants.DRAG_START
                                                    )
                                                }
                                            },
                                            onDragStopped = {
                                                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                                    view.performHapticFeedback(
                                                        HapticFeedbackConstants.GESTURE_END
                                                    )
                                                }
                                            },
                                            interactionSource = interactionSource
                                        )
                                    ) {
                                        Icon(
                                            Icons.Rounded.DragHandle,
                                            contentDescription = stringResource(R.string.action_reorder)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            stepDeletionDialogState.open(step)
                                        }
                                    ) {
                                        Icon(
                                            Icons.Rounded.Delete,
                                            stringResource(id = R.string.action_delete)
                                        )
                                    }
                                }
                            },
                            showFractionalValues = showFractionalValues,
                            onClickRecipeLink = { }
                        )
                    }
                }
            }
        }
    }

    CommonDeletionDialog(
        state = stepDeletionDialogState,
        onConfirm = { step ->
            coroutineScope.launch {
                recipe?.deleteStep(step)
                values.updateSteps()
            }
        }
    )
}