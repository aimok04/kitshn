package de.kitshn.ui.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ViewCarousel
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorRequestStateState
import de.kitshn.api.tandoor.model.TandoorScrapedSpace
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.ui.TandoorRequestErrorHandler
import de.kitshn.ui.component.alert.LoadingErrorAlertPaneWrapper
import de.kitshn.ui.component.icons.IconWithState
import de.kitshn.ui.component.icons.IconWithStateState
import de.kitshn.ui.modifier.loadingPlaceHolder
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_switch_space
import kitshn.composeapp.generated.resources.common_select
import kitshn.composeapp.generated.resources.common_selected
import kitshn.composeapp.generated.resources.lorem_ipsum_short
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource


@Composable
fun SpaceSwitchDialog(
    client: TandoorClient?,
    onRefresh: () -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val scrapeRequestState = rememberTandoorRequestState()
    val switchRequestState = rememberTandoorRequestState()

    val spaces = remember { mutableStateListOf<TandoorScrapedSpace>() }
    var newActiveSpace by remember { mutableIntStateOf(-1) }

    suspend fun update() {
        scrapeRequestState.wrapRequest {
            client!!.space.retrieveSpaces().let {
                spaces.clear()
                spaces.addAll(it)
            }
        }
    }

    LaunchedEffect(client) {
        if(client == null) return@LaunchedEffect
        update()
    }

    AlertDialog(
        modifier = Modifier.padding(top = 24.dp, bottom = 24.dp),
        onDismissRequest = {
            onDismiss()
        },
        icon = {
            Icon(Icons.Outlined.ViewCarousel, stringResource(Res.string.action_switch_space))
        },
        title = {
            Text(stringResource(Res.string.action_switch_space))
        },
        text = {
            LoadingErrorAlertPaneWrapper(
                modifier = Modifier.padding(16.dp),
                alertPaneModifier = Modifier.fillMaxWidth(),
                loadingState = scrapeRequestState.state.toErrorLoadingSuccessState()
            ) {
                LazyColumn(
                    Modifier
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    if(scrapeRequestState.state == TandoorRequestStateState.LOADING && spaces.isEmpty()) {
                        items(2) {
                            ListItem(
                                headlineContent = {
                                    Text(
                                        text = stringResource(Res.string.lorem_ipsum_short),
                                        Modifier.loadingPlaceHolder(scrapeRequestState.state.toErrorLoadingSuccessState())
                                    )
                                }
                            )
                        }
                    } else {
                        items(spaces.size, key = { spaces[it].id }) {
                            val space = spaces[it]

                            ListItem(
                                modifier = Modifier.clickable {
                                    coroutineScope.launch {
                                        switchRequestState.wrapRequest {
                                            newActiveSpace = space.id
                                            client!!.space.switch(space.id)
                                            update()

                                            onRefresh()
                                            onDismiss()
                                        }
                                    }
                                },
                                headlineContent = {
                                    Text(text = space.name)
                                },
                                trailingContent = {
                                    IconWithState(
                                        imageVector = if(space.active) Icons.Rounded.Check else Icons.Rounded.RadioButtonUnchecked,
                                        contentDescription = if(space.active) stringResource(Res.string.common_selected) else stringResource(
                                            Res.string.common_select
                                        ),
                                        state = if(newActiveSpace == space.id) switchRequestState.state.toIconWithState() else IconWithStateState.DEFAULT
                                    )
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { }
    )

    TandoorRequestErrorHandler(switchRequestState)
}