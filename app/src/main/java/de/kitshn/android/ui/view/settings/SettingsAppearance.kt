package de.kitshn.android.ui.view.settings

import android.os.Build
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import de.kitshn.android.R
import de.kitshn.android.ui.component.buttons.BackButton
import de.kitshn.android.ui.component.settings.SettingsSwitchListItem
import de.kitshn.android.ui.view.ViewParameters
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewSettingsAppearance(
    p: ViewParameters
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(state = rememberTopAppBarState())

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { BackButton(p.back) },
                title = { Text(stringResource(id = R.string.settings_section_appearance_label)) },
                scrollBehavior = scrollBehavior
            )
        }
    ) {
        val enableSystemTheme = p.vm.settings.getEnableSystemTheme.collectAsState(initial = true)

        LazyColumn(
            modifier = Modifier
                .padding(it)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            item {
                SettingsSwitchListItem(
                    label = { Text(stringResource(R.string.settings_section_appearance_follow_system_label)) },
                    description = { Text(stringResource(R.string.settings_section_appearance_follow_system_description)) },
                    icon = Icons.Rounded.AutoAwesome,
                    contentDescription = stringResource(R.string.settings_section_appearance_follow_system_label),
                    checked = enableSystemTheme.value
                ) {
                    coroutineScope.launch {
                        p.vm.settings.setEnableSystemTheme(it)
                    }
                }
            }

            item {
                val checked = p.vm.settings.getEnableDarkTheme.collectAsState(initial = false)

                SettingsSwitchListItem(
                    label = { Text(stringResource(R.string.settings_section_appearance_dark_mode_label)) },
                    description = { Text(stringResource(R.string.settings_section_appearance_dark_mode_description)) },
                    icon = Icons.Rounded.DarkMode,
                    contentDescription = stringResource(R.string.settings_section_appearance_dark_mode_label),
                    enabled = !enableSystemTheme.value,
                    checked = checked.value
                ) {
                    coroutineScope.launch {
                        p.vm.settings.setEnableDarkTheme(it)
                    }
                }
            }

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                item {
                    HorizontalDivider(
                        Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )
                }

                item {
                    val checked =
                        p.vm.settings.getEnableDynamicColors.collectAsState(initial = true)

                    SettingsSwitchListItem(
                        label = { Text(stringResource(R.string.settings_section_appearance_dynamic_color_label)) },
                        description = { Text(stringResource(R.string.settings_section_appearance_dynamic_color_description)) },
                        icon = Icons.Rounded.Palette,
                        contentDescription = stringResource(R.string.settings_section_appearance_dynamic_color_label),
                        checked = checked.value
                    ) {
                        coroutineScope.launch {
                            p.vm.settings.setEnableDynamicColors(it)
                        }
                    }
                }
            }
        }
    }
}