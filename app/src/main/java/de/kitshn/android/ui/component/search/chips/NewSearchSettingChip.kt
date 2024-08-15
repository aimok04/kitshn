package de.kitshn.android.ui.component.search.chips

import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.kitshn.android.R
import de.kitshn.android.ui.component.search.AdditionalSearchSettingsChipRowState

@Composable
fun NewSearchSettingChip(
    state: AdditionalSearchSettingsChipRowState
) {
    FilterChip(
        selected = state.new,
        onClick = {
            state.new = !state.new
            state.update()
        },
        label = { Text(stringResource(R.string.common_new)) }
    )
}