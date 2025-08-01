package de.kitshn.ui.component.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

enum class SettingsListItemPosition {
    TOP,
    BETWEEN,
    BOTTOM,
    SINGULAR
}

@Composable
fun SettingsListItem(
    modifier: Modifier = Modifier,
    position: SettingsListItemPosition = SettingsListItemPosition.SINGULAR,
    overlineContent: @Composable () -> Unit = { },
    label: @Composable () -> Unit,
    description: @Composable (() -> Unit)? = null,
    icon: ImageVector? = null,
    iconTint: Color = LocalContentColor.current,
    contentDescription: String,
    enabled: Boolean = true,
    trailingContent: @Composable () -> Unit = {},
    alternativeColors: Boolean = false,
    selected: Boolean = false,
    onClick: () -> Unit = {}
) {
    val containerColor = if(selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else if(alternativeColors) {
        MaterialTheme.colorScheme.surfaceContainerLow
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    ListItem(
        modifier = modifier
            .padding(
                when(position) {
                    SettingsListItemPosition.TOP -> PaddingValues(
                        top = 8.dp,
                        bottom = 1.dp,
                        start = 16.dp,
                        end = 16.dp
                    )

                    SettingsListItemPosition.BETWEEN -> PaddingValues(
                        top = 1.dp,
                        bottom = 1.dp,
                        start = 16.dp,
                        end = 16.dp
                    )

                    SettingsListItemPosition.BOTTOM -> PaddingValues(
                        top = 1.dp,
                        bottom = 8.dp,
                        start = 16.dp,
                        end = 16.dp
                    )

                    SettingsListItemPosition.SINGULAR -> PaddingValues(
                        top = 8.dp,
                        bottom = 8.dp,
                        start = 16.dp,
                        end = 16.dp
                    )
                }
            )
            .clip(
                when(position) {
                    SettingsListItemPosition.TOP -> RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 4.dp
                    )

                    SettingsListItemPosition.BETWEEN -> RoundedCornerShape(4.dp)
                    SettingsListItemPosition.BOTTOM -> RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 4.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    )

                    SettingsListItemPosition.SINGULAR -> RoundedCornerShape(16.dp)
                }
            )
            .alpha(if(enabled) 1f else 0.5f)
            .clickable { if(enabled) onClick() },
        colors = ListItemDefaults.colors(
            containerColor = containerColor,
            leadingIconColor = MaterialTheme.colorScheme.contentColorFor(containerColor),
            headlineColor = MaterialTheme.colorScheme.contentColorFor(containerColor),
            supportingColor = MaterialTheme.colorScheme.contentColorFor(containerColor)
                .copy(alpha = 0.8f)
        ),
        overlineContent = overlineContent,
        headlineContent = label,
        supportingContent = description,
        leadingContent = if(icon == null) {
            null
        } else {
            {
                Icon(
                    modifier = Modifier
                        .background(
                            if(selected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                            RoundedCornerShape(8.dp)
                        )
                        .padding(4.dp),
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = iconTint
                )
            }
        },
        trailingContent = trailingContent
    )
}