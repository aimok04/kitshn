package de.kitshn.android.ui.dialog.recipe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.kitshn.android.R
import de.kitshn.android.api.tandoor.TandoorRequestStateState
import de.kitshn.android.api.tandoor.model.TandoorIngredient
import de.kitshn.android.api.tandoor.model.TandoorStep
import de.kitshn.android.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.android.api.tandoor.rememberTandoorRequestState
import de.kitshn.android.ui.TandoorRequestErrorHandler
import de.kitshn.android.ui.component.MarkdownRichTextWithTimerDetection
import de.kitshn.android.ui.component.model.ingredient.IngredientsList
import de.kitshn.android.ui.component.model.recipe.step.RecipeStepIndicator
import de.kitshn.android.ui.component.model.recipe.step.RecipeStepMultimediaBox
import de.kitshn.android.ui.dialog.AdaptiveFullscreenDialog
import de.kitshn.android.ui.layout.ResponsiveSideBySideLayout
import de.kitshn.android.ui.state.foreverRememberNotSavable
import de.kitshn.android.ui.state.foreverRememberPagerState
import de.kitshn.android.ui.theme.Typography
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun rememberRecipeIngredientAllocationDialogState(
    key: String
): RecipeIngredientAllocationDialogState {
    val value by foreverRememberNotSavable(
        key = key,
        initialValue = RecipeIngredientAllocationDialogState().apply {
            this.rememberKey = key
        }
    )

    return value
}

class RecipeIngredientAllocationDialogState(
    val shown: MutableState<Boolean> = mutableStateOf(false),
    val linkContent: MutableState<TandoorRecipe?> = mutableStateOf(null)
) {
    internal var rememberKey: String = "RecipeIngredientAllocationDialog"

    val unallocatedIngredientIdList = mutableStateListOf<Int>()
    val ingredientIdListByStepId = mutableStateMapOf<Int, MutableList<Int>>()

    fun open(linkContent: TandoorRecipe) {
        unallocatedIngredientIdList.clear()
        ingredientIdListByStepId.clear()

        this.linkContent.value = linkContent
        this.shown.value = true
    }

    fun dismiss() {
        this.shown.value = false
        this.linkContent.value = null
    }
}

@Composable
fun RecipeIngredientAllocationDialog(
    state: RecipeIngredientAllocationDialogState,
    onRefresh: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    if(!state.shown.value) return
    val recipe = state.linkContent.value!!

    val partialUpdateRequestState = rememberTandoorRequestState()

    var showSavingAlertDialog by rememberSaveable { mutableStateOf(false) }
    var savedSteps by rememberSaveable { mutableIntStateOf(0) }

    var showUnallocatedIngredientIdListNotEmptyError by rememberSaveable { mutableStateOf(false) }

    val ingredients = remember { mutableStateListOf<TandoorIngredient>() }
    val ingredientById = remember { mutableStateMapOf<Int, TandoorIngredient>() }

    val stepById = remember { mutableStateMapOf<Int, TandoorStep>() }
    val stepByIngredientId = remember { mutableStateMapOf<Int, TandoorStep>() }

    val pagerState = foreverRememberPagerState(
        key = "${state.rememberKey}/${recipe.id}/pagerState"
    ) {
        recipe.steps.size
    }

    LaunchedEffect(Unit, state.linkContent.value, state.shown.value) {
        ingredients.clear()
        stepById.clear()

        val populate = state.ingredientIdListByStepId.size == 0
        val populateEmpty = recipe.showIngredientAllocationActionChipSync()

        recipe.steps.forEach { step ->
            stepById[step.id] = step
            if(populate) state.ingredientIdListByStepId[step.id] = mutableListOf()

            step.ingredients.forEach { ing ->
                ingredients.add(ing)

                ingredientById[ing.id] = ing
                stepByIngredientId[ing.id] = step

                if(populate) if(populateEmpty) {
                    state.unallocatedIngredientIdList.add(ing.id)
                } else {
                    state.ingredientIdListByStepId[step.id]?.add(ing.id)
                }
            }
        }
    }

    AdaptiveFullscreenDialog(
        onDismiss = { state.dismiss() },
        title = {
            Text(
                text = stringResource(R.string.action_allocate_ingredients)
            )
        },
        topAppBarActions = {
            FilledIconButton(
                onClick = {
                    if(state.unallocatedIngredientIdList.size > 0) {
                        showUnallocatedIngredientIdListNotEmptyError = true
                        return@FilledIconButton
                    }

                    coroutineScope.launch {
                        state.ingredientIdListByStepId.forEach stepForeach@{ (stepId, ingredientIdList) ->
                            val step = stepById[stepId]
                                ?: throw Exception("Could not fetch step $stepId")

                            val rawIngredientsList = mutableListOf<JSONObject>()

                            ingredientIdList.forEach { ingredientId ->
                                val rawIngredient =
                                    stepByIngredientId[ingredientId]?.getRawIngredientById(
                                        ingredientId
                                    )
                                        ?: throw Exception("Could not extract raw ingredient in step $stepId for ingredient $ingredientId")

                                rawIngredientsList.add(JSONObject(rawIngredient.toString()))
                            }

                            partialUpdateRequestState.wrapRequest {
                                savedSteps = 0
                                showSavingAlertDialog = true

                                val ingredientsJSONArray = JSONArray().apply {
                                    repeat(rawIngredientsList.size) { index ->
                                        val rawIngredient = rawIngredientsList[index]

                                        rawIngredient.remove("id")
                                        if(!rawIngredient.isNull("food")) {
                                            val food = rawIngredient.getJSONObject("food")
                                            food.remove("id")
                                            food.remove("parent")
                                            food.remove("numchild")
                                            rawIngredient.put("food", food)
                                        }

                                        if(!rawIngredient.isNull("unit")) {
                                            val unit = rawIngredient.getJSONObject("unit")
                                            unit.remove("id")
                                            rawIngredient.put("unit", unit)
                                        }

                                        rawIngredient.remove("used_in_recipes")
                                        rawIngredient.put("order", index)

                                        put(rawIngredient)
                                    }
                                }

                                step.partialUpdate(ingredientsRaw = ingredientsJSONArray)
                                savedSteps++
                            }

                            if(partialUpdateRequestState.state == TandoorRequestStateState.ERROR) {
                                // fixing bug
                                if(partialUpdateRequestState.error != null) if(partialUpdateRequestState.error?.volleyError?.networkResponse?.statusCode == 500)
                                    return@stepForeach

                                return@launch
                            }
                        }

                        showSavingAlertDialog = false

                        state.dismiss()
                        onRefresh()
                    }
                }
            ) {
                Icon(Icons.Rounded.Save, stringResource(id = R.string.action_save))
            }
        },
        bottomBar = {
            RecipeStepIndicator(
                count = recipe.steps.size,
                selected = pagerState.currentPage,
                includeFinishIndicator = false,
                bottomPadding = if(it) with(density) {
                    WindowInsets.navigationBars.getBottom(density).toDp()
                } else 0.dp
            ) {
                coroutineScope.launch {
                    pagerState.scrollToPage(it)
                }
            }
        }
    ) { nsc, _, _ ->
        HorizontalPager(
            modifier = Modifier.fillMaxSize(),
            state = pagerState,
            pageNestedScrollConnection = nsc
        ) { index ->
            val step = recipe.steps[index]

            val ingredientsList = remember { mutableStateListOf<TandoorIngredient>() }
            val selectedIngredientIds = remember { mutableStateListOf<Int>() }

            LaunchedEffect(step) {
                selectedIngredientIds.clear()
                state.ingredientIdListByStepId[step.id]?.let { selectedIngredientIds.addAll(it) }
            }

            LaunchedEffect(
                step,
                selectedIngredientIds.toList(),
                state.unallocatedIngredientIdList.toList()
            ) {
                ingredientsList.clear()
                ingredientsList.addAll(selectedIngredientIds.map { ingredientById[it]!! })
                ingredientsList.addAll(ingredients.filter {
                    state.unallocatedIngredientIdList.contains(
                        it.id
                    )
                })
                ingredientsList.addAll(ingredients.filter {
                    !state.unallocatedIngredientIdList.contains(
                        it.id
                    ) && !selectedIngredientIds.contains(it.id)
                })
            }

            fun onSelectionChange(
                ingredient: TandoorIngredient,
                selected: Boolean
            ) {
                if(selected) {
                    state.ingredientIdListByStepId.forEach { it.value.remove(ingredient.id) }

                    state.unallocatedIngredientIdList.remove(ingredient.id)
                    selectedIngredientIds.add(ingredient.id)
                } else {
                    state.unallocatedIngredientIdList.add(ingredient.id)
                    state.ingredientIdListByStepId.forEach { it.value.remove(ingredient.id) }
                }

                state.ingredientIdListByStepId[step.id] = selectedIngredientIds
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .nestedScroll(nsc)
            ) {
                RecipeStepMultimediaBox(
                    recipe = recipe,
                    step = step
                )

                ResponsiveSideBySideLayout(
                    rightMinWidth = 300.dp,
                    rightMaxWidth = 500.dp,
                    leftMinWidth = 300.dp,
                    leftLayout = {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                modifier = Modifier.padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 16.dp,
                                    bottom = 8.dp
                                ),
                                text = step.name.ifBlank {
                                    stringResource(
                                        R.string.common_step,
                                        index + 1
                                    )
                                },
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                style = Typography.titleLarge
                            )
                        }

                        MarkdownRichTextWithTimerDetection(
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                            timerName = step.name,
                            markdown = step.instruction
                        )
                    }
                ) { enoughSpace ->
                    if(!enoughSpace) HorizontalDivider()

                    Box(
                        Modifier
                            .padding(16.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        IngredientsList(
                            list = ingredientsList,

                            itemModifier = {
                                Modifier
                                    .alpha(
                                        if(state.unallocatedIngredientIdList.contains(it.id)) {
                                            1f
                                        } else if(selectedIngredientIds.contains(it.id)) {
                                            1f
                                        } else {
                                            0.2f
                                        }
                                    )
                                    .clickable {
                                        onSelectionChange(
                                            it,
                                            !selectedIngredientIds.contains(it.id)
                                        )
                                    }
                            },
                            itemTrailingContent = {
                                Checkbox(
                                    checked = selectedIngredientIds.contains(it.id),
                                    onCheckedChange = { value ->
                                        onSelectionChange(it, value)
                                    }
                                )
                            },

                            factor = 1.0,
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                        )
                    }
                }
            }
        }
    }

    if(showSavingAlertDialog) AlertDialog(
        onDismissRequest = { },
        confirmButton = { },
        icon = { Icon(Icons.Rounded.Save, stringResource(id = R.string.action_save)) },
        title = { Text(text = stringResource(R.string.action_saving)) },
        text = {
            LinearProgressIndicator()
        }
    )

    if(showUnallocatedIngredientIdListNotEmptyError) AlertDialog(
        onDismissRequest = { showUnallocatedIngredientIdListNotEmptyError = false },
        confirmButton = {
            Button(onClick = {
                showUnallocatedIngredientIdListNotEmptyError = false
            }) {
                Text(text = stringResource(R.string.common_okay))
            }
        },
        icon = {
            Icon(
                Icons.Rounded.ErrorOutline,
                stringResource(R.string.error_unallocated_ingredients)
            )
        },
        title = { Text(text = stringResource(R.string.error_unallocated_ingredients)) },
        text = { Text(text = stringResource(R.string.error_unallocated_ingredients_description)) }
    )

    TandoorRequestErrorHandler(state = partialUpdateRequestState)
}