package de.kitshn.ui.dialog.mealplan

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Groups2
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
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.TandoorMealPlan
import de.kitshn.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.api.tandoor.model.recipe.TandoorRecipeOverview
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.api.tandoor.route.TandoorUser
import de.kitshn.model.form.KitshnForm
import de.kitshn.model.form.KitshnFormSection
import de.kitshn.model.form.item.KitshnFormCheckItem
import de.kitshn.model.form.item.field.KitshnFormDateFieldItem
import de.kitshn.model.form.item.field.KitshnFormIntegerFieldItem
import de.kitshn.model.form.item.field.KitshnFormMealTypeSearchFieldItem
import de.kitshn.model.form.item.field.KitshnFormRecipeSearchFieldItem
import de.kitshn.model.form.item.field.KitshnFormSelectUsersFieldItem
import de.kitshn.model.form.item.field.KitshnFormTextFieldItem
import de.kitshn.parseTandoorDate
import de.kitshn.ui.TandoorRequestErrorHandler
import de.kitshn.ui.dialog.AdaptiveFullscreenDialog
import de.kitshn.ui.dialog.recipe.RecipeAddToShoppingDialog
import de.kitshn.ui.dialog.recipe.rememberRecipeAddToShoppingDialogState
import de.kitshn.ui.state.foreverRememberNotSavable
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_create
import kitshn.composeapp.generated.resources.action_create_entry
import kitshn.composeapp.generated.resources.action_edit_entry
import kitshn.composeapp.generated.resources.action_save
import kitshn.composeapp.generated.resources.action_share
import kitshn.composeapp.generated.resources.common_add_to_shopping_list
import kitshn.composeapp.generated.resources.common_end_date
import kitshn.composeapp.generated.resources.common_meal_type
import kitshn.composeapp.generated.resources.common_note
import kitshn.composeapp.generated.resources.common_portions
import kitshn.composeapp.generated.resources.common_recipe
import kitshn.composeapp.generated.resources.common_select_users_for_sharing
import kitshn.composeapp.generated.resources.common_select_users_for_sharing_empty
import kitshn.composeapp.generated.resources.common_shopping
import kitshn.composeapp.generated.resources.common_start_date
import kitshn.composeapp.generated.resources.common_title
import kitshn.composeapp.generated.resources.form_error_title_max_64
import kitshn.composeapp.generated.resources.meal_plan_form_add_to_shopping_list_description
import kitshn.composeapp.generated.resources.meal_plan_form_error_entry_needs_title_or_recipe
import kitshn.composeapp.generated.resources.meal_plan_form_review_add_to_shopping_list_description
import kitshn.composeapp.generated.resources.meal_plan_form_review_add_to_shopping_list_label
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

data class MealPlanCreationAndEditDefaultValues(
    val title: String = "",
    val note: String = "",
    val recipeId: Int? = null,
    val servings: Int? = null,
    val mealTypeId: Int? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val shared: List<TandoorUser> = listOf(),
    val addToShopping: Boolean = true,
    val reviewAddToShopping: Boolean = true
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
            shared = mealPlan.shared,
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
    showFractionalValues: Boolean,
    onRefresh: () -> Unit
) {
    if(creationState?.shown?.value != true && editState?.shown?.value != true) return

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

    var startDate by remember { mutableStateOf(defaultValues.startDate) }
    var endDate by remember { mutableStateOf(defaultValues.endDate) }

    var shared by remember { mutableStateOf(defaultValues.shared) }

    var addToShopping by rememberSaveable { mutableStateOf(defaultValues.addToShopping) }
    var reviewAddToShopping by rememberSaveable { mutableStateOf(defaultValues.reviewAddToShopping) }

    val servingsText = if(recipeOverview?.servings_text.isNullOrBlank())
        stringResource(Res.string.common_portions) else recipeOverview?.servings_text!!

    val requestMealPlanState = rememberTandoorRequestState()
    val requestRecipeAddToShoppingState = rememberTandoorRequestState()

    var recipeAddToShoppingDialogRecipe by remember { mutableStateOf<TandoorRecipe?>(null) }
    var recipeAddToShoppingDialogMealPlan by remember { mutableStateOf<TandoorMealPlan?>(null) }
    val recipeAddToShoppingDialogState = rememberRecipeAddToShoppingDialogState()

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

                            label = { Text(stringResource(Res.string.common_title)) },
                            leadingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Rounded.Label,
                                    stringResource(Res.string.common_title)
                                )
                            },

                            optional = true,

                            check = {
                                if(it.length > 64) {
                                    getString(Res.string.form_error_title_max_64)
                                } else if(it.isBlank() && recipeId == null) {
                                    getString(Res.string.meal_plan_form_error_entry_needs_title_or_recipe)
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

                            label = { Text(stringResource(Res.string.common_start_date)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.DateRange,
                                    stringResource(Res.string.common_start_date)
                                )
                            },

                            optional = false,

                            check = { null }
                        ),
                        KitshnFormDateFieldItem(
                            value = { endDate },
                            onValueChange = { endDate = it },

                            minDate = { startDate },

                            label = { Text(stringResource(Res.string.common_end_date)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.DateRange,
                                    stringResource(Res.string.common_end_date)
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
                                if(recipeId == null) return@KitshnFormRecipeSearchFieldItem

                                client.container.recipeOverview[recipeId]?.servings?.let {
                                    servings = it
                                }
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
                                if(title.isBlank() && it == null) {
                                    getString(Res.string.meal_plan_form_error_entry_needs_title_or_recipe)
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
                                    getString(Res.string.meal_plan_form_error_entry_needs_title_or_recipe)
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

                            label = { Text(stringResource(Res.string.common_meal_type)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Category,
                                    stringResource(Res.string.common_meal_type)
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

                            label = { Text(stringResource(Res.string.common_note)) },
                            leadingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Rounded.Notes,
                                    stringResource(Res.string.common_note)
                                )
                            },

                            optional = true,

                            check = { null }
                        )
                    )
                ),
                KitshnFormSection(
                    listOf(
                        KitshnFormSelectUsersFieldItem(
                            client = client,
                            value = { shared },
                            onValueChange = {
                                shared = it
                            },

                            label = { Text(stringResource(Res.string.action_share)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Groups2,
                                    stringResource(Res.string.action_share)
                                )
                            },

                            dialogTitle = { stringResource(Res.string.common_select_users_for_sharing) },
                            dialogEmptyErrorText = { stringResource(Res.string.common_select_users_for_sharing_empty) },

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
                                    stringResource(Res.string.common_shopping)
                                )
                            },
                            label = { Text(stringResource(Res.string.common_add_to_shopping_list)) },
                            description = { Text(stringResource(Res.string.meal_plan_form_add_to_shopping_list_description)) }
                        )
                    )
                ),
                KitshnFormSection(
                    listOf(
                        KitshnFormCheckItem(
                            value = { if(addToShopping) reviewAddToShopping else false },
                            onValueChange = { if(addToShopping) reviewAddToShopping = it },

                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Checklist,
                                    stringResource(Res.string.meal_plan_form_review_add_to_shopping_list_label)
                                )
                            },
                            label = { Text(stringResource(Res.string.meal_plan_form_review_add_to_shopping_list_label)) },
                            description = { Text(stringResource(Res.string.meal_plan_form_review_add_to_shopping_list_description)) },

                            enabled = { addToShopping }
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
                                shared = shared,
                                addshopping = addToShopping
                            )
                        }

                        onRefresh()
                        editState?.dismiss()
                    } else {
                        val mealPlan = requestMealPlanState.wrapRequest {
                            client.mealPlan.create(
                                title = title,
                                recipe = client.container.recipeOverview[recipeId],
                                servings = servings!!,
                                note = note,
                                from_date = startDate!!,
                                to_date = endDate,
                                meal_type = client.container.mealType[mealTypeId]!!,
                                addshopping = if(reviewAddToShopping) {
                                    false
                                } else {
                                    addToShopping
                                },
                                shared = shared
                            )
                        }

                        if(mealPlan != null) {
                            if(addToShopping && reviewAddToShopping && recipeId != null) {
                                recipeAddToShoppingDialogRecipe = client.recipe.get(recipeId!!)
                                recipeAddToShoppingDialogMealPlan = mealPlan

                                recipeAddToShoppingDialogState.open(
                                    recipe = recipeAddToShoppingDialogRecipe!!,
                                    servings = servings!!.toDouble()
                                )
                                return@launch
                            }

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
                    stringResource(Res.string.action_edit_entry)
                } else {
                    stringResource(Res.string.action_create_entry)
                }
            )
        },
        actions = {
            form.RenderSubmitButton()
        }
    ) { it, _, _ ->
        form.Render(it)
    }

    RecipeAddToShoppingDialog(
        state = recipeAddToShoppingDialogState,
        showFractionalValues = showFractionalValues,
        onSubmit = { ingredients, mServings ->
            coroutineScope.launch {
                requestRecipeAddToShoppingState.wrapRequest {
                    recipeAddToShoppingDialogRecipe?.shopping(
                        ingredients = ingredients.map { it.id },
                        servings = mServings,
                        mealplan = recipeAddToShoppingDialogMealPlan
                    )

                    onRefresh()
                    creationState?.dismiss()
                }
            }
        }
    )

    TandoorRequestErrorHandler(state = requestMealPlanState)
    TandoorRequestErrorHandler(state = requestRecipeAddToShoppingState)
}