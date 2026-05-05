package de.kitshn.ui.dialog.recipe.import

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Draw
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.window.core.layout.WindowWidthSizeClass
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorRequestState
import de.kitshn.ui.component.settings.ListItemPositionNeedsPadding
import de.kitshn.ui.component.settings.SettingsListItem
import de.kitshn.ui.component.settings.SettingsListItemPosition
import de.kitshn.ui.modifier.fullWidthAlertDialogPadding
import kitshn.shared.generated.resources.Res
import kitshn.shared.generated.resources.action_abort
import kitshn.shared.generated.resources.action_add_or_import_recipe
import kitshn.shared.generated.resources.recipe_import_type_ai_description
import kitshn.shared.generated.resources.recipe_import_type_ai_label
import kitshn.shared.generated.resources.recipe_import_type_manual_description
import kitshn.shared.generated.resources.recipe_import_type_manual_label
import kitshn.shared.generated.resources.recipe_import_type_social_media_description
import kitshn.shared.generated.resources.recipe_import_type_social_media_label
import kitshn.shared.generated.resources.recipe_import_type_url_description
import kitshn.shared.generated.resources.recipe_import_type_url_label
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

enum class RecipeCreationType(
    val icon: ImageVector,
    val label: StringResource,
    val description: StringResource,
    val position: SettingsListItemPosition,
    val requiresAI: Boolean = false
) {
    MANUAL(
        icon = Icons.Rounded.Draw,
        label = Res.string.recipe_import_type_manual_label,
        description = Res.string.recipe_import_type_manual_description,
        position = SettingsListItemPosition.SINGULAR,
    ),
    URL(
        icon = Icons.Rounded.Language,
        label = Res.string.recipe_import_type_url_label,
        position = SettingsListItemPosition.TOP,
        description = Res.string.recipe_import_type_url_description
    ),
    AI(
        icon = Icons.Rounded.AutoAwesome,
        label = Res.string.recipe_import_type_ai_label,
        description = Res.string.recipe_import_type_ai_description,
        position = SettingsListItemPosition.BETWEEN,
        requiresAI = true
    ),
    SOCIAL_MEDIA(
        icon = Icons.Rounded.Tag,
        label = Res.string.recipe_import_type_social_media_label,
        description = Res.string.recipe_import_type_social_media_description,
        position = SettingsListItemPosition.BOTTOM,
        requiresAI = true
    )
}

@Composable
fun rememberRecipeCreationTypePickerState(): RecipeCreationTypePickerState {
    return remember {
        RecipeCreationTypePickerState()
    }
}

class RecipeCreationTypePickerState(
    val shown: MutableState<Boolean> = mutableStateOf(false)
) {
    fun open() {
        this.shown.value = true
    }

    fun dismiss() {
        this.shown.value = false
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RecipeCreationTypePicker(
    client: TandoorClient,
    state: RecipeCreationTypePickerState,
    onSelect: (type: RecipeCreationType) -> Unit
) {
    if (!state.shown.value) return

    var aiEnabled by remember { mutableStateOf(false) }
    LaunchedEffect(client) {
        TandoorRequestState().wrapRequest {
            aiEnabled = client.space.current().ai_enabled
        }
    }

    val select: (RecipeCreationType) -> Unit = {
        onSelect(it)
        state.dismiss()
    }

    val isCompact = currentWindowAdaptiveInfo()
        .windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT

    if (isCompact) {
        ModalBottomSheet(
            onDismissRequest = state::dismiss
        ) {
            RecipeCreationTypeList(
                modifier = Modifier.padding(PaddingValues(16.dp)),
                aiEnabled = aiEnabled,
                onSelect = select
            )
        }
    } else {
        AlertDialog(
            modifier = Modifier.fullWidthAlertDialogPadding(),
            onDismissRequest = state::dismiss,
            title = {
                Text(stringResource(Res.string.action_add_or_import_recipe))
            },
            text = {
                RecipeCreationTypeList(
                    aiEnabled = aiEnabled,
                    onSelect = select
                )
            },
            confirmButton = {
                FilledTonalButton(onClick = state::dismiss) {
                    Text(stringResource(Res.string.action_abort))
                }
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RecipeCreationTypeList(
    aiEnabled: Boolean,
    onSelect: (RecipeCreationType) -> Unit,
    modifier: Modifier = Modifier
) {
    val types = remember { RecipeCreationType.entries }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
    ) {
        types.forEachIndexed { index, type ->
            if (index != 0 && ListItemPositionNeedsPadding(type.position)) {
                Spacer(Modifier.height(16.dp))
            }

            SettingsListItem(
                position = type.position,
                icon = type.icon,
                label = { Text(stringResource(type.label)) },
                description = { Text(stringResource(type.description)) },
                contentDescription = stringResource(type.description),
                enabled = !type.requiresAI || aiEnabled,
                onClick = { onSelect(type) }
            )
        }
    }
}
