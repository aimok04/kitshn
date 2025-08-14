package de.kitshn.ui.dialog.recipe.import

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.ui.component.settings.SettingsListItem
import de.kitshn.ui.component.settings.SettingsListItemPosition
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.recipe_import_type_ai_description
import kitshn.composeapp.generated.resources.recipe_import_type_ai_label
import kitshn.composeapp.generated.resources.recipe_import_type_social_media_description
import kitshn.composeapp.generated.resources.recipe_import_type_social_media_label
import kitshn.composeapp.generated.resources.recipe_import_type_url_description
import kitshn.composeapp.generated.resources.recipe_import_type_url_label
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

enum class RecipeImportType(
    val icon: ImageVector,
    val label: StringResource,
    val description: StringResource,
    val enabled: (client: TandoorClient) -> Boolean = { true }
) {
    URL(
        icon = Icons.Rounded.Language,
        label = Res.string.recipe_import_type_url_label,
        description = Res.string.recipe_import_type_url_description
    ),
    AI(
        icon = Icons.Rounded.AutoAwesome,
        label = Res.string.recipe_import_type_ai_label,
        description = Res.string.recipe_import_type_ai_description,
        enabled = { it.container.serverSettings?.enable_ai_import ?: false }
    ),
    SOCIAL_MEDIA(
        icon = Icons.Rounded.Tag,
        label = Res.string.recipe_import_type_social_media_label,
        description = Res.string.recipe_import_type_social_media_description,
        enabled = { it.container.serverSettings?.enable_ai_import ?: false }
    )
}

@Composable
fun rememberRecipeImportTypeBottomSheetState(): RecipeImportTypeBottomSheetState {
    return remember {
        RecipeImportTypeBottomSheetState()
    }
}

class RecipeImportTypeBottomSheetState(
    val shown: MutableState<Boolean> = mutableStateOf(false)
) {
    fun open() {
        this.shown.value = true
    }

    fun dismiss() {
        this.shown.value = false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeImportTypeBottomSheet(
    client: TandoorClient,
    state: RecipeImportTypeBottomSheetState,
    onSelect: (type: RecipeImportType) -> Unit
) {
    if(!state.shown.value) return

    val types = remember { RecipeImportType.entries }

    ModalBottomSheet(
        onDismissRequest = {
            state.dismiss()
        }
    ) {
        LazyColumn {
            items(types.size) {
                val type = types[it]

                SettingsListItem(
                    position = when(it) {
                        0 -> SettingsListItemPosition.SINGULAR
                        1 -> SettingsListItemPosition.TOP
                        types.size - 1 -> SettingsListItemPosition.BOTTOM
                        else -> SettingsListItemPosition.BETWEEN
                    },
                    icon = type.icon,
                    label = { Text(stringResource(type.label)) },
                    description = { Text(stringResource(type.description)) },
                    contentDescription = stringResource(type.description),
                    enabled = type.enabled(client)
                ) {
                    onSelect(type)
                    state.dismiss()
                }
            }
        }
    }
}