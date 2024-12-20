package de.kitshn.ui.view.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.Web
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import de.kitshn.ui.component.buttons.BackButton
import de.kitshn.ui.component.settings.SettingsListItem
import de.kitshn.ui.dialog.version.TandoorServerVersionCompatibilityDialog
import de.kitshn.ui.view.ViewParameters
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_sign_out
import kitshn.composeapp.generated.resources.action_sign_out_description
import kitshn.composeapp.generated.resources.common_instance_url
import kitshn.composeapp.generated.resources.common_unknown
import kitshn.composeapp.generated.resources.common_version
import kitshn.composeapp.generated.resources.settings_section_server_label
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewSettingsServer(
    p: ViewParameters
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(state = rememberTopAppBarState())

    val coroutineScope = rememberCoroutineScope()

    var showVersionCompatibilityBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { BackButton(p.back) },
                title = { Text(stringResource(Res.string.settings_section_server_label)) },
                scrollBehavior = scrollBehavior
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(it)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            item {
                SettingsListItem(
                    label = { Text(stringResource(Res.string.common_instance_url)) },
                    description = {
                        Text(
                            p.vm.tandoorClient?.credentials?.instanceUrl
                                ?: stringResource(Res.string.common_unknown)
                        )
                    },
                    icon = Icons.Rounded.Web,
                    contentDescription = stringResource(Res.string.common_instance_url)
                )

                SettingsListItem(
                    label = { Text(stringResource(Res.string.common_version)) },
                    description = {
                        Text(
                            p.vm.tandoorClient?.container?.openapiData?.version
                                ?: stringResource(Res.string.common_unknown)
                        )
                    },
                    icon = Icons.Rounded.Numbers,
                    enabled = p.vm.tandoorClient?.container?.openapiData?.version != null,
                    contentDescription = stringResource(Res.string.common_version)
                ) {
                    coroutineScope.launch {
                        showVersionCompatibilityBottomSheet = true
                    }
                }

                SettingsListItem(
                    label = { Text(stringResource(Res.string.action_sign_out)) },
                    description = { Text(stringResource(Res.string.action_sign_out_description)) },
                    icon = Icons.AutoMirrored.Rounded.Logout,
                    contentDescription = stringResource(Res.string.action_sign_out)
                ) {
                    coroutineScope.launch {
                        p.vm.settings.getOnboardingCompleted
                        p.vm.settings.saveTandoorCredentials(null)

                        p.vm.navHostController?.navigate("onboarding") {
                            popUpTo("main") {
                                inclusive = true
                            }
                        }
                    }
                }
            }
        }
    }

    TandoorServerVersionCompatibilityDialog(
        vm = p.vm,
        shown = showVersionCompatibilityBottomSheet,
        autoDisplay = false
    ) {
        showVersionCompatibilityBottomSheet = false
    }
}