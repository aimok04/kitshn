package de.kitshn.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import de.kitshn.ui.component.settings.SettingsListItemPosition
import org.jetbrains.compose.resources.StringResource

abstract class SettingsBaseModel {
    abstract val id: String
}

class SettingsDividerModel(override val id: String) : SettingsBaseModel()

data class SettingsItemModel(
    override val id: String,
    val position: SettingsListItemPosition,
    val icon: ImageVector,
    val contentDescription: StringResource,
    val label: StringResource,
    val description: StringResource,
    val enabled: Boolean = true,
    val onClick: () -> Unit = {},
    val content: (@Composable (back: (() -> Unit)?) -> Unit)? = null
) : SettingsBaseModel()
