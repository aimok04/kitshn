package de.kitshn.ui.dialog.recipe.creationandedit

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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.TandoorRequestState
import de.kitshn.api.tandoor.model.TandoorStep
import de.kitshn.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.handleTandoorRequestState
import de.kitshn.removeIf
import de.kitshn.ui.component.alert.FullSizeAlertPane
import de.kitshn.ui.component.model.recipe.step.RecipeStepCard
import de.kitshn.ui.dialog.common.CommonDeletionDialog
import de.kitshn.ui.dialog.common.rememberCommonDeletionDialogState
import de.kitshn.ui.dialog.recipe.step.StepCreationAndEditDefaultValues
import de.kitshn.ui.dialog.recipe.step.StepCreationAndEditDialog
import de.kitshn.ui.dialog.recipe.step.StepCreationDialogState
import de.kitshn.ui.dialog.recipe.step.StepEditDialogState
import de.kitshn.ui.selectionMode.SelectionModeState
import de.kitshn.ui.selectionMode.values.selectionModeCardColors
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_add
import kitshn.composeapp.generated.resources.action_delete
import kitshn.composeapp.generated.resources.action_edit
import kitshn.composeapp.generated.resources.action_reorder
import kitshn.composeapp.generated.resources.recipe_edit_no_steps_added
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun StepsPage(
    selectionState: SelectionModeState<Int>,
    recipe: TandoorRecipe?,
    values: RecipeCreationAndEditDialogValue,
    showFractionalValues: Boolean
) {
    val coroutineScope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current

    val stepById = remember { mutableStateMapOf<Int, TandoorStep>() }
    LaunchedEffect(recipe, values._stepsUpdate) {
        stepById.clear()
        recipe?.steps?.forEach { stepById[it.id] = it }

        values.stepsOrder.removeIf { stepById[it]?._destroyed != false }
    }

    val stepCreationDialogState = remember { StepCreationDialogState() }
    val stepEditDialogState = remember { StepEditDialogState() }

    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        values.stepsOrder.apply { add(to.index, removeAt(from.index)) }
        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
    }

    val stepDeletionDialogState = rememberCommonDeletionDialogState<TandoorStep>()

    var blockRendering by remember { mutableStateOf(false) }
    LaunchedEffect(values._stepsUpdate) {
        if(values._stepsUpdate == 0L) return@LaunchedEffect

        blockRendering = true
        delay(50)
        blockRendering = false
    }

    Box(
        Modifier.fillMaxSize()
    ) {
        if(values.stepsOrder.size == 0) {
            FullSizeAlertPane(
                imageVector = Icons.Rounded.QuestionMark,
                contentDescription = stringResource(Res.string.recipe_edit_no_steps_added),
                text = stringResource(Res.string.recipe_edit_no_steps_added)
            )
        } else {
            if(blockRendering) return

            LazyColumn(
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                state = lazyListState
            ) {
                items(values.stepsOrder.size, key = { values.stepsOrder[it] }) { index ->
                    val step = stepById[values.stepsOrder[index]] ?: return@items
                    if(step._destroyed) return@items

                    val interactionSource = remember { MutableInteractionSource() }
                    ReorderableItem(
                        state = reorderableLazyListState,
                        key = step.id,
                        animateItemModifier = Modifier
                    ) {
                        // fetch step_recipe if not null for recipe search field when editing step
                        LaunchedEffect(step.id) {
                            if(step.step_recipe == null) return@LaunchedEffect
                            TandoorRequestState().wrapRequest {
                                step.client!!.container.recipeOverview[step.step_recipe!!] =
                                    step.client!!.recipe.get(
                                        step.step_recipe!!
                                    ).toOverview()
                            }
                        }

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
                                                hapticFeedback.performHapticFeedback(
                                                    HapticFeedbackType.LongPress
                                                )
                                            },
                                            onDragStopped = {
                                                hapticFeedback.performHapticFeedback(
                                                    HapticFeedbackType.GestureEnd
                                                )
                                            },
                                            interactionSource = interactionSource
                                        )
                                    ) {
                                        Icon(
                                            Icons.Rounded.DragHandle,
                                            contentDescription = stringResource(Res.string.action_reorder)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            stepEditDialogState.open(step)
                                        }
                                    ) {
                                        Icon(
                                            Icons.Rounded.Edit,
                                            stringResource(Res.string.action_edit)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            stepDeletionDialogState.open(step)
                                        }
                                    ) {
                                        Icon(
                                            Icons.Rounded.Delete,
                                            stringResource(Res.string.action_delete)
                                        )
                                    }
                                }
                            },
                            showFractionalValues = showFractionalValues,
                            onClickRecipeLink = { },
                            onStartTimer = { seconds, timerName ->

                            }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            modifier = Modifier.padding(16.dp)
                .align(Alignment.BottomEnd),
            onClick = {
                recipe?.let {
                    stepCreationDialogState.open(
                        recipe = it,
                        values = StepCreationAndEditDefaultValues()
                    )
                }
            }
        ) {
            Icon(Icons.Rounded.Add, stringResource(Res.string.action_add))
        }
    }

    CommonDeletionDialog(
        state = stepDeletionDialogState,
        onConfirm = { step ->
            coroutineScope.launch {
                val request = TandoorRequestState().apply {
                    wrapRequest {
                        recipe?.deleteStep(step)
                        values.updateSteps()
                    }
                }

                hapticFeedback.handleTandoorRequestState(request)
            }
        }
    )

    recipe?.client?.let {
        StepCreationAndEditDialog(
            client = it,
            creationState = stepCreationDialogState,
            editState = stepEditDialogState,
            onCreate = { step ->
                recipe.steps.add(step)
                values.stepsOrder.add(step.id)

                values.updateSteps()
            },
            onUpdate = { updatedStep ->
                val index = recipe.steps.indexOfFirst { it.id == updatedStep.id }
                recipe.steps[index] = updatedStep
                values.updateSteps()
            }
        )
    }
}