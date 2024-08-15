package de.kitshn.android.ui.selectionMode.values

import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun ListItemDefaults.selectionModeListItemColors(
    selected: Boolean,
    defaultColors: ListItemColors = colors(),
    selectionModeColors: ListItemColors = colors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        headlineColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
): ListItemColors {
    return if(selected) {
        selectionModeColors
    } else {
        defaultColors
    }
}