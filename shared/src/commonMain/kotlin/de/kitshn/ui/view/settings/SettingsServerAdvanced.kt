package de.kitshn.ui.view.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import de.kitshn.utils.ClientCertificateData
import de.kitshn.utils.rememberClientCertificateSelector
import kitshn.shared.generated.resources.Res
import kitshn.shared.generated.resources.action_abort
import kitshn.shared.generated.resources.action_remove
import kitshn.shared.generated.resources.settings_section_server_advanced_label
import kitshn.shared.generated.resources.settings_section_server_advanced_long_timeout_description
import kitshn.shared.generated.resources.settings_section_server_advanced_long_timeout_label
import kitshn.shared.generated.resources.settings_section_server_advanced_reset_description
import kitshn.shared.generated.resources.settings_section_server_advanced_reset_label
import kitshn.shared.generated.resources.settings_section_server_advanced_short_timeout_description
import kitshn.shared.generated.resources.settings_section_server_advanced_short_timeout_label
import kitshn.shared.generated.resources.settings_section_server_mtls_description_none
import kitshn.shared.generated.resources.settings_section_server_mtls_description_pkcs12
import kitshn.shared.generated.resources.settings_section_server_mtls_label
import kitshn.shared.generated.resources.settings_section_server_mtls_remove
import kitshn.shared.generated.resources.settings_section_server_mtls_remove_dialog_description
import kitshn.shared.generated.resources.settings_section_server_mtls_remove_dialog_title
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

    // mTLS state
    val credentials = p.vm.tandoorClient?.credentials
    val mtlsCertificateAlias = credentials?.mtlsCertificateAlias
    val mtlsCertificateData = credentials?.mtlsCertificateData
    val isMtlsConfigured = mtlsCertificateAlias != null || mtlsCertificateData != null
    var showMtlsRemoveDialog by remember { mutableStateOf(false) }
    val certificateSelector = rememberClientCertificateSelector()

    fun applyMtlsCert(data: ClientCertificateData) {
        val creds = p.vm.tandoorClient?.credentials ?: return
        p.vm.updateCredentials(
            creds.copy(
                mtlsCertificateAlias = data.alias,
                mtlsCertificateData = data.pkcs12DataBase64,
                mtlsCertificatePassword = data.pkcs12Password,
            )
        )
    }

    fun removeMtlsCert() {
        val creds = p.vm.tandoorClient?.credentials ?: return
        p.vm.updateCredentials(
            creds.copy(
                mtlsCertificateAlias = null,
                mtlsCertificateData = null,
                mtlsCertificatePassword = null,
            )
        )
    }

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
            Spacer(Modifier.height(8.dp))
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

        item {
            Spacer(Modifier.height(24.dp))
        }

        item {
            SettingsListItem(
                position = if(isMtlsConfigured) SettingsListItemPosition.TOP else SettingsListItemPosition.SINGULAR,
                label = { Text(stringResource(Res.string.settings_section_server_mtls_label)) },
                description = {
                    Text(
                        when {
                            mtlsCertificateAlias != null -> mtlsCertificateAlias
                            mtlsCertificateData != null -> stringResource(Res.string.settings_section_server_mtls_description_pkcs12)
                            else -> stringResource(Res.string.settings_section_server_mtls_description_none)
                        }
                    )
                },
                icon = Icons.Rounded.Security,
                contentDescription = stringResource(Res.string.settings_section_server_mtls_label)
            ) {
                certificateSelector.selectCertificate { data ->
                    if(data != null) applyMtlsCert(data)
                }
            }
        }

        if(isMtlsConfigured) {
            item {
                SettingsListItem(
                    position = SettingsListItemPosition.BOTTOM,
                    label = { Text(stringResource(Res.string.settings_section_server_mtls_remove)) },
                    icon = Icons.Rounded.Delete,
                    contentDescription = stringResource(Res.string.settings_section_server_mtls_remove)
                ) {
                    showMtlsRemoveDialog = true
                }
            }
        }
    }

    TimeoutSelectionBottomSheet(
        state = timeoutSelectionBottomSheetState
    )

    if(showMtlsRemoveDialog) AlertDialog(
        onDismissRequest = { showMtlsRemoveDialog = false },
        icon = { Icon(Icons.Rounded.Delete, null) },
        title = { Text(stringResource(Res.string.settings_section_server_mtls_remove_dialog_title)) },
        text = { Text(stringResource(Res.string.settings_section_server_mtls_remove_dialog_description)) },
        confirmButton = {
            Button(onClick = {
                showMtlsRemoveDialog = false
                removeMtlsCert()
            }) { Text(stringResource(Res.string.action_remove)) }
        },
        dismissButton = {
            TextButton(onClick = {
                showMtlsRemoveDialog = false
            }) { Text(stringResource(Res.string.action_abort)) }
        }
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
