package de.kitshn.android.ui.route.main.subroute.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.DeveloperBoard
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import de.kitshn.android.BuildConfig
import de.kitshn.android.R
import de.kitshn.android.model.SettingsBaseModel
import de.kitshn.android.model.SettingsDividerModel
import de.kitshn.android.model.SettingsItemModel
import de.kitshn.android.ui.component.settings.SettingsListItem
import de.kitshn.android.ui.layout.KitshnListDetailPaneScaffold
import de.kitshn.android.ui.route.RouteParameters
import de.kitshn.android.ui.view.ViewParameters
import de.kitshn.android.ui.view.settings.ViewSettingsAbout
import de.kitshn.android.ui.view.settings.ViewSettingsAppearance
import de.kitshn.android.ui.view.settings.ViewSettingsBehavior
import de.kitshn.android.ui.view.settings.ViewSettingsDebug
import de.kitshn.android.ui.view.settings.ViewSettingsServer
import org.acra.ACRA

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteMainSubrouteSettings(
    p: RouteParameters
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val settingsModelList = remember {
        mutableStateListOf<SettingsBaseModel>(
            SettingsItemModel(
                id = "SERVER",
                icon = Icons.Rounded.Cloud,
                contentDescription = context.getString(R.string.settings_section_server_description),
                label = context.getString(R.string.settings_section_server_label),
                description = context.getString(R.string.settings_section_server_description),
                content = { ViewSettingsServer(ViewParameters(p.vm, it)) }
            ),
            SettingsItemModel(
                id = "APPEARANCE",
                icon = Icons.Rounded.Palette,
                contentDescription = context.getString(R.string.settings_section_appearance_description),
                label = context.getString(R.string.settings_section_appearance_label),
                description = context.getString(R.string.settings_section_appearance_description),
                content = { ViewSettingsAppearance(ViewParameters(p.vm, it)) }
            ),
            SettingsItemModel(
                id = "BEHAVIOR",
                icon = Icons.Rounded.Tune,
                contentDescription = context.getString(R.string.settings_section_behavior_description),
                label = context.getString(R.string.settings_section_behavior_label),
                description = context.getString(R.string.settings_section_behavior_description),
                content = { ViewSettingsBehavior(ViewParameters(p.vm, it)) }
            ),
            SettingsItemModel(
                id = "ABOUT",
                icon = Icons.Rounded.Info,
                contentDescription = context.getString(R.string.settings_section_about_description),
                label = context.getString(R.string.settings_section_about_label),
                description = context.getString(R.string.settings_section_about_description),
                content = { ViewSettingsAbout(ViewParameters(p.vm, it)) }
            )
        ).apply {
            if(BuildConfig.DEBUG) add(SettingsItemModel(
                id = "DEBUG",
                icon = Icons.Rounded.DeveloperBoard,
                contentDescription = "Test experimental settings",
                label = "Debug settings",
                description = "Test experimental settings",
                content = { ViewSettingsDebug(ViewParameters(p.vm, it)) }
            ))
        }
    }

    KitshnListDetailPaneScaffold(
        key = "RouteMainSubrouteSettings",
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.navigation_settings)) },
                colors = it,
                actions = {
                    IconButton(
                        onClick = {
                            ACRA.errorReporter.handleException(null)
                        }
                    ) {
                        Icon(Icons.Rounded.BugReport, stringResource(R.string.common_error_report))
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        listContent = { pv, selectedId, supportsMultiplePanes, _, select ->
            LazyColumn(
                modifier = Modifier
                    .padding(pv)
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