package de.kitshn.ui.component.search.chips

import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import de.kitshn.ui.component.search.AdditionalSearchSettingsChipRowState
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.common_random
import org.jetbrains.compose.resources.stringResource

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
        label = { Text(stringResource(Res.string.common_random)) }
    )
}