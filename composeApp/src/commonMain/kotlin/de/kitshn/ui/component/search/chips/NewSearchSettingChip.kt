package de.kitshn.ui.component.search.chips

import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import de.kitshn.ui.component.search.AdditionalSearchSettingsChipRowState
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.common_new
import org.jetbrains.compose.resources.stringResource

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
        label = { Text(stringResource(Res.string.common_new)) }
    )
}