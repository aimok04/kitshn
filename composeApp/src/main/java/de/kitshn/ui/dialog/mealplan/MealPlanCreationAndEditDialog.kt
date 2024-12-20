package de.kitshn.ui.dialog.mealplan

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import de.kitshn.R
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.TandoorMealPlan
import de.kitshn.api.tandoor.model.recipe.TandoorRecipeOverview
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.model.form.KitshnForm
import de.kitshn.model.form.KitshnFormSection
import de.kitshn.model.form.item.KitshnFormCheckItem
import de.kitshn.model.form.item.field.KitshnFormDateFieldItem
import de.kitshn.model.form.item.field.KitshnFormIntegerFieldItem
import de.kitshn.model.form.item.field.KitshnFormMealTypeSearchFieldItem
import de.kitshn.model.form.item.field.KitshnFormRecipeSearchFieldItem
import de.kitshn.model.form.item.field.KitshnFormTextFieldItem
import de.kitshn.parseTandoorDate
import de.kitshn.ui.TandoorRequestErrorHandler
import de.kitshn.ui.dialog.AdaptiveFullscreenDialog
import de.kitshn.ui.state.foreverRememberNotSavable
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.roundToInt

data class MealPlanCreationAndEditDefaultValues(
    val title: String = "",
    val note: String = "",
    val recipeId: Int? = null,
    val servings: Int? = null,
    val mealTypeId: Int? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val addToShopping: Boolean = false,
)

@Composable
fun rememberMealPlanEditDialogState(
    key: String
): MealPlanEditDialogState {
    val value by foreverRememberNotSavable(
        key = key,
        initialValue = MealPlanEditDialogState()
    )

    return value
}

class MealPlanEditDialogState(
    val shown: MutableState<Boolean> = mutableStateOf(false)
) {

    var defaultValues = MealPlanCreationAndEditDefaultValues()
    var mealPlan by mutableStateOf<TandoorMealPlan?>(null)

    fun open(mealPlan: TandoorMealPlan) {
        this.mealPlan = mealPlan

        this.defaultValues = MealPlanCreationAndEditDefaultValues(
            title = mealPlan.title,
            note = mealPlan.note ?: "",
            recipeId = mealPlan.recipe?.id,
            servings = mealPlan.servings.roundToInt(),
            mealTypeId = mealPlan.meal_type.id,
            startDate = mealPlan.from_date.parseTandoorDate(),
            endDate = mealPlan.to_date.parseTandoorDate(),
            addToShopping = mealPlan.shopping
        )

        this.shown.value = true
    }

    fun dismiss() {
        this.shown.value = false
    }
}

@Composable
fun rememberMealPlanCreationDialogState(
    key: String
): MealPlanCreationDialogState {
    val value by foreverRememberNotSavable(
        key = key,
        initialValue = MealPlanCreationDialogState()
    )

    return value
}

class MealPlanCreationDialogState(
    val shown: MutableState<Boolean> = mutableStateOf(false)
) {

    var defaultValues = MealPlanCreationAndEditDefaultValues()

    fun open(values: MealPlanCreationAndEditDefaultValues) {
        this.defaultValues = values
        this.shown.value = true
    }

    fun dismiss() {
        this.shown.value = false
    }
}

@Composable
fun MealPlanCreationAndEditDialog(
    client: TandoorClient,
    creationState: MealPlanCreationDialogState? = null,
    editState: MealPlanEditDialogState? = null,
    onRefresh: () -> Unit
) {
    if(creationState?.shown?.value != true && editState?.shown?.value != true) return

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val defaultValues =
        if(creationState?.shown?.value == true) creationState.defaultValues else editState?.defaultValues
    if(defaultValues == null) return

    val isEditDialog = editState?.shown?.value == true

    // form values
    var title by rememberSaveable { mutableStateOf(defaultValues.title) }
    var note by rememberSaveable { mutableStateOf(defaultValues.note) }

    var recipeId by rememberSaveable { mutableStateOf(defaultValues.recipeId) }
    var servings by rememberSaveable { mutableStateOf(defaultValues.servings) }

    var recipeOverview by remember { mutableStateOf<TandoorRecipeOverview?>(null) }
    LaunchedEffect(recipeId) { recipeOverview = client.container.recipeOverview[recipeId] }

    var mealTypeId by rememberSaveable { mutableStateOf(defaultValues.mealTypeId) }

    var startDate by rememberSaveable { mutableStateOf(defaultValues.startDate) }
    var endDate by rememberSaveable { mutableStateOf(defaultValues.endDate) }

    var addToShopping by rememberSaveable { mutableStateOf(defaultValues.addToShopping) }

    val servingsText = if(recipeOverview?.servings_text.isNullOrBlank())
        stringResource(id = R.string.common_portions) else recipeOverview?.servings_text!!

    val requestMealPlanState = rememberTandoorRequestState()

    val form = remember {
        KitshnForm(
            sections = listOf(
                KitshnFormSection(
                    listOf(
                        KitshnFormTextFieldItem(
                            value = { title },
                            onValueChange = {
                                title = it
                            },

                            label = { Text(stringResource(R.string.common_title)) },
                            leadingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Rounded.Label,
                                    stringResource(R.string.common_title)
                                )
                            },

                            optional = true,

                            check = {
                                if(it.length > 64) {
                                    context.getString(R.string.form_error_title_max_64)
                                } else if(it.isBlank() && recipeId == null) {
                                    context.getString(R.string.meal_plan_form_error_entry_needs_title_or_recipe)
                                } else {
                                    null
                                }
                            }
                        )
                    )
                ),
                KitshnFormSection(
                    listOf(
                        KitshnFormDateFieldItem(
                            value = { startDate },
                            onValueChange = { startDate = it },

                            maxDate = { endDate },

                            label = { Text(stringResource(R.string.common_start_date)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.DateRange,
                                    stringResource(R.string.common_start_date)
                                )
                            },

                            optional = false,

                            check = { null }
                        ),
                        KitshnFormDateFieldItem(
                            value = { endDate },
                            onValueChange = { endDate = it },

                            minDate = { startDate },

                            label = { Text(stringResource(R.string.common_end_date)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.DateRange,
                                    stringResource(R.string.common_end_date)
                                )
                            },

                            optional = true,

                            check = { null }
                        )
                    )
                ),
                KitshnFormSection(
                    listOf(
                        KitshnFormRecipeSearchFieldItem(
                            client = client,
                            value = { recipeId },
                            onValueChange = {
                                recipeId = it
                            },

                            label = { Text(stringResource(R.string.common_recipe)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Receipt,
                                    stringResource(R.string.common_recipe)
                                )
                            },

                            optional = true,

                            check = {
                                if(title.isBlank() && it == null) {
                                    context.getString(R.string.meal_plan_form_error_entry_needs_title_or_recipe)
                                } else {
                                    null
                                }
                            }
                        ),
                        KitshnFormIntegerFieldItem(
                            value = { servings },
                            onValueChange = { servings = it },

                            label = { Text(servingsText) },
                            leadingIcon = { Icon(Icons.Rounded.Numbers, servingsText) },

                            min = { 1 },

                            optional = false,

                            check = {
                                if(title.isBlank() && it == null) {
                                    context.getString(R.string.meal_plan_form_error_entry_needs_title_or_recipe)
                                } else {
                                    null
                                }
                            }
                        )
                    )
                ),
                KitshnFormSection(
                    listOf(
                        KitshnFormMealTypeSearchFieldItem(
                            client = client,
                            value = { mealTypeId },
                            onValueChange = {
                                mealTypeId = it
                            },

                            label = { Text(stringResource(R.string.common_meal_type)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Category,
                                    stringResource(R.string.common_meal_type)
                                )
                            },

                            optional = false,

                            check = {
                                null
                            }
                        )
                    )
                ),
                KitshnFormSection(
                    listOf(
                        KitshnFormTextFieldItem(
                            value = { note },
                            onValueChange = {
                                note = it
                            },

                            label = { Text(stringResource(R.string.common_note)) },
                            leadingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Rounded.Notes,
                                    stringResource(R.string.common_note)
                                )
                            },

                            optional = true,

                            check = { null }
                        )
                    )
                ),
                KitshnFormSection(
                    listOf(
                        KitshnFormCheckItem(
                            value = { addToShopping },
                            onValueChange = { addToShopping = it },

                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.ShoppingCart,
                                    stringResource(R.string.common_shopping)
                                )
                            },
                            label = { Text(stringResource(R.string.common_add_to_shopping_list)) },
                            description = { Text(stringResource(R.string.meal_plan_form_add_to_shopping_list_description)) }
                        )
                    )
                )
            ),
            submitButton = {
                Button(onClick = it) {
                    Text(
                        text = if(isEditDialog) {
                            stringResource(R.string.action_save)
                        } else {
                            stringResource(R.string.action_create)
                        }
                    )
                }
            },
            onSubmit = {
                if(servings == null) return@KitshnForm
                if(startDate == null) return@KitshnForm
                if(!client.container.mealType.containsKey(mealTypeId)) return@KitshnForm

                coroutineScope.launch {
                    if(isEditDialog) {
                        requestMealPlanState.wrapRequest {
                            editState?.mealPlan?.partialUpdate(
                                title = title,
                                recipe = client.container.recipeOverview[recipeId],
                                servings = servings!!,
                                note = note,
                                from_date = startDate!!,
                                to_date = endDate,
                                meal_type = client.container.mealType[mealTypeId]!!,
                                addshopping = addToShopping
                            )
                        }

                        onRefresh()
                        editState?.dismiss()
                    } else {
                        val mealPlan = requestMealPlanState.wrapRequest {
                            val userPreference = client.userPreference.fetch()

                            client.mealPlan.create(
                                title = title,
                                recipe = client.container.recipeOverview[recipeId],
                                servings = servings!!,
                                note = note,
                                from_date = startDate!!,
                                to_date = endDate,
                                meal_type = client.container.mealType[mealTypeId]!!,
                                addshopping = addToShopping,
                                shared = userPreference.plan_share
                            )
                        }

                        if(mealPlan != null) {
                            onRefresh()
                            creationState?.dismiss()
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
                    stringResource(R.string.action_edit_entry)
                } else {
                    stringResource(R.string.action_create_entry)
                }
            )
        },
        actions = {
            form.RenderSubmitButton()
        }
    ) { it, _, _ ->
        form.Render(it)
    }

    TandoorRequestErrorHandler(state = requestMealPlanState)
}