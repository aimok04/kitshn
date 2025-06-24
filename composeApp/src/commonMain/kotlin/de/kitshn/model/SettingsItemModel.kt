package de.kitshn.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import de.kitshn.ui.component.settings.SettingsListItemPosition

open class SettingsBaseModel
class SettingsDividerModel : SettingsBaseModel()

data class SettingsItemModel(
    val position: SettingsListItemPosition,
    val id: String,
    val icon: ImageVector,
    val contentDescription: String,
    val label: String,
    val description: String,
    val content: @Composable (back: (() -> Unit)?) -> Unit
) : SettingsBaseModel()