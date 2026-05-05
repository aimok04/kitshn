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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.TandoorRequestStateState
import de.kitshn.api.tandoor.model.TandoorSpace
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.handleTandoorRequestState
import de.kitshn.repo.SpaceRepo
import de.kitshn.ui.TandoorRequestErrorHandler
import de.kitshn.ui.component.alert.LoadingErrorAlertPaneWrapper
import de.kitshn.ui.component.icons.IconWithState
import de.kitshn.ui.component.icons.IconWithStateState
import de.kitshn.ui.modifier.loadingPlaceHolder
import kitshn.shared.generated.resources.Res
import kitshn.shared.generated.resources.action_switch_space
import kitshn.shared.generated.resources.common_select
import kitshn.shared.generated.resources.common_selected
import kitshn.shared.generated.resources.lorem_ipsum_short
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/**
 * Stateless SpaceSwitchDialog emits intents [onSwitch] and [onDismiss]
 */
@Composable
fun SpaceSwitchDialog(
    spaces: List<TandoorSpace>,
    activeSpaceId: Int?,
    isLoading: Boolean,
    pendingSpaceId: Int?,
    pendingState: TandoorRequestStateState,
    onSwitch: (TandoorSpace) -> Unit,
    onDismiss: () -> Unit,
) {
    val loadingState = if(isLoading) TandoorRequestStateState.LOADING
    else TandoorRequestStateState.SUCCESS

    AlertDialog(
        modifier = Modifier.padding(top = 24.dp, bottom = 24.dp),
        onDismissRequest = onDismiss,
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
                loadingState = loadingState.toErrorLoadingSuccessState()
            ) {
                LazyColumn(
                    Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .fillMaxWidth()
                ) {
                    if(isLoading && spaces.isEmpty()) {
                        items(2) {
                            ListItem(
                                headlineContent = {
                                    Text(
                                        text = stringResource(Res.string.lorem_ipsum_short),
                                        Modifier.loadingPlaceHolder(loadingState.toErrorLoadingSuccessState())
                                    )
                                }
                            )
                        }
                    } else {
                        items(spaces.size, key = { spaces[it].id }) {
                            val space = spaces[it]
                            val isActive = space.id == activeSpaceId

                            ListItem(
                                modifier = Modifier.clickable { onSwitch(space) },
                                headlineContent = {
                                    Text(text = space.name)
                                },
                                trailingContent = {
                                    IconWithState(
                                        imageVector = if(isActive) Icons.Rounded.Check else Icons.Rounded.RadioButtonUnchecked,
                                        contentDescription = stringResource(
                                            if(isActive) Res.string.common_selected else Res.string.common_select
                                        ),
                                        state = if(pendingSpaceId == space.id) pendingState.toIconWithState() else IconWithStateState.DEFAULT
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
}

/**
 * Stateful convenience wrapper around [SpaceSwitchDialog]
 */
@Composable
fun SpaceSwitchDialog(
    repo: SpaceRepo,
    onSwitched: () -> Unit,
    onDismiss: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current

    val syncRequestState = rememberTandoorRequestState()
    val switchRequestState = rememberTandoorRequestState()

    val activeSpace by repo.current.collectAsState(null)
    val spaces = remember { mutableStateListOf<TandoorSpace>() }
    var pendingSpaceId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(repo) {
        syncRequestState.wrapRequest {
            repo.sync(force = true)
            spaces.clear()
            spaces.addAll(repo.spaces(forceRefresh = true))
        }
    }

    SpaceSwitchDialog(
        spaces = spaces,
        activeSpaceId = activeSpace?.id,
        isLoading = syncRequestState.state == TandoorRequestStateState.LOADING,
        pendingSpaceId = pendingSpaceId,
        pendingState = switchRequestState.state,
        onSwitch = { space ->
            coroutineScope.launch {
                pendingSpaceId = space.id
                switchRequestState.wrapRequest {
                    repo.switch(space.id)
                    onSwitched()
                    onDismiss()
                }
                hapticFeedback.handleTandoorRequestState(switchRequestState)
            }
        },
        onDismiss = onDismiss,
    )

    TandoorRequestErrorHandler(switchRequestState)
}
