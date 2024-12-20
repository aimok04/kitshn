package de.kitshn.ui.component.buttons

import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.kitshn.R

enum class WideActionChipType {
    INFO,
    WARNING,
    ERROR
}

@Composable
fun WideActionChip(
    modifier: Modifier = Modifier,
    type: WideActionChipType,
    actionLabel: String,
    onAction: () -> Unit
) {
    ListItem(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                onAction()
            },
        leadingContent = {
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = stringResource(R.string.common_info)
            )
        },
        headlineContent = {
            Text(
                text = actionLabel
            )
        },
        trailingContent = {
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, actionLabel)
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            leadingIconColor = MaterialTheme.colorScheme.error,
            headlineColor = MaterialTheme.colorScheme.onErrorContainer
        )
    )
}