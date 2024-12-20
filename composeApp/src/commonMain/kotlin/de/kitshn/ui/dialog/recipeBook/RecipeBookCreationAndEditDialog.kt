package de.kitshn.ui.dialog.recipeBook

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.automirrored.rounded.Notes
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
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.TandoorRecipeBook
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.model.form.KitshnForm
import de.kitshn.model.form.KitshnFormSection
import de.kitshn.model.form.item.field.KitshnFormTextFieldItem
import de.kitshn.ui.TandoorRequestErrorHandler
import de.kitshn.ui.dialog.AdaptiveFullscreenDialog
import de.kitshn.ui.state.foreverRememberNotSavable
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_create
import kitshn.composeapp.generated.resources.action_create_recipe_book
import kitshn.composeapp.generated.resources.action_edit_recipe_book
import kitshn.composeapp.generated.resources.action_save
import kitshn.composeapp.generated.resources.common_description
import kitshn.composeapp.generated.resources.common_name
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

data class RecipeBookCreationAndEditDefaultValues(
    val name: String = "",
    val description: String = ""
)

@Composable
fun rememberRecipeBookEditDialogState(
    key: String
): RecipeBookEditDialogState {
    val value by foreverRememberNotSavable(
        key = key,
        initialValue = RecipeBookEditDialogState()
    )

    return value
}

class RecipeBookEditDialogState(
    val shown: MutableState<Boolean> = mutableStateOf(false)
) {

    var defaultValues = RecipeBookCreationAndEditDefaultValues()
    var recipeBook by mutableStateOf<TandoorRecipeBook?>(null)

    fun open(recipeBook: TandoorRecipeBook) {
        this.recipeBook = recipeBook

        this.defaultValues = RecipeBookCreationAndEditDefaultValues(
            name = recipeBook.name,
            description = recipeBook.description
        )

        this.shown.value = true
    }

    fun dismiss() {
        this.shown.value = false
    }
}

@Composable
fun rememberRecipeBookCreationDialogState(
    key: String
): RecipeBookCreationDialogState {
    val value by foreverRememberNotSavable(
        key = key,
        initialValue = RecipeBookCreationDialogState()
    )

    return value
}

class RecipeBookCreationDialogState(
    val shown: MutableState<Boolean> = mutableStateOf(false)
) {

    var defaultValues = RecipeBookCreationAndEditDefaultValues()

    fun open(values: RecipeBookCreationAndEditDefaultValues = RecipeBookCreationAndEditDefaultValues()) {
        this.defaultValues = values
        this.shown.value = true
    }

    fun dismiss() {
        this.shown.value = false
    }
}

@Composable
fun RecipeBookCreationAndEditDialog(
    client: TandoorClient,
    creationState: RecipeBookCreationDialogState? = null,
    editState: RecipeBookEditDialogState? = null,
    onRefresh: () -> Unit
) {
    if(creationState?.shown?.value != true && editState?.shown?.value != true) return

    val coroutineScope = rememberCoroutineScope()

    val defaultValues =
        if(creationState?.shown?.value == true) creationState.defaultValues else editState?.defaultValues
    if(defaultValues == null) return

    val isEditDialog = editState?.shown?.value == true

    // form values
    var name by rememberSaveable { mutableStateOf(defaultValues.name) }
    var description by rememberSaveable { mutableStateOf(defaultValues.description) }

    val requestRecipeBookState = rememberTandoorRequestState()

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

                            optional = false,

                            check = {
                                if(it.length > 128) {
                                    getString(Res.string.common_name)
                                } else {
                                    null
                                }
                            }
                        )
                    )
                ),
                KitshnFormSection(
                    listOf(
                        KitshnFormTextFieldItem(
                            value = { description },
                            onValueChange = {
                                description = it
                            },

                            label = { Text(stringResource(Res.string.common_description)) },
                            leadingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Rounded.Notes,
                                    stringResource(Res.string.common_description)
                                )
                            },

                            optional = true,

                            check = { null }
                        )
                    )
                ),
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
                        requestRecipeBookState.wrapRequest {
                            editState?.recipeBook?.partialUpdate(
                                name = name,
                                description = description
                            )
                        }

                        onRefresh()
                        editState?.dismiss()
                    } else {
                        val recipeBook = requestRecipeBookState.wrapRequest {
                            client.recipeBook.create(
                                name = name,
                                description = description
                            )
                        }

                        if(recipeBook != null) {
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
                    stringResource(Res.string.action_edit_recipe_book)
                } else {
                    stringResource(Res.string.action_create_recipe_book)
                }
            )
        },
        actions = {
            form.RenderSubmitButton()
        }
    ) { it, _, _ ->
        form.Render(it)
    }

    TandoorRequestErrorHandler(state = requestRecipeBookState)
}