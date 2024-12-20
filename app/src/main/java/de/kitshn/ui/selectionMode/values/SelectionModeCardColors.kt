package de.kitshn.ui.selectionMode.values

import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun CardDefaults.selectionModeCardColors(
    selected: Boolean,
    defaultCardColors: CardColors = cardColors(),
    selectionModeCardColors: CardColors = cardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
): CardColors {
    return if(selected) {
        selectionModeCardColors
    } else {
        defaultCardColors
    }
}