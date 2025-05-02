package de.kitshn.ui.dialog.recipe.step

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Scale
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import de.kitshn.HapticFeedbackHandler
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.TandoorIngredient
import de.kitshn.api.tandoor.model.TandoorStep
import de.kitshn.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.copy
import de.kitshn.json
import de.kitshn.model.form.KitshnForm
import de.kitshn.model.form.KitshnFormSection
import de.kitshn.model.form.item.field.KitshnFormIntegerFieldItem
import de.kitshn.model.form.item.field.KitshnFormRecipeSearchFieldItem
import de.kitshn.model.form.item.field.KitshnFormTextFieldItem
import de.kitshn.ui.TandoorRequestErrorHandler
import de.kitshn.ui.component.editor.MarkdownEditor
import de.kitshn.ui.component.input.DoubleField
import de.kitshn.ui.component.input.FoodSearchField
import de.kitshn.ui.component.input.UnitSearchField
import de.kitshn.ui.dialog.AdaptiveFullscreenDialog
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_add_ingredient
import kitshn.composeapp.generated.resources.action_create
import kitshn.composeapp.generated.resources.action_create_step
import kitshn.composeapp.generated.resources.action_delete
import kitshn.composeapp.generated.resources.action_edit_step
import kitshn.composeapp.generated.resources.action_reorder
import kitshn.composeapp.generated.resources.action_save
import kitshn.composeapp.generated.resources.common_amount
import kitshn.composeapp.generated.resources.common_food
import kitshn.composeapp.generated.resources.common_minute_min
import kitshn.composeapp.generated.resources.common_name
import kitshn.composeapp.generated.resources.common_note
import kitshn.composeapp.generated.resources.common_recipe
import kitshn.composeapp.generated.resources.common_time_work
import kitshn.composeapp.generated.resources.common_unit
import kitshn.composeapp.generated.resources.form_error_name_max_128
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class StepCreationAndEditDefaultValues(
    val name: String = "",
    val instruction: String = "",
    val ingredients: List<TandoorIngredient> = listOf(),
    val time: Int = 0,
    val step_recipe: Int? = null,
    val show_as_header: Boolean = false
)

class StepEditDialogState(
    val shown: MutableState<Boolean> = mutableStateOf(false)
) {

    var defaultValues = StepCreationAndEditDefaultValues()
    var step by mutableStateOf<TandoorStep?>(null)

    fun open(step: TandoorStep) {
        this.step = step

        this.defaultValues = StepCreationAndEditDefaultValues(
            name = step.name,
            instruction = step.instruction,
            ingredients = step.ingredients,
            time = step.time,
            step_recipe = step.step_recipe,
            show_as_header = step.show_as_header
        )

        this.shown.value = true
    }

    fun dismiss() {
        this.shown.value = false
    }
}

class StepCreationDialogState(
    val shown: MutableState<Boolean> = mutableStateOf(false)
) {

    var defaultValues = StepCreationAndEditDefaultValues()

    var recipe by mutableStateOf<TandoorRecipe?>(null)

    fun open(
        recipe: TandoorRecipe,
        values: StepCreationAndEditDefaultValues
    ) {
        this.recipe = recipe

        this.defaultValues = values
        this.shown.value = true
    }

    fun dismiss() {
        this.shown.value = false
    }
}

@Serializable
class IngredientModel(
    @Transient
    val ingredient: TandoorIngredient? = null
) {

    @OptIn(ExperimentalUuidApi::class)
    @Transient
    val id = Uuid.random().toHexString()

    var amount by mutableStateOf(if(ingredient?.amount == 0.0) null else ingredient?.amount)
    var unit by mutableStateOf(ingredient?.unit?.name)
    var food by mutableStateOf(ingredient?.food?.name)
    var note by mutableStateOf(ingredient?.note)

    @OptIn(ExperimentalSerializationApi::class)
    fun toJsonObject(
        order: Int? = null
    ): JsonObject {
        val model = ingredient?.let {
            json.encodeToJsonElement(ingredient)
                .jsonObject
        }

        return buildJsonObject {
            model?.entries?.forEach {
                put(it.key, it.value)
            }

            val unitName = unit?.ifBlank { null }
            val foodName = food?.ifBlank { null }

            put("amount", JsonPrimitive(if(amount == null) 0.0 else amount))
            put(
                "unit", if(unitName != null) {
                    buildJsonObject {
                        put("name", JsonPrimitive(unitName))
                    }
                } else {
                    JsonPrimitive(null)
                })
            put(
                "food", if(foodName != null) {
                    buildJsonObject {
                        put("name", JsonPrimitive(foodName))
                    }
                } else {
                    JsonPrimitive(null)
                })
            put("note", JsonPrimitive(note))
            if(order != null) put("order", JsonPrimitive(order))
        }
    }

}

@Composable
fun StepCreationAndEditDialog(
    client: TandoorClient,
    creationState: StepCreationDialogState? = null,
    editState: StepEditDialogState? = null,
    onCreate: (step: TandoorStep) -> Unit,
    onUpdate: (step: TandoorStep) -> Unit
) {
    if(creationState?.shown?.value != true && editState?.shown?.value != true) return

    val coroutineScope = rememberCoroutineScope()
    val hapticFeedbackHandler = HapticFeedbackHandler()

    val defaultValues =
        if(creationState?.shown?.value == true) creationState.defaultValues else editState?.defaultValues
    if(defaultValues == null) return

    val isEditDialog = editState?.shown?.value == true

    // form values
    val instructionEditorState = rememberRichTextState()
    LaunchedEffect(defaultValues) {
        instructionEditorState.setMarkdown(defaultValues.instruction)
    }

    var name by remember { mutableStateOf(defaultValues.name) }

    val ingredients = remember {
        mutableStateListOf<IngredientModel>().apply {
            defaultValues.ingredients.forEach { add(IngredientModel(it)) }
        }
    }

    var time by remember { mutableStateOf(defaultValues.time) }

    var step_recipe by remember { mutableStateOf(defaultValues.step_recipe) }

    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        ingredients.apply { add(to.index - 4, removeAt(from.index - 4)) }
        hapticFeedbackHandler(de.kitshn.HapticFeedbackType.SHORT_TICK)
    }

    val requestStepState = rememberTandoorRequestState()

    val form = remember {
        KitshnForm(
            sections = listOf(
                KitshnFormSection(
                    listOf(
                        KitshnFormTextFieldItem(
                            value = { name },
                            onValueChange = {
                                name = it
                            },

                            label = { Text(stringResource(Res.string.common_name)) },
                            leadingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Rounded.Label,
                                    stringResource(Res.string.common_name)
                                )
                            },

                            optional = true,

                            check = {
                                if(it.length > 128) {
                                    getString(Res.string.form_error_name_max_128)
                                } else {
                                    null
                                }
                            }
                        )
                    )
                ),
                KitshnFormSection(
                    listOf(
                        KitshnFormIntegerFieldItem(
                            value = { if(time == 0) null else time },
                            onValueChange = {
                                time = it ?: 0
                            },

                            label = { Text(stringResource(Res.string.common_time_work)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Timer,
                                    stringResource(Res.string.common_time_work)
                                )
                            },
                            suffix = { Text(stringResource(Res.string.common_minute_min)) },

                            min = { 0 },

                            optional = true,

                            check = { null }
                        )
                    )
                ),
                KitshnFormSection(
                    listOf(
                        KitshnFormRecipeSearchFieldItem(
                            client = client,

                            value = { step_recipe },
                            onValueChange = {
                                step_recipe = it
                            },

                            label = { Text(stringResource(Res.string.common_recipe)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Receipt,
                                    stringResource(Res.string.common_recipe)
                                )
                            },

                            optional = true,

                            check = {
                                null
                            }
                        )
                    )
                )
            ),
            submitButton = {
                Button(onClick = it) {
                    Text(
                        text = if(isEditDialog) {
                            stringResource(Res.string.action_save)
                        } else {
                            stringResource(Res.string.action_create)
                        }
                    )
                }
            },
            onSubmit = {
                coroutineScope.launch {
                    if(isEditDialog) {
                        requestStepState.wrapRequest {
                            val rawStep = client.step.getRaw(id = editState!!.step!!.id)

                            val updatedStep = editState.step!!.updateRaw(
                                buildJsonObject {
                                    copy(rawStep)

                                    put("name", JsonPrimitive(name))
                                    put(
                                        "instruction",
                                        JsonPrimitive(instructionEditorState.toMarkdown())
                                    )
                                    put("time", JsonPrimitive(time))
                                    put("ingredients", buildJsonArray {
                                        repeat(ingredients.size) {
                                            add(ingredients[it].toJsonObject(it))
                                        }
                                    })
                                    put("step_recipe", JsonPrimitive(step_recipe))
                                }
                            )

                            onUpdate(updatedStep)
                            editState.dismiss()
                        }
                    } else {
                        val recipe = creationState!!.recipe!!

                        requestStepState.wrapRequest {
                            val updatedRecipe = recipe.partialUpdate(
                                steps = buildJsonArray {
                                    recipe.stepsRaw.forEach { add(it) }

                                    add(
                                        buildJsonObject {
                                            put("name", JsonPrimitive(name))
                                            put(
                                                "instruction",
                                                JsonPrimitive(instructionEditorState.toMarkdown())
                                            )
                                            put("time", JsonPrimitive(time))
                                            put("ingredients", buildJsonArray {
                                                repeat(ingredients.size) {
                                                    add(ingredients[it].toJsonObject(it))
                                                }
                                            })
                                            put("step_recipe", JsonPrimitive(step_recipe))
                                            put("order", JsonPrimitive(recipe.stepsRaw.size))
                                        }
                                    )
                                }
                            )

                            delay(500)

                            onCreate(updatedRecipe.steps.last())
                            creationState.dismiss()
                        }
                    }
                }
            }
        )
    }

    AdaptiveFullscreenDialog(
        onDismiss = {
            creationState?.dismiss()
            editState?.dismiss()
        },
        title = {
            Text(
                text = if(isEditDialog) {
                    stringResource(Res.string.action_edit_step)
                } else {
                    stringResource(Res.string.action_create_step)
                }
            )
        },
        actions = {
            form.RenderSubmitButton()
        }
    ) { it, _, _ ->
        LazyColumn(
            modifier = Modifier.nestedScroll(it),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            state = lazyListState
        ) {
            item {
                MarkdownEditor(
                    state = instructionEditorState
                )
            }

            item {
                HorizontalDivider(
                    modifier = Modifier.padding(
                        top = 8.dp,
                        bottom = 8.dp
                    )
                )
            }

            item {
                form.sections.forEach {
                    it.items.forEach { item ->
                        Spacer(Modifier.height(8.dp))

                        item.Render(
                            Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            item {
                HorizontalDivider(
                    modifier = Modifier.padding(
                        top = 8.dp,
                        bottom = 8.dp
                    )
                )
            }

            items(ingredients.size, key = { ingredients[it].id }) { index ->
                val ingredient = ingredients[index]

                val interactionSource = remember { MutableInteractionSource() }
                ReorderableItem(
                    state = reorderableLazyListState,
                    key = ingredient.id,
                    animateItemModifier = Modifier
                ) {
                    Row(
                        Modifier.padding(top = 8.dp, end = 8.dp)
                    ) {
                        Column(
                            Modifier.height(118.dp),
                            verticalArrangement = Arrangement.SpaceAround
                        ) {
                            IconButton(
                                onClick = { },
                                modifier = Modifier.longPressDraggableHandle(
                                    onDragStarted = {
                                        hapticFeedbackHandler(de.kitshn.HapticFeedbackType.DRAG_START)
                                    },
                                    onDragStopped = {
                                        hapticFeedbackHandler(de.kitshn.HapticFeedbackType.GESTURE_END)
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
                                    ingredients.remove(ingredient)
                                },
                            ) {
                                Icon(
                                    Icons.Rounded.Delete,
                                    contentDescription = stringResource(Res.string.action_delete)
                                )
                            }
                        }

                        Column {
                            Row {
                                DoubleField(
                                    modifier = Modifier.fillMaxWidth(0.5f),

                                    value = ingredient.amount,

                                    leadingIcon = {
                                        Icon(
                                            Icons.Rounded.Numbers,
                                            stringResource(Res.string.common_amount)
                                        )
                                    },
                                    label = { Text(text = stringResource(Res.string.common_amount)) },

                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Next
                                    ),

                                    onValueChange = {
                                        ingredient.amount = it
                                    }
                                )

                                Spacer(Modifier.width(4.dp))

                                UnitSearchField(
                                    modifier = Modifier.fillMaxWidth(),
                                    dropdownMenuModifier = Modifier,

                                    client = client,
                                    value = ingredient.unit,

                                    leadingIcon = {
                                        Icon(
                                            Icons.Rounded.Scale,
                                            stringResource(Res.string.common_unit)
                                        )
                                    },
                                    label = { Text(text = stringResource(Res.string.common_unit)) },

                                    keyboardOptions = KeyboardOptions(
                                        imeAction = ImeAction.Next
                                    ),

                                    onValueChange = {
                                        ingredient.unit = it
                                    },

                                    onSelect = { }
                                )
                            }

                            Spacer(Modifier.height(4.dp))

                            Row {
                                FoodSearchField(
                                    modifier = Modifier.fillMaxWidth(0.5f),
                                    dropdownMenuModifier = Modifier,

                                    client = client,
                                    value = ingredient.food,

                                    leadingIcon = {
                                        Icon(
                                            Icons.AutoMirrored.Rounded.Label,
                                            stringResource(Res.string.common_food)
                                        )
                                    },
                                    label = { Text(text = stringResource(Res.string.common_food)) },

                                    keyboardOptions = KeyboardOptions(
                                        imeAction = ImeAction.Done
                                    ),

                                    onValueChange = {
                                        ingredient.food = it
                                    },

                                    onSelect = { }
                                )

                                Spacer(Modifier.width(4.dp))

                                TextField(
                                    modifier = Modifier.fillMaxWidth(),

                                    value = ingredient.note ?: "",

                                    label = { Text(text = stringResource(Res.string.common_note)) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.AutoMirrored.Rounded.Notes,
                                            stringResource(Res.string.common_note)
                                        )
                                    },

                                    keyboardOptions = KeyboardOptions(
                                        imeAction = ImeAction.Next
                                    ),

                                    singleLine = true,

                                    onValueChange = {
                                        ingredient.note = it.ifBlank { null }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            ingredients.add(IngredientModel())
                        }
                    ) {
                        Icon(
                            Icons.Rounded.Add,
                            stringResource(Res.string.action_add_ingredient)
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(stringResource(Res.string.action_add_ingredient))
                    }
                }
            }
        }
    }

    TandoorRequestErrorHandler(state = requestStepState)
}