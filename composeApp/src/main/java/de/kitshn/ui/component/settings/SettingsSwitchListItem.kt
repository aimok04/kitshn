package de.kitshn.ui.component.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun SettingsSwitchListItem(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(
        start = 16.dp,
        end = 16.dp,
        top = 8.dp,
        bottom = 8.dp
    ),
    label: @Composable () -> Unit,
    description: @Composable () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean = true,
    checked: Boolean = false,
    onCheckedChanged: (checked: Boolean) -> Unit
) {
    SettingsListItem(
        modifier = modifier,
        contentPadding = contentPadding,
        label = label,
        description = description,
        icon = icon,
        enabled = enabled,
        contentDescription = contentDescription,
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = { if(enabled) onCheckedChanged(it) },
                enabled = enabled
            )
        }
    ) {
        onCheckedChanged(!checked)
    }
}