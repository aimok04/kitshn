package de.kitshn.ui.dialog.household

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Groups2
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.TandoorRequestStateState
import de.kitshn.api.tandoor.model.TandoorHousehold
import de.kitshn.model.form.KitshnForm
import de.kitshn.model.form.KitshnFormSection
import de.kitshn.model.form.item.field.KitshnFormTextFieldItem
import de.kitshn.ui.dialog.AdaptiveFullscreenDialog
import de.kitshn.ui.dialog.common.CommonDeletionDialog
import kitshn.shared.generated.resources.Res
import kitshn.shared.generated.resources.action_create
import kitshn.shared.generated.resources.action_create_household
import kitshn.shared.generated.resources.action_delete
import kitshn.shared.generated.resources.action_edit_household
import kitshn.shared.generated.resources.action_save
import kitshn.shared.generated.resources.common_name
import kitshn.shared.generated.resources.form_error_field_empty
import kitshn.shared.generated.resources.form_error_name_max_128
import kitshn.shared.generated.resources.household_creation_intro_description
import kitshn.shared.generated.resources.household_creation_intro_title
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

data class PartialTandoorHousehold(
    val name: String
)

data class HouseholdCreationAndEditDialogState(
    val shown: Boolean = false,
    val editing: TandoorHousehold? = null,
    val name: String = "",
    val deleteConfirmation: TandoorHousehold? = null,
    val saveState: TandoorRequestStateState = TandoorRequestStateState.IDLE,
    val deleteState: TandoorRequestStateState = TandoorRequestStateState.IDLE,
) {
    val isEdit: Boolean get() = editing != null
    val isSaving: Boolean get() = saveState == TandoorRequestStateState.LOADING
    val isDeleting: Boolean get() = deleteState == TandoorRequestStateState.LOADING
    val isProcessing: Boolean get() = isSaving || isDeleting
}

sealed interface HouseholdCreationAndEditDialogEvent {
    data class NameChange(val name: String) : HouseholdCreationAndEditDialogEvent
    data class Save(val household: PartialTandoorHousehold) : HouseholdCreationAndEditDialogEvent
    data object RequestDelete : HouseholdCreationAndEditDialogEvent
    data class ConfirmDelete(val household: TandoorHousehold) : HouseholdCreationAndEditDialogEvent
    data object CancelDelete : HouseholdCreationAndEditDialogEvent
    data object Dismiss : HouseholdCreationAndEditDialogEvent
}

suspend fun validateHouseholdName(name: String): String? = when {
    name.length > 128 -> getString(Res.string.form_error_name_max_128)
    name.isBlank() -> getString(Res.string.form_error_field_empty)
    else -> null
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HouseholdCreationAndEditDialog(
    state: HouseholdCreationAndEditDialogState,
    onEvent: (HouseholdCreationAndEditDialogEvent) -> Unit,
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
                    HouseholdCreationAndEditDialogEvent.Save(
                        PartialTandoorHousehold(currentState.name)
                    )
                )
            },
            sections = listOf(
                KitshnFormSection(
                    listOf(
                        KitshnFormTextFieldItem(
                            value = { currentState.name },
                            onValueChange = {
                                currentOnEvent(HouseholdCreationAndEditDialogEvent.NameChange(it))
                            },
                            label = { Text(stringResource(Res.string.common_name)) },
                            leadingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Rounded.Label,
                                    stringResource(Res.string.common_name)
                                )
                            },
                            optional = false,
                            check = ::validateHouseholdName,
                        )
                    )
                )
            )
        )
    }

    AdaptiveFullscreenDialog(
        onDismiss = {
            if (!state.isProcessing) onEvent(HouseholdCreationAndEditDialogEvent.Dismiss)
        },
        title = {
            Text(
                text = if (state.isEdit) {
                    stringResource(Res.string.action_edit_household)
                } else {
                    stringResource(Res.string.action_create_household)
                }
            )
        },
        topAppBarActions = {
            if (state.isEdit) {
                IconButton(
                    onClick = { onEvent(HouseholdCreationAndEditDialogEvent.RequestDelete) },
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
        form.Render(it) {
            item(span = { GridItemSpan(this.maxCurrentLineSpan) }) {
                HouseholdIntroCard()
            }
        }
    }

    CommonDeletionDialog(
        model = state.deleteConfirmation,
        onConfirm = { onEvent(HouseholdCreationAndEditDialogEvent.ConfirmDelete(it)) },
        onDismiss = { onEvent(HouseholdCreationAndEditDialogEvent.CancelDelete) },
    )

}

@Composable
private fun HouseholdIntroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Groups2,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(Res.string.household_creation_intro_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(Res.string.household_creation_intro_description),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
