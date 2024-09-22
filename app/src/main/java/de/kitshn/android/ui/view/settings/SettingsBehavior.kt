package de.kitshn.android.ui.view.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import de.kitshn.android.R
import de.kitshn.android.ui.component.buttons.BackButton
import de.kitshn.android.ui.component.settings.SettingsSwitchListItem
import de.kitshn.android.ui.view.ViewParameters
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewSettingsBehavior(
    p: ViewParameters
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(state = rememberTopAppBarState())

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { BackButton(p.back) },
                title = { Text(stringResource(id = R.string.settings_section_behavior_label)) },
                scrollBehavior = scrollBehavior
            )
        }
    ) {
        val useShareWrapper = p.vm.settings.getUseShareWrapper.collectAsState(initial = true)
        val hideIngredientAllocationActionChips =
            p.vm.settings.getHideIngredientAllocationActionChips.collectAsState(initial = false)

        LazyColumn(
            modifier = Modifier
                .padding(it)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            item {
                SettingsSwitchListItem(
                    label = { Text(stringResource(R.string.settings_section_behavior_use_share_wrapper_label)) },
                    description = { Text(stringResource(R.string.settings_section_behavior_use_share_wrapper_description)) },
                    icon = Icons.Rounded.Share,
                    contentDescription = stringResource(R.string.settings_section_behavior_use_share_wrapper_label),
                    checked = useShareWrapper.value
                ) {
                    coroutineScope.launch {
                        p.vm.settings.setUseShareWrapper(it)
                    }
                }
            }

            item {
                SettingsSwitchListItem(
                    label = { Text(stringResource(R.string.settings_section_behavior_hide_ingredient_allocation_action_chip_label)) },
                    description = { Text(stringResource(R.string.settings_section_behavior_hide_ingredient_allocation_action_chip_description)) },
                    icon = Icons.Rounded.WarningAmber,
                    contentDescription = stringResource(R.string.settings_section_behavior_hide_ingredient_allocation_action_chip_label),
                    checked = hideIngredientAllocationActionChips.value
                ) {
                    coroutineScope.launch {
                        p.vm.settings.setHideIngredientAllocationActionChips(it)
                    }
                }
            }
        }
    }
}