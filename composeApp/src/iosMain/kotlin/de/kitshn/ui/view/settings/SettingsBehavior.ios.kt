package de.kitshn.ui.view.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CarCrash
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import de.kitshn.ui.component.settings.SettingsSwitchListItem
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.common_option_requires_restart
import kitshn.composeapp.generated.resources.settings_section_behavior_enable_crash_reporting
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import platform.Foundation.NSUserDefaults

const val KEY_ALLOW_CRASH_REPORTING = "allow_crash_reporting"

actual fun LazyListScope.prependItems() {
    item {
        var value by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            while(true) {
                value = NSUserDefaults.standardUserDefaults.boolForKey(KEY_ALLOW_CRASH_REPORTING)
                delay(200)
            }
        }

        SettingsSwitchListItem(
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

    item {
        HorizontalDivider(
            Modifier.padding(top = 8.dp, bottom = 8.dp)
        )
    }
}