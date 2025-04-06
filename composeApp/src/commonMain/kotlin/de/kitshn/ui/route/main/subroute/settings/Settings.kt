package de.kitshn.ui.route.main.subroute.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
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
import de.kitshn.ui.component.settings.SettingsListItem
import de.kitshn.ui.layout.KitshnListDetailPaneScaffold
import de.kitshn.ui.route.RouteParameters
import de.kitshn.ui.view.ViewParameters
import de.kitshn.ui.view.settings.ViewSettingsAbout
import de.kitshn.ui.view.settings.ViewSettingsAppearance
import de.kitshn.ui.view.settings.ViewSettingsBehavior
import de.kitshn.ui.view.settings.ViewSettingsDebug
import de.kitshn.ui.view.settings.ViewSettingsServer
import kitshn.composeApp.BuildConfig
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.common_error_report
import kitshn.composeapp.generated.resources.ios_support_badge
import kitshn.composeapp.generated.resources.ios_support_manage_subscription_description
import kitshn.composeapp.generated.resources.ios_support_manage_subscription_label
import kitshn.composeapp.generated.resources.kofi_support_description
import kitshn.composeapp.generated.resources.kofi_support_label
import kitshn.composeapp.generated.resources.navigation_settings
import kitshn.composeapp.generated.resources.settings_section_about_description
import kitshn.composeapp.generated.resources.settings_section_about_label
import kitshn.composeapp.generated.resources.settings_section_appearance_description
import kitshn.composeapp.generated.resources.settings_section_appearance_label
import kitshn.composeapp.generated.resources.settings_section_behavior_description
import kitshn.composeapp.generated.resources.settings_section_behavior_label
import kitshn.composeapp.generated.resources.settings_section_server_description
import kitshn.composeapp.generated.resources.settings_section_server_label
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteMainSubrouteSettings(
    p: RouteParameters
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val settingsModelList = remember {
        mutableStateListOf<SettingsBaseModel>(

        ).apply {
            if(platformDetails.debug) add(SettingsItemModel(
                id = "DEBUG",
                icon = Icons.Rounded.DeveloperBoard,
                contentDescription = "Test experimental settings",
                label = "Debug settings",
                description = "Test experimental settings",
                content = { ViewSettingsDebug(ViewParameters(p.vm, it)) }
            ))
        }
    }

    val crashReportHandler = crashReportHandler()
    val launchWebsiteHandler = launchWebsiteHandler()

    LaunchedEffect(Unit) {
        settingsModelList.addAll(listOf(
            SettingsItemModel(
                id = "SERVER",
                icon = Icons.Rounded.Cloud,
                contentDescription = getString(Res.string.settings_section_server_description),
                label = getString(Res.string.settings_section_server_label),
                description = getString(Res.string.settings_section_server_description),
                content = { ViewSettingsServer(ViewParameters(p.vm, it)) }
            ),
            SettingsItemModel(
                id = "APPEARANCE",
                icon = Icons.Rounded.Palette,
                contentDescription = getString(Res.string.settings_section_appearance_description),
                label = getString(Res.string.settings_section_appearance_label),
                description = getString(Res.string.settings_section_appearance_description),
                content = { ViewSettingsAppearance(ViewParameters(p.vm, it)) }
            ),
            SettingsItemModel(
                id = "BEHAVIOR",
                icon = Icons.Rounded.Tune,
                contentDescription = getString(Res.string.settings_section_behavior_description),
                label = getString(Res.string.settings_section_behavior_label),
                description = getString(Res.string.settings_section_behavior_description),
                content = { ViewSettingsBehavior(ViewParameters(p.vm, it)) }
            ),
            SettingsItemModel(
                id = "ABOUT",
                icon = Icons.Rounded.Info,
                contentDescription = getString(Res.string.settings_section_about_description),
                label = getString(Res.string.settings_section_about_label),
                description = getString(Res.string.settings_section_about_description),
                content = { ViewSettingsAbout(ViewParameters(p.vm, it)) }
            )
        ))
    }

    KitshnListDetailPaneScaffold(
        key = "RouteMainSubrouteSettings",
        topBar = {
            TopAppBar(
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
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                ) {
                    items(settingsModelList.size) { index ->
                        val model = settingsModelList[index]

                        if(model is SettingsDividerModel) {
                            HorizontalDivider()
                        } else if(model is SettingsItemModel) {
                            SettingsListItem(
                                icon = model.icon,
                                contentDescription = model.contentDescription,
                                label = { Text(model.label) },
                                description = { Text(model.description) },
                                alternativeColors = supportsMultiplePanes,
                                selected = selectedId == model.id
                            ) {
                                select(model.id)
                            }
                        }
                    }
                }

                if(platformDetails.platform == Platforms.IOS) {
                    SettingsListItem(
                        modifier = Modifier.padding(bottom = 8.dp),
                        icon = Icons.Rounded.Diamond,
                        label = { Text(stringResource(Res.string.ios_support_manage_subscription_label)) },
                        description = { Text(stringResource(Res.string.ios_support_manage_subscription_description)) },
                        contentDescription = stringResource(Res.string.ios_support_manage_subscription_description)
                    ) {
                        p.vm.navigateTo("iOS/manageSubscription")
                    }
                } else {
                    SettingsListItem(
                        modifier = Modifier.padding(bottom = 8.dp),
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
        for(item in settingsModelList) {
            if(item !is SettingsItemModel) continue
            if(selectedId != item.id) continue

            item.content(back)
            break
        }
    }
}