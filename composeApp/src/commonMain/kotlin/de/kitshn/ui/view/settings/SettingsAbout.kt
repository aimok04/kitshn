package de.kitshn.ui.view.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Copyright
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Report
import androidx.compose.material.icons.rounded.Web
import androidx.compose.material3.Badge
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.ui.compose.rememberLibraries
import com.mikepenz.aboutlibraries.ui.compose.util.author
import de.kitshn.launchWebsiteHandler
import de.kitshn.platformDetails
import de.kitshn.ui.component.buttons.BackButton
import de.kitshn.ui.component.settings.SettingsListItem
import de.kitshn.ui.component.settings.SettingsListItemPosition
import de.kitshn.ui.dialog.AboutLibraryBottomSheet
import de.kitshn.ui.dialog.rememberAboutLibraryBottomSheetState
import de.kitshn.ui.theme.KitshnYellow
import de.kitshn.ui.theme.Typography
import de.kitshn.ui.view.ViewParameters
import kitshn.composeApp.BuildConfig
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_open_issue
import kitshn.composeapp.generated.resources.app_name
import kitshn.composeapp.generated.resources.common_contact
import kitshn.composeapp.generated.resources.common_version
import kitshn.composeapp.generated.resources.common_website
import kitshn.composeapp.generated.resources.github_mark
import kitshn.composeapp.generated.resources.ic_logo_ico
import kitshn.composeapp.generated.resources.settings_section_about_item_freepik
import kitshn.composeapp.generated.resources.settings_section_about_item_new_issue_description
import kitshn.composeapp.generated.resources.settings_section_about_label
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewSettingsAbout(
    p: ViewParameters
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(state = rememberTopAppBarState())

    val launchWebsite = launchWebsiteHandler()
    val uriHandler = LocalUriHandler.current

    val libs by rememberLibraries {
        Res.readBytes("files/aboutlibraries.json").decodeToString()
    }

    val aboutLibraryBottomSheetState = rememberAboutLibraryBottomSheetState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = { BackButton(p.back) },
                title = { Text(stringResource(Res.string.settings_section_about_label)) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { pv ->
        Column(
            modifier = Modifier
                .padding(pv)
        ) {
            LazyColumn(
                modifier = Modifier
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                item {
                    SettingsListItem(
                        position = SettingsListItemPosition.SINGULAR,
                        label = { Text("${stringResource(Res.string.app_name)} (${platformDetails.buildType}) (${platformDetails.platform.displayName})") },
                        description = { Text("${stringResource(Res.string.common_version)} ${platformDetails.packageExtendedVersion}") },
                        icon = vectorResource(Res.drawable.ic_logo_ico),
                        iconTint = KitshnYellow,
                        contentDescription = stringResource(Res.string.app_name)
                    )
                }

                item {
                    SettingsListItem(
                        position = SettingsListItemPosition.TOP,
                        label = { Text("Github") },
                        description = { Text(BuildConfig.ABOUT_GITHUB) },
                        icon = vectorResource(Res.drawable.github_mark),
                        contentDescription = "Github",
                        onClick = {
                            launchWebsite(BuildConfig.ABOUT_GITHUB)
                        }
                    )
                }

                item {
                    SettingsListItem(
                        position = SettingsListItemPosition.BOTTOM,
                        label = { Text(stringResource(Res.string.action_open_issue)) },
                        description = { Text(stringResource(Res.string.settings_section_about_item_new_issue_description)) },
                        icon = Icons.Rounded.Report,
                        contentDescription = stringResource(Res.string.action_open_issue),
                        onClick = {
                            launchWebsite(BuildConfig.ABOUT_GITHUB_NEW_ISSUE)
                        }
                    )
                }

                item {
                    SettingsListItem(
                        position = SettingsListItemPosition.TOP,
                        label = { Text(stringResource(Res.string.common_website)) },
                        description = { Text(BuildConfig.ABOUT_CONTACT_WEBSITE) },
                        icon = Icons.Rounded.Web,
                        contentDescription = stringResource(Res.string.common_website),
                        onClick = {
                            launchWebsite(BuildConfig.ABOUT_CONTACT_WEBSITE)
                        }
                    )
                }

                item {
                    SettingsListItem(
                        position = SettingsListItemPosition.BOTTOM,
                        label = { Text(stringResource(Res.string.common_contact)) },
                        description = { Text(BuildConfig.ABOUT_CONTACT_MAILTO) },
                        icon = Icons.Rounded.Mail,
                        contentDescription = stringResource(Res.string.common_contact),
                        onClick = {
                            uriHandler.openUri("mailto:${BuildConfig.ABOUT_CONTACT_MAILTO}")
                        }
                    )
                }

                platformSpecificItems()

                item {
                    SettingsListItem(
                        position = SettingsListItemPosition.TOP,
                        label = { Text(stringResource(Res.string.settings_section_about_item_freepik)) },
                        description = { Text("Icon made by Freepik from www.flaticon.com") },
                        icon = Icons.Rounded.Copyright,
                        contentDescription = stringResource(Res.string.settings_section_about_item_freepik),
                        onClick = {
                            launchWebsite("https://www.flaticon.com/free-icon/chef-hat-outline-symbol_45582")
                        }
                    )
                }

                items(libs?.libraries?.size ?: 0) {
                    val library = libs!!.libraries[it]

                    SettingsListItem(
                        position = when(it) {
                            (libs?.libraries?.size ?: 0) - 1 -> SettingsListItemPosition.BOTTOM
                            else -> SettingsListItemPosition.BETWEEN
                        },
                        overlineContent = { Text(library.author) },
                        label = { Text(library.name) },
                        description = {
                            FlowRow(
                                Modifier.padding(top = 8.dp)
                            ) {
                                library.licenses.forEach {
                                    Badge(
                                        containerColor = if(library.openSource)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            MaterialTheme.colorScheme.tertiaryContainer,
                                        contentColor = if(library.openSource)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else
                                            MaterialTheme.colorScheme.onTertiaryContainer
                                    ) {
                                        Text(
                                            text = it.name
                                        )
                                    }
                                }
                            }
                        },
                        trailingContent = {
                            Text(
                                library.artifactVersion ?: "",
                                style = Typography().labelMedium
                            )
                        },
                        contentDescription = library.name,
                        onClick = { aboutLibraryBottomSheetState.open(library) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    AboutLibraryBottomSheet(state = aboutLibraryBottomSheetState)
}

expect fun LazyListScope.platformSpecificItems()