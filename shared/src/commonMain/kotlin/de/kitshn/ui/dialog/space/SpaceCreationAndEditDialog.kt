package de.kitshn.ui.dialog.space

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.TandoorRequestStateState
import de.kitshn.api.tandoor.model.PartialTandoorSpace
import de.kitshn.api.tandoor.model.TandoorSpace
import de.kitshn.model.form.KitshnForm
import de.kitshn.model.form.KitshnFormSection
import de.kitshn.model.form.item.field.KitshnFormTextFieldItem
import de.kitshn.ui.dialog.AdaptiveFullscreenDialog
import de.kitshn.ui.dialog.common.CommonDeletionDialog
import kitshn.shared.generated.resources.Res
import kitshn.shared.generated.resources.action_create
import kitshn.shared.generated.resources.action_create_space
import kitshn.shared.generated.resources.action_delete
import kitshn.shared.generated.resources.action_edit_space
import kitshn.shared.generated.resources.action_save
import kitshn.shared.generated.resources.common_name
import kitshn.shared.generated.resources.form_error_field_empty
import kitshn.shared.generated.resources.form_error_name_max_128
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

data class SpaceCreationAndEditDialogState(
    val shown: Boolean = false,
    val editing: TandoorSpace? = null,
    val name: String = "",
    val deleteConfirmation: TandoorSpace? = null,
    val saveState: TandoorRequestStateState = TandoorRequestStateState.IDLE,
    val deleteState: TandoorRequestStateState = TandoorRequestStateState.IDLE,
) {
    val isEdit: Boolean get() = editing != null
    val isSaving: Boolean get() = saveState == TandoorRequestStateState.LOADING
    val isDeleting: Boolean get() = deleteState == TandoorRequestStateState.LOADING
    val isProcessing: Boolean get() = isSaving || isDeleting
}

sealed interface SpaceCreationAndEditDialogEvent {
    data class NameChange(val name: String) : SpaceCreationAndEditDialogEvent
    data class Save(val partial: PartialTandoorSpace) : SpaceCreationAndEditDialogEvent
    data object RequestDelete : SpaceCreationAndEditDialogEvent
    data class ConfirmDelete(val space: TandoorSpace) : SpaceCreationAndEditDialogEvent
    data object CancelDelete : SpaceCreationAndEditDialogEvent
    data object Dismiss : SpaceCreationAndEditDialogEvent
}

suspend fun validateSpaceName(name: String): String? = when {
    name.length > 128 -> getString(Res.string.form_error_name_max_128)
    name.isBlank() -> getString(Res.string.form_error_field_empty)
    else -> null
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SpaceCreationAndEditDialog(
    state: SpaceCreationAndEditDialogState,
    onEvent: (SpaceCreationAndEditDialogEvent) -> Unit,
) {
    if (!state.shown) return

    val currentState by rememberUpdatedState(state)
    val currentOnEvent by rememberUpdatedState(onEvent)

    val form = remember {
        KitshnForm(
            submitButton = {
                Button(onClick = it, enabled = !currentState.isProcessing) {
                    if (currentState.isSaving) {
                        CircularWavyProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text(
                            text = if (currentState.isEdit) {
                                stringResource(Res.string.action_save)
                            } else {
                                stringResource(Res.string.action_create)
                            }
                        )
                    }
                }
            },
            onSubmit = {
                currentOnEvent(
                    SpaceCreationAndEditDialogEvent.Save(
                        PartialTandoorSpace(name = currentState.name)
                    )
                )
            },
            sections = listOf(
                KitshnFormSection(
                    listOf(
                        KitshnFormTextFieldItem(
                            value = { currentState.name },
                            onValueChange = {
                                currentOnEvent(SpaceCreationAndEditDialogEvent.NameChange(it))
                            },
                            label = { Text(stringResource(Res.string.common_name)) },
                            leadingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Rounded.Label,
                                    stringResource(Res.string.common_name)
                                )
                            },
                            optional = false,
                            check = ::validateSpaceName,
                        )
                    )
                )
            )
        )
    }

    AdaptiveFullscreenDialog(
        onDismiss = {
            if (!state.isProcessing) onEvent(SpaceCreationAndEditDialogEvent.Dismiss)
        },
        title = {
            Text(
                text = if (state.isEdit) {
                    stringResource(Res.string.action_edit_space)
                } else {
                    stringResource(Res.string.action_create_space)
                }
            )
        },
        topAppBarActions = {
            if (state.isEdit) {
                IconButton(
                    onClick = { onEvent(SpaceCreationAndEditDialogEvent.RequestDelete) },
                    enabled = !state.isProcessing,
                ) {
                    if (state.isDeleting) {
                        CircularWavyProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Icon(Icons.Rounded.Delete, stringResource(Res.string.action_delete))
                    }
                }
            }
        },
        actions = {
            form.RenderSubmitButton()
        }
    ) { it, _, _ ->
        form.Render(it)
    }

    CommonDeletionDialog(
        model = state.deleteConfirmation,
        onConfirm = { onEvent(SpaceCreationAndEditDialogEvent.ConfirmDelete(it)) },
        onDismiss = { onEvent(SpaceCreationAndEditDialogEvent.CancelDelete) },
    )
}
