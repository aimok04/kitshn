package de.kitshn.android.ui.view.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Copyright
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material.icons.rounded.Report
import androidx.compose.material.icons.rounded.Web
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.ui.compose.m3.util.author
import com.mikepenz.aboutlibraries.util.withContext
import de.kitshn.android.BuildConfig
import de.kitshn.android.R
import de.kitshn.android.launchCustomTabs
import de.kitshn.android.launchMarketPage
import de.kitshn.android.ui.component.buttons.BackButton
import de.kitshn.android.ui.component.settings.SettingsListItem
import de.kitshn.android.ui.dialog.AboutLibraryBottomSheet
import de.kitshn.android.ui.dialog.rememberAboutLibraryBottomSheetState
import de.kitshn.android.ui.theme.KitshnYellow
import de.kitshn.android.ui.theme.Typography
import de.kitshn.android.ui.view.ViewParameters

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ViewSettingsAbout(
    p: ViewParameters
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(state = rememberTopAppBarState())

    val libs by remember {
        mutableStateOf<Libs?>(
            Libs.Builder()
                .withContext(context)
                .build()
        )
    }

    val aboutLibraryBottomSheetState = rememberAboutLibraryBottomSheetState()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { BackButton(p.back) },
                title = { Text(stringResource(id = R.string.settings_section_about_label)) },
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
                        label = { Text("${stringResource(R.string.app_name)} (${BuildConfig.BUILD_TYPE})") },
                        description = { Text("${stringResource(id = R.string.common_version)} ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})") },
                        icon = ImageVector.vectorResource(id = R.drawable.ic_logo_ico),
                        iconTint = KitshnYellow,
                        contentDescription = stringResource(R.string.app_name)
                    )
                }

                item {
                    SettingsListItem(
                        label = { Text("Github") },
                        description = { Text(stringResource(id = R.string.about_github)) },
                        icon = ImageVector.vectorResource(id = R.drawable.github_mark),
                        contentDescription = "Github",
                        onClick = {
                            context.launchCustomTabs(context.getString(R.string.about_github))
                        }
                    )
                }

                item {
                    SettingsListItem(
                        label = { Text(stringResource(R.string.action_open_issue)) },
                        description = { Text(stringResource(R.string.settings_section_about_item_new_issue_description)) },
                        icon = Icons.Rounded.Report,
                        contentDescription = stringResource(R.string.action_open_issue),
                        onClick = {
                            context.launchCustomTabs(context.getString(R.string.about_github_new_issue))
                        }
                    )
                }

                item {
                    SettingsListItem(
                        label = { Text(stringResource(R.string.common_website)) },
                        description = { Text(stringResource(id = R.string.about_contact_website)) },
                        icon = Icons.Rounded.Web,
                        contentDescription = stringResource(R.string.common_website),
                        onClick = {
                            Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse(context.getString(R.string.about_contact_website))
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }.let {
                                context.startActivity(it)
                            }
                        }
                    )
                }

                item {
                    SettingsListItem(
                        label = { Text(stringResource(R.string.common_contact)) },
                        description = { Text(stringResource(id = R.string.about_contact_mailto)) },
                        icon = Icons.Rounded.Mail,
                        contentDescription = stringResource(R.string.common_contact),
                        onClick = {
                            Intent(Intent.ACTION_SENDTO).apply {
                                data =
                                    Uri.parse("mailto:${context.getString(R.string.about_contact_mailto)}")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }.let {
                                context.startActivity(it)
                            }
                        }
                    )
                }

                item {
                    SettingsListItem(
                        label = { Text(stringResource(R.string.common_review)) },
                        description = { Text(stringResource(R.string.settings_section_about_item_review_description)) },
                        icon = Icons.Rounded.RateReview,
                        contentDescription = stringResource(R.string.common_review),
                        onClick = {
                            context.launchMarketPage(BuildConfig.APPLICATION_ID)
                        }
                    )
                }

                item {
                    HorizontalDivider(
                        Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )
                }

                item {
                    SettingsListItem(
                        label = { Text(stringResource(R.string.settings_section_about_item_freepik)) },
                        description = { Text("Icon made by Freepik from www.flaticon.com") },
                        icon = Icons.Rounded.Copyright,
                        contentDescription = stringResource(R.string.settings_section_about_item_freepik),
                        onClick = {
                            context.launchCustomTabs("https://www.flaticon.com/free-icon/chef-hat-outline-symbol_45582")
                        }
                    )
                }

                items(libs?.libraries?.size ?: 0) {
                    val library = libs!!.libraries[it]

                    SettingsListItem(
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
                                style = Typography.labelMedium
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