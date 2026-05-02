package de.kitshn.ui.view.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.TandoorTimeoutSettings
import de.kitshn.ui.component.buttons.BackButton
import de.kitshn.ui.component.settings.SettingsListItem
import de.kitshn.ui.component.settings.SettingsListItemPosition
import de.kitshn.ui.dialog.TimeoutSelectionBottomSheet
import de.kitshn.ui.dialog.rememberTimeoutSelectionBottomSheetState
import de.kitshn.ui.view.ViewParameters
import kitshn.shared.generated.resources.Res
import kitshn.shared.generated.resources.settings_section_server_advanced_label
import kitshn.shared.generated.resources.settings_section_server_advanced_long_timeout_description
import kitshn.shared.generated.resources.settings_section_server_advanced_long_timeout_label
import kitshn.shared.generated.resources.settings_section_server_advanced_reset_description
import kitshn.shared.generated.resources.settings_section_server_advanced_reset_label
import kitshn.shared.generated.resources.settings_section_server_advanced_short_timeout_description
import kitshn.shared.generated.resources.settings_section_server_advanced_short_timeout_label
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SettingsServerAdvancedContent(
    p: ViewParameters,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    val timeoutSettingsFlow =
        p.vm.settings.getTandoorTimeoutSettings.collectAsState(initial = TandoorTimeoutSettings())
    val timeoutSettings = timeoutSettingsFlow.value

    val shortOptions = listOf(2000L, 5000L, 10000L, 20000L, 30000L, 60000L)
    val longOptions = listOf(30000L, 60000L, 120000L, 300000L, 600000L)

    val timeoutSelectionBottomSheetState = rememberTimeoutSelectionBottomSheetState()

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)

    ) {
        item {
            SettingsListItem(
                position = SettingsListItemPosition.TOP,
                label = { Text(stringResource(Res.string.settings_section_server_advanced_short_timeout_label)) },
                description = {
                    Text(
                        stringResource(
                            Res.string.settings_section_server_advanced_short_timeout_description,
                            (timeoutSettings.shortTimeout / 1000).toInt()
                        )
                    )
                },
                icon = Icons.Rounded.Timer,
                contentDescription = stringResource(Res.string.settings_section_server_advanced_short_timeout_label)
            ) {
                timeoutSelectionBottomSheetState.open(
                    options = shortOptions,
                    selectedValue = timeoutSettings.shortTimeout,
                    defaultValue = TandoorTimeoutSettings().shortTimeout,
                    onSelect = { newValue ->
                        coroutineScope.launch {
                            p.vm.settings.setTandoorTimeoutSettings(
                                timeoutSettings.copy(shortTimeout = newValue)
                            )
                            timeoutSelectionBottomSheetState.dismiss()
                        }
                    }
                )
            }
        }

        item {
            SettingsListItem(
                position = SettingsListItemPosition.BOTTOM,
                label = { Text(stringResource(Res.string.settings_section_server_advanced_long_timeout_label)) },
                description = {
                    Text(
                        stringResource(
                            Res.string.settings_section_server_advanced_long_timeout_description,
                            (timeoutSettings.longTimeout / 1000).toInt()
                        )
                    )
                },
                icon = Icons.Rounded.Timer,
                contentDescription = stringResource(Res.string.settings_section_server_advanced_long_timeout_label)
            ) {
                timeoutSelectionBottomSheetState.open(
                    options = longOptions,
                    selectedValue = timeoutSettings.longTimeout,
                    defaultValue = TandoorTimeoutSettings().longTimeout,
                    onSelect = { newValue ->
                        coroutineScope.launch {
                            p.vm.settings.setTandoorTimeoutSettings(
                                timeoutSettings.copy(longTimeout = newValue)
                            )
                            timeoutSelectionBottomSheetState.dismiss()
                        }
                    }
                )
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
        }

        item {
            SettingsListItem(
                position = SettingsListItemPosition.SINGULAR,
                label = { Text(stringResource(Res.string.settings_section_server_advanced_reset_label)) },
                description = { Text(stringResource(Res.string.settings_section_server_advanced_reset_description)) },
                icon = Icons.Rounded.Refresh,
                contentDescription = stringResource(Res.string.settings_section_server_advanced_reset_label)
            ) {
                coroutineScope.launch {
                    p.vm.settings.setTandoorTimeoutSettings(TandoorTimeoutSettings())
                }
            }
        }
    }

    TimeoutSelectionBottomSheet(
        state = timeoutSelectionBottomSheetState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewSettingsServerAdvanced(
    p: ViewParameters
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(state = rememberTopAppBarState())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = { BackButton(p.back) },
                title = { Text(stringResource(Res.string.settings_section_server_advanced_label)) },
                scrollBehavior = scrollBehavior
            )
        }
    ) {
        SettingsServerAdvancedContent(
            p = p,
            modifier = Modifier
                .padding(it)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        )
    }
}
