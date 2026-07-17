package de.kitshn.ui.route.main.subroute.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ViewCarousel
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import de.kitshn.Platforms
import de.kitshn.crash.crashReportHandler
import de.kitshn.launchWebsiteHandler
import de.kitshn.model.SettingsBaseModel
import de.kitshn.model.SettingsDividerModel
import de.kitshn.model.SettingsItemModel
import de.kitshn.platformDetails
import de.kitshn.repo.SpaceRepo
import de.kitshn.ui.component.settings.SettingsListItem
import de.kitshn.ui.component.settings.SettingsListItemPosition
import de.kitshn.ui.dialog.space.SpaceSwitchDialog
import de.kitshn.ui.layout.KitshnListDetailPaneScaffold
import de.kitshn.ui.route.RouteParameters
import de.kitshn.ui.view.ViewParameters
import de.kitshn.ui.view.settings.ViewSettingsAbout
import de.kitshn.ui.view.settings.ViewSettingsAppearance
import de.kitshn.ui.view.settings.ViewSettingsBehavior
import de.kitshn.ui.view.settings.ViewSettingsDebug
import de.kitshn.ui.view.settings.ViewSettingsServer
import kitshn.shared.BuildConfig
import kitshn.shared.generated.resources.Res
import kitshn.shared.generated.resources.action_switch_space
import kitshn.shared.generated.resources.common_error_report
import kitshn.shared.generated.resources.ios_support_badge
import kitshn.shared.generated.resources.ios_support_manage_subscription_description
import kitshn.shared.generated.resources.ios_support_manage_subscription_label
import kitshn.shared.generated.resources.kofi_support_description
import kitshn.shared.generated.resources.kofi_support_label
import kitshn.shared.generated.resources.navigation_settings
import kitshn.shared.generated.resources.settings_section_about_description
import kitshn.shared.generated.resources.settings_section_about_label
import kitshn.shared.generated.resources.settings_section_appearance_description
import kitshn.shared.generated.resources.settings_section_appearance_label
import kitshn.shared.generated.resources.settings_section_behavior_description
import kitshn.shared.generated.resources.settings_section_behavior_label
import kitshn.shared.generated.resources.settings_section_debug_description
import kitshn.shared.generated.resources.settings_section_debug_label
import kitshn.shared.generated.resources.settings_section_server_description
import kitshn.shared.generated.resources.settings_section_server_label
import kitshn.shared.generated.resources.settings_section_space_description
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RouteMainSubrouteSettings(
    p: RouteParameters
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val spaceRepo = koinInject<SpaceRepo>()
    var showSpaceSwitchDialog by remember { mutableStateOf(false) }

    val crashReportHandler = crashReportHandler()
    val launchWebsiteHandler = launchWebsiteHandler()

    val settingsItems = remember {
        buildList<SettingsBaseModel> {
            if(platformDetails.debug) add(SettingsItemModel(
                position = SettingsListItemPosition.SINGULAR,
                id = "DEBUG",
                icon = Icons.Rounded.DeveloperBoard,
                contentDescription = Res.string.settings_section_debug_description,
                label = Res.string.settings_section_debug_label,
                description = Res.string.settings_section_debug_description,
                content = { ViewSettingsDebug(ViewParameters(p.vm, it)) }
            ))
            add(SettingsItemModel(
                position = SettingsListItemPosition.SINGULAR,
                id = "SERVER",
                icon = Icons.Rounded.Cloud,
                contentDescription = Res.string.settings_section_server_description,
                label = Res.string.settings_section_server_label,
                description = Res.string.settings_section_server_description,
                content = { ViewSettingsServer(ViewParameters(p.vm, it)) }
            ))
            add(SettingsItemModel(
                position = SettingsListItemPosition.TOP,
                id = "APPEARANCE",
                icon = Icons.Rounded.Palette,
                contentDescription = Res.string.settings_section_appearance_description,
                label = Res.string.settings_section_appearance_label,
                description = Res.string.settings_section_appearance_description,
                content = { ViewSettingsAppearance(ViewParameters(p.vm, it)) }
            ))
            add(SettingsItemModel(
                position = SettingsListItemPosition.BOTTOM,
                id = "BEHAVIOR",
                icon = Icons.Rounded.Tune,
                contentDescription = Res.string.settings_section_behavior_description,
                label = Res.string.settings_section_behavior_label,
                description = Res.string.settings_section_behavior_description,
                content = { ViewSettingsBehavior(ViewParameters(p.vm, it)) }
            ))
            add(SettingsItemModel(
                position = SettingsListItemPosition.SINGULAR,
                id = "SWITCH_SPACE",
                icon = Icons.Outlined.ViewCarousel,
                contentDescription = Res.string.settings_section_space_description,
                label = Res.string.action_switch_space,
                description = Res.string.settings_section_space_description,
                onClick = { showSpaceSwitchDialog = true }
            ))
            add(SettingsItemModel(
                position = SettingsListItemPosition.SINGULAR,
                id = "ABOUT",
                icon = Icons.Rounded.Info,
                contentDescription = Res.string.settings_section_about_description,
                label = Res.string.settings_section_about_label,
                description = Res.string.settings_section_about_description,
                content = { ViewSettingsAbout(ViewParameters(p.vm, it)) }
            ))
        }
    }

    if (showSpaceSwitchDialog) SpaceSwitchDialog(
        repo = spaceRepo,
        onSwitched = {
            p.vm.refreshApp()
        },
        onDismiss = { showSpaceSwitchDialog = false }
    )

    KitshnListDetailPaneScaffold(
        key = "RouteMainSubrouteSettings",
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.navigation_settings)) },
                colors = it,
                actions = {
                    if(p.vm.uiState.iosIsSubscribed) IconButton(
                        onClick = { }
                    ) {
                        Icon(
                            Icons.Rounded.Diamond, stringResource(Res.string.ios_support_badge)
                        )
                    }

                    if(crashReportHandler != null) IconButton(
                        onClick = {
                            crashReportHandler(null)
                        }
                    ) {
                        Icon(
                            Icons.Rounded.BugReport,
                            stringResource(Res.string.common_error_report)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        listContent = { pv, selectedId, supportsMultiplePanes, _, select ->
            Column(
                Modifier.padding(pv)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f, true)
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                ) {
                    items(
                        items = settingsItems,
                        key = { it.id }
                    ) { model ->
                        if(model is SettingsDividerModel) {
                            HorizontalDivider()
                        } else if(model is SettingsItemModel) {
                            SettingsListItem(
                                position = model.position,
                                icon = model.icon,
                                contentDescription = stringResource(model.contentDescription),
                                label = { Text(stringResource(model.label)) },
                                description = { Text(stringResource(model.description)) },
                                enabled = model.enabled,
                                alternativeColors = supportsMultiplePanes,
                                selected = selectedId == model.id
                            ) {
                                if(model.content == null) {
                                    model.onClick()
                                } else {
                                    select(model.id)
                                }
                            }

                            if(model.position == SettingsListItemPosition.SINGULAR || model.position == SettingsListItemPosition.BOTTOM) {
                                Spacer(Modifier.height(16.dp))
                            }
                        }
                    }
                }

                if(platformDetails.platform == Platforms.IOS) {
                    SettingsListItem(
                        position = SettingsListItemPosition.SINGULAR,
                        modifier = Modifier.padding(16.dp),
                        icon = Icons.Rounded.Diamond,
                        label = { Text(stringResource(Res.string.ios_support_manage_subscription_label)) },
                        description = { Text(stringResource(Res.string.ios_support_manage_subscription_description)) },
                        contentDescription = stringResource(Res.string.ios_support_manage_subscription_description)
                    ) {
                        p.vm.navigateTo("iOS/manageSubscription")
                    }
                } else {
                    SettingsListItem(
                        position = SettingsListItemPosition.SINGULAR,
                        modifier = Modifier.padding(16.dp),
                        icon = Icons.Rounded.Diamond,
                        label = { Text(stringResource(Res.string.kofi_support_label)) },
                        description = { Text(stringResource(Res.string.kofi_support_description)) },
                        contentDescription = stringResource(Res.string.kofi_support_description)
                    ) {
                        launchWebsiteHandler(BuildConfig.FUNDING_KOFI)
                    }
                }
            }
        }
    ) { selectedId, _, _, _, _, back ->
        for(item in settingsItems) {
            if(item !is SettingsItemModel) continue
            if(selectedId != item.id) continue

            item.content?.invoke(back)
            break
        }
    }
}