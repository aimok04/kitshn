package de.kitshn.ui.dialog.mealtype

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.graphics.toArgb
import de.kitshn.TimePrecision
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.TandoorMealType
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.getRoundedDateTime
import de.kitshn.handleTandoorRequestState
import de.kitshn.model.form.KitshnForm
import de.kitshn.model.form.KitshnFormSection
import de.kitshn.model.form.item.field.KitshnFormColorFieldItem
import de.kitshn.model.form.item.field.KitshnFormTextFieldItem
import de.kitshn.model.form.item.field.KitshnFormTimeFieldItem
import de.kitshn.toColorInt
import de.kitshn.ui.TandoorRequestErrorHandler
import de.kitshn.ui.dialog.AdaptiveFullscreenDialog
import de.kitshn.ui.state.ColorStateSaver
import de.kitshn.ui.state.LocalTimeStateSaver
import de.kitshn.ui.state.foreverRememberNotSavable
import kitshn.shared.generated.resources.Res
import kitshn.shared.generated.resources.action_add_meal_type
import kitshn.shared.generated.resources.action_create
import kitshn.shared.generated.resources.action_create_entry
import kitshn.shared.generated.resources.action_edit_entry
import kitshn.shared.generated.resources.action_edit_meal_type
import kitshn.shared.generated.resources.action_save
import kitshn.shared.generated.resources.common_color
import kitshn.shared.generated.resources.common_name
import kitshn.shared.generated.resources.common_time
import kitshn.shared.generated.resources.form_error_field_empty
import kitshn.shared.generated.resources.form_error_name_max_128
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import kotlin.time.ExperimentalTime

data class MealTypeCreationAndEditValues @OptIn(ExperimentalTime::class) constructor(
    val name: String = "",
    val order: Int = 0,
    val time: LocalTime = getRoundedDateTime(TimePrecision.HOURS).time,
    val color: Color = Color.Gray,
    val isDefault: Boolean = false,
)

@OptIn(ExperimentalTime::class)
fun TandoorMealType.toValues(): MealTypeCreationAndEditValues {
    return MealTypeCreationAndEditValues(
        name = name,
        order = order,
        time = runCatching { time?.let { LocalTime.parse(it) } }.getOrNull()
            ?: getRoundedDateTime(TimePrecision.HOURS).time,
        color = Color(colorStr?.toColorInt() ?: Color.Gray.toArgb()),
        isDefault = false,
    )
}

class MealTypeCreationAndEditDialogState(
    val shown: MutableState<Boolean> = mutableStateOf(false)
) {
    @OptIn(ExperimentalTime::class)
    var defaultValues = MealTypeCreationAndEditValues()
    var mealType by mutableStateOf<TandoorMealType?>(null)
    var isEdit = false

    fun create(values: MealTypeCreationAndEditValues = MealTypeCreationAndEditValues()) {
        this.defaultValues = values
        this.isEdit = false
        this.shown.value = true
    }

    fun edit(mealType: TandoorMealType) {
        this.mealType = mealType
        this.defaultValues = mealType.toValues()
        this.isEdit = true
        this.shown.value = true
    }

    fun dismiss() {
        this.shown.value = false
    }
}

@Composable
fun rememberMealTypeCreationAndEditDialogState(
    key: String
): MealTypeCreationAndEditDialogState {
    val value by foreverRememberNotSavable(
        key = key,
        initialValue = MealTypeCreationAndEditDialogState()
    )

    return value
}

@OptIn(ExperimentalTime::class)
@Composable
fun MealTypeCreationAndEditDialog(
    client: TandoorClient,
    state: MealTypeCreationAndEditDialogState,
    onSaved: (TandoorMealType) -> Unit
) {

    if (!state.shown.value) return

    val coroutineScope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current

    var name by rememberSaveable(state.defaultValues) {
        mutableStateOf(state.defaultValues.name)
    }
    var order by rememberSaveable(state.defaultValues) {
        mutableStateOf(state.defaultValues.order)
    }
    var time by rememberSaveable(
        saver = LocalTimeStateSaver,
        inputs = arrayOf(state.defaultValues)
    ) { mutableStateOf(state.defaultValues.time) }
    var color by rememberSaveable(
        saver = ColorStateSaver,
        inputs = arrayOf(state.defaultValues)
    ) { mutableStateOf(state.defaultValues.color) }

    val requestMealTypeState = rememberTandoorRequestState()

    val form = remember {
        KitshnForm(
            sections = listOf(
                KitshnFormSection(
                    listOf(
                        KitshnFormTextFieldItem(
                            value = { name },
                            onValueChange = { name = it },
                            label = { Text(stringResource(Res.string.common_name)) },
                            leadingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Rounded.Label,
                                    stringResource(Res.string.common_name)
                                )
                            },
                            optional = false,
                            check = {
                                if (it.length > 128) {
                                    getString(Res.string.form_error_name_max_128)
                                } else if (it.isBlank()) {
                                    getString(Res.string.form_error_field_empty)
                                } else {
                                    null
                                }
                            }
                        )
                    )
                ),
                KitshnFormSection(
                    listOf(
                        KitshnFormTimeFieldItem(
                            value = { time },
                            onValueChange = { time = it ?: LocalTime(12, 0) },
                            label = { Text(stringResource(Res.string.common_time)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Timer,
                                    stringResource(Res.string.common_time)
                                )
                            },
                            optional = false,
                            check = { null }
                        ),
                        KitshnFormColorFieldItem(
                            value = { color },
                            onValueChange = { color = it ?: Color.Gray },
                            label = { Text(stringResource(Res.string.common_color)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Palette,
                                    stringResource(Res.string.common_color)
                                )
                            },
                            optional = false,
                            check = { null }
                        )
                    )
                )
            ),
            submitButton = {
                Button(onClick = it) {
                    Text(
                        text = if (state.isEdit) {
                            stringResource(Res.string.action_save)
                        } else {
                            stringResource(Res.string.action_create)
                        }
                    )
                }
            },
            onSubmit = {
                coroutineScope.launch {
                    if (state.isEdit) {
                        requestMealTypeState.wrapRequest {
                            state.mealType = state.mealType?.partialUpdate(
                                name = name,
                                order = order,
                                time = time,
                                color = color,
                            )
                        }
                    } else {
                        requestMealTypeState.wrapRequest {
                            state.mealType = client.mealType.create(
                                name = name,
                                order = order,
                                time = time,
                                color = color,
                            )
                        }
                    }

                    hapticFeedback.handleTandoorRequestState(requestMealTypeState)

                    state.dismiss()
                    if (state.mealType != null) {
                        onSaved(state.mealType!!)
                    }

                }
            }
        )
    }

    AdaptiveFullscreenDialog(
        onDismiss = {
            state.dismiss()
        },
        title = {
            Text(
                text = if (state.isEdit) {
                    stringResource(Res.string.action_edit_meal_type)
                } else {
                    stringResource(Res.string.action_add_meal_type)
                }
            )
        },
        actions = {
            form.RenderSubmitButton()
        }
    ) { it, _, _ ->
        form.Render(it)
    }

    TandoorRequestErrorHandler(state = requestMealTypeState)
}