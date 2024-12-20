package de.kitshn.ui.component.search.chips

import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.kitshn.R
import de.kitshn.ui.component.search.AdditionalSearchSettingsChipRowState

@Composable
fun RandomSearchSettingChip(
    state: AdditionalSearchSettingsChipRowState
) {
    FilterChip(
        selected = state.random,
        onClick = {
            state.random = !state.random
            state.update()
        },
        label = { Text(stringResource(R.string.common_random)) }
    )
}