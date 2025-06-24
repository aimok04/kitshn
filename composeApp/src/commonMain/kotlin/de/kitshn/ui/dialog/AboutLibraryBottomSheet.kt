package de.kitshn.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.ui.compose.m3.util.author
import de.kitshn.launchWebsiteHandler
import de.kitshn.ui.theme.Typography
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_open_source
import org.jetbrains.compose.resources.stringResource

@Composable
fun rememberAboutLibraryBottomSheetState(): AboutLibraryBottomSheetState {
    return remember {
        AboutLibraryBottomSheetState()
    }
}

class AboutLibraryBottomSheetState(
    val shown: MutableState<Boolean> = mutableStateOf(false),
    val linkContent: MutableState<Library?> = mutableStateOf(null)
) {
    fun open(linkContent: Library) {
        this.linkContent.value = linkContent
        this.shown.value = true
    }

    fun dismiss() {
        this.shown.value = false
        this.linkContent.value = null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutLibraryBottomSheet(
    state: AboutLibraryBottomSheetState
) {
    val launchWebsite = launchWebsiteHandler()

    val density = LocalDensity.current

    val modalBottomSheetState = rememberModalBottomSheetState()

    val library = state.linkContent.value ?: return

    LaunchedEffect(
        state.shown.value
    ) {
        if(state.shown.value) {
            modalBottomSheetState.show()
        } else {
            modalBottomSheetState.hide()
        }
    }

    ModalBottomSheet(
        modifier = Modifier.padding(
            top = with(density) {
                WindowInsets.statusBars
                    .getTop(density)
                    .toDp() * 2
            }
        ),
        onDismissRequest = {
            state.dismiss()
        },
        sheetState = modalBottomSheetState
    ) {
        LazyColumn(
            Modifier.padding(16.dp)
        ) {
            item {
                Text(
                    text = library.author,
                    style = Typography().bodySmall
                )
            }

            item {
                Spacer(Modifier.height(4.dp))
            }

            item {
                Text(
                    text = library.name,
                    style = Typography().titleLarge
                )
            }

            item {
                Spacer(Modifier.height(16.dp))
            }

            item {
                if(library.description != null) Text(
                    text = library.description!!,
                    style = Typography().bodyLarge
                )
            }

            item {
                Spacer(Modifier.height(16.dp))
            }

            item {
                FlowRow {
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
            }

            if(library.website != null) {
                item {
                    Spacer(Modifier.height(16.dp))
                }

                item {
                    Box(
                        Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        OutlinedButton(
                            onClick = {
                                launchWebsite(library.website!!)
                            }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.OpenInNew,
                                stringResource(Res.string.action_open_source)
                            )

                            Spacer(Modifier.width(8.dp))

                            Text(stringResource(Res.string.action_open_source))
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            library.licenses.forEach {
                item {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = it.year ?: "",
                                style = Typography().bodySmall
                            )

                            Spacer(Modifier.height(4.dp))

                            Text(
                                text = it.name,
                                style = Typography().titleMedium
                            )
                        }

                        if(it.url != null) IconButton(
                            onClick = {
                                launchWebsite(it.url!!)
                            }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.OpenInNew,
                                stringResource(Res.string.action_open_source)
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Text(text = it.licenseContent ?: "")
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}