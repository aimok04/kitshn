package de.kitshn.ui.component.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import de.kitshn.ui.LocalSnackbarHostState
import kitshn.shared.generated.resources.Res
import kitshn.shared.generated.resources.common_offline_action_unavailable
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

enum class SettingsListItemPosition {
    TOP,
    BETWEEN,
    BOTTOM,
    SINGULAR
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
    onlineOnly: Boolean = false,
    isOnline: Boolean = true,
    trailingContent: @Composable () -> Unit = {},
    alternativeColors: Boolean = false,
    selected: Boolean = false,
    containerColor: Color? = null,
    onClick: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHostState.current
    val offlineWarning = stringResource(Res.string.common_offline_action_unavailable)

    val disabledAlpha = ListItemDefaults.colors().disabledContentColor.alpha
    val effectivelyEnabled = enabled && (isOnline || !onlineOnly)

    val finalContainerColor = containerColor
        ?: if(alternativeColors) {
            MaterialTheme.colorScheme.surfaceContainerLow
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        }

    val supportingColor = MaterialTheme.colorScheme.contentColorFor(finalContainerColor)

    val colors = ListItemDefaults.colors(
        containerColor = finalContainerColor,
        leadingIconColor = MaterialTheme.colorScheme.contentColorFor(finalContainerColor),
        headlineColor = MaterialTheme.colorScheme.contentColorFor(finalContainerColor),
        supportingColor = when(supportingColor.isSpecified) {
            true -> supportingColor.copy(alpha = 0.8f)
            else -> supportingColor
        }
    )

    SegmentedListItem(
        modifier = modifier.alpha(if(effectivelyEnabled) 1f else disabledAlpha),
        enabled = enabled,
        checked = selected,
        onCheckedChange = {
            if(enabled) {
                if(onlineOnly && !isOnline) {
                    coroutineScope.launch {
                        snackbarHostState?.showSnackbar(offlineWarning)
                    }
                } else {
                    onClick()
                }
            }
        },

        shapes = ListItemDefaults.segmentedShapes(
            index = when(position) {
                SettingsListItemPosition.TOP -> 0
                SettingsListItemPosition.BETWEEN -> 1
                SettingsListItemPosition.BOTTOM -> 2
                else -> 0
            },
            count = 3,
            defaultShapes = ListItemDefaults.shapes(
                shape = when(position == SettingsListItemPosition.SINGULAR) {
                    true -> RoundedCornerShape(16.dp)
                    false -> null
                }
            )
        ),

        colors = colors,
        overlineContent = overlineContent,
        content = label,
        supportingContent = description,
        leadingContent = if(icon == null) {
            null
        } else {
            {
                Icon(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerHigh,
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