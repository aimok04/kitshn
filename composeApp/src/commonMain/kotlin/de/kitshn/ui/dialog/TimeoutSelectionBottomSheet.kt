package de.kitshn.ui.dialog

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.kitshn.ui.component.settings.SettingsListItem
import de.kitshn.ui.component.settings.SettingsListItemPosition
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.common_default
import kitshn.composeapp.generated.resources.common_plural_seconds
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun rememberTimeoutSelectionBottomSheetState(): TimeoutSelectionBottomSheetState {
    return remember {
        TimeoutSelectionBottomSheetState()
    }
}

class TimeoutSelectionBottomSheetState(
    val shown: MutableState<Boolean> = mutableStateOf(false)
) {
    var options by mutableStateOf<List<Long>>(listOf())
    var selectedValue by mutableStateOf<Long>(0L)
    var defaultValue by mutableStateOf<Long>(0L)
    var onSelect: (Long) -> Unit = {}

    fun open(
        options: List<Long>,
        selectedValue: Long,
        defaultValue: Long,
        onSelect: (Long) -> Unit
    ) {
        this.options = options
        this.selectedValue = selectedValue
        this.defaultValue = defaultValue
        this.onSelect = onSelect
        this.shown.value = true
    }

    fun dismiss() {
        this.shown.value = false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeoutSelectionBottomSheet(
    state: TimeoutSelectionBottomSheetState
) {
    if(!state.shown.value) return

    ModalBottomSheet(
        onDismissRequest = {
            state.dismiss()
        }
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = stringResource(Res.string.settings_section_server_advanced_timeout_selection_title),
            style = MaterialTheme.typography.titleLarge
        )

        LazyColumn(
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            items(state.options.size) { index ->
                val option = state.options[index]
                val position = when {
                    state.options.size == 1 -> SettingsListItemPosition.SINGULAR
                    index == 0 -> SettingsListItemPosition.TOP
                    index == state.options.size - 1 -> SettingsListItemPosition.BOTTOM
                    else -> SettingsListItemPosition.BETWEEN
                }

                val label = pluralStringResource(
                    Res.plurals.common_plural_seconds,
                    (option / 1000).toInt(),
                    (option / 1000).toInt()
                )

                SettingsListItem(
                    position = position,
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(label)
                            if(option == state.defaultValue) {
                                Text(
                                    modifier = Modifier.padding(start = 8.dp),
                                    text = "(${stringResource(Res.string.common_default)})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    icon = Icons.Rounded.Timer,
                    trailingContent = {
                        if(option == state.selectedValue) {
                            Icon(Icons.Rounded.Check, null)
                        }
                    },
                    contentDescription = label
                ) {
                    state.onSelect(option)
                }
            }
        }
    }
}
