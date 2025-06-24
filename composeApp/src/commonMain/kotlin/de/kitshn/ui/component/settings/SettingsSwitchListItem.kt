package de.kitshn.ui.component.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import de.kitshn.ui.component.input.expressive.ExpressiveSwitch

@Composable
fun SettingsSwitchListItem(
    modifier: Modifier = Modifier,
    position: SettingsListItemPosition = SettingsListItemPosition.SINGULAR,
    label: @Composable () -> Unit,
    description: (@Composable () -> Unit)? = null,
    icon: ImageVector? = null,
    contentDescription: String,
    enabled: Boolean = true,
    checked: Boolean = false,
    onCheckedChanged: (checked: Boolean) -> Unit
) {
    SettingsListItem(
        modifier = modifier,
        position = position,
        label = label,
        description = description,
        icon = icon,
        enabled = enabled,
        contentDescription = contentDescription,
        trailingContent = {
            ExpressiveSwitch(
                checked = checked,
                onCheckedChange = { if(enabled) onCheckedChanged(it) },
                enabled = enabled
            )
        }
    ) {
        onCheckedChanged(!checked)
    }
}