package de.kitshn.ui.dialog

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import de.kitshn.KitshnViewModel
import de.kitshn.Platforms
import de.kitshn.platformDetails
import de.kitshn.ui.component.settings.SettingsSwitchListItem
import kitshn.composeApp.BuildConfig
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_continue
import kitshn.composeapp.generated.resources.action_open_source
import kitshn.composeapp.generated.resources.ios_shortcut_branding
import kitshn.composeapp.generated.resources.ios_timer_dialog_action_install_shortcut
import kitshn.composeapp.generated.resources.ios_timer_dialog_body
import kitshn.composeapp.generated.resources.ios_timer_dialog_shortcut_installed_switch
import kitshn.composeapp.generated.resources.ios_timer_dialog_title
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun rememberLaunchTimerInfoBottomSheetState(): LaunchTimerInfoBottomSheetState {
    return remember {
        LaunchTimerInfoBottomSheetState()
    }
}

class LaunchTimerInfoBottomSheetState(
    val shown: MutableState<Boolean> = mutableStateOf(false)
) {
    fun open() {
        this.shown.value = true
    }

    fun dismiss() {
        this.shown.value = false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaunchTimerInfoBottomSheet(
    vm: KitshnViewModel,
    state: LaunchTimerInfoBottomSheetState
) {
    // only show on iOS
    if (platformDetails.platform != Platforms.IOS) return

    val coroutineScope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    if (state.shown.value) {
        val sheetState = rememberModalBottomSheetState()
        var secondStage by remember { mutableStateOf(false) }

        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                state.dismiss()
            }
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        modifier = Modifier.size(96.dp),
                        painter = painterResource(Res.drawable.ios_shortcut_branding),
                        contentDescription = stringResource(Res.string.ios_timer_dialog_title)
                    )

                    Spacer(Modifier.width(24.dp))

                    Text(
                        text = stringResource(Res.string.ios_timer_dialog_title),
                        style = MaterialTheme.typography.displaySmall
                    )
                }

                Text(
                    text = stringResource(
                        Res.string.ios_timer_dialog_body,
                        BuildConfig.IOS_TIMER_SHORTCUT_NAME
                    )
                )

                AnimatedContent(
                    targetState = secondStage
                ) {
                    when (it) {
                        false -> {
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    secondStage = true

                                    coroutineScope.launch {
                                        sheetState.expand()
                                        uriHandler.openUri(BuildConfig.IOS_TIMER_SHORTCUT_LINK)
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Rounded.OpenInNew,
                                    stringResource(Res.string.action_open_source)
                                )

                                Spacer(Modifier.width(8.dp))

                                Text(stringResource(Res.string.ios_timer_dialog_action_install_shortcut))
                            }
                        }

                        true -> {
                            Column {
                                val buttonFocusRequester = remember { FocusRequester() }
                                var confirmedInstall by remember { mutableStateOf(false) }

                                SettingsSwitchListItem(
                                    label = {
                                        Text(
                                            text = stringResource(Res.string.ios_timer_dialog_shortcut_installed_switch)
                                        )
                                    },
                                    contentDescription = stringResource(Res.string.ios_timer_dialog_shortcut_installed_switch),
                                    checked = confirmedInstall,
                                    onCheckedChanged = {
                                        confirmedInstall = it
                                    }
                                )

                                Spacer(Modifier.height(16.dp))

                                Button(
                                    modifier = Modifier.fillMaxWidth()
                                        .focusRequester(buttonFocusRequester),
                                    enabled = confirmedInstall,
                                    onClick = {
                                        if (!confirmedInstall) return@Button

                                        vm.settings.setIosTimerShortcutInstalled(true)
                                        state.dismiss()
                                    }
                                ) {
                                    Text(stringResource(Res.string.action_continue))
                                }

                                LaunchedEffect(Unit) {
                                    buttonFocusRequester.requestFocus()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}