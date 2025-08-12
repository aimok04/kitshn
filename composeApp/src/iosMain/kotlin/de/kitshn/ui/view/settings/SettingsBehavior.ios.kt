package de.kitshn.ui.view.settings

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CarCrash
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontStyle
import de.kitshn.KitshnViewModel
import de.kitshn.ui.component.settings.SettingsListItem
import de.kitshn.ui.component.settings.SettingsListItemPosition
import de.kitshn.ui.component.settings.SettingsSwitchListItem
import de.kitshn.ui.dialog.LaunchTimerInfoBottomSheet
import de.kitshn.ui.dialog.rememberLaunchTimerInfoBottomSheetState
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.common_option_requires_restart
import kitshn.composeapp.generated.resources.settings_section_behavior_enable_crash_reporting
import kitshn.composeapp.generated.resources.settings_section_behavior_ios_install_timer_shortcut_description
import kitshn.composeapp.generated.resources.settings_section_behavior_ios_install_timer_shortcut_label
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import platform.Foundation.NSUserDefaults

const val KEY_ALLOW_CRASH_REPORTING = "allow_crash_reporting"

actual fun LazyListScope.prependItems(
    vm: KitshnViewModel
) {
    item {
        val launchTimerInfoBottomSheetState = rememberLaunchTimerInfoBottomSheetState()

        SettingsListItem(
            position = SettingsListItemPosition.SINGULAR,
            label = {
                Text(stringResource(Res.string.settings_section_behavior_ios_install_timer_shortcut_label))
            },
            description = {
                Text(stringResource(Res.string.settings_section_behavior_ios_install_timer_shortcut_description))
            },
            icon = Icons.Rounded.Download,
            contentDescription = stringResource(Res.string.settings_section_behavior_ios_install_timer_shortcut_label)
        ) {
            vm.settings.setIosTimerShortcutInstalled(false)
            launchTimerInfoBottomSheetState.open()
        }

        LaunchTimerInfoBottomSheet(
            vm = vm,
            state = launchTimerInfoBottomSheetState
        )
    }

    item {
        var value by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            while(true) {
                value = NSUserDefaults.standardUserDefaults.boolForKey(KEY_ALLOW_CRASH_REPORTING)
                delay(200)
            }
        }

        SettingsSwitchListItem(
            position = SettingsListItemPosition.SINGULAR,
            label = { Text(stringResource(Res.string.settings_section_behavior_enable_crash_reporting)) },
            description = {
                Text(
                    stringResource(Res.string.common_option_requires_restart),
                    fontStyle = FontStyle.Italic
                )
            },
            icon = Icons.Rounded.CarCrash,
            contentDescription = stringResource(Res.string.settings_section_behavior_enable_crash_reporting),
            checked = value
        ) {
            NSUserDefaults.standardUserDefaults.setBool(it, KEY_ALLOW_CRASH_REPORTING)
        }
    }
}