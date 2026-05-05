package de.kitshn.ui.dialog

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ViewCarousel
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import kitshn.shared.generated.resources.action_abort
import kitshn.shared.generated.resources.action_switch_space
import kitshn.shared.generated.resources.common_select
import kitshn.shared.generated.resources.common_selected
import kitshn.shared.generated.resources.lorem_ipsum_short
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource


@OptIn(ExperimentalMaterial3Api::class)
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
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ViewCarousel,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                text = stringResource(Res.string.action_switch_space),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            val state = if (isLoading) TandoorRequestStateState.LOADING
            else TandoorRequestStateState.SUCCESS

            LoadingErrorAlertPaneWrapper(loadingState = state.toErrorLoadingSuccessState()) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isLoading && spaces.isEmpty()) {
                        items(3) { SpaceItem(name = null) }
                    } else {
                        itemsIndexed(spaces, key = { _, it -> it.id }) { index, space ->
                            SpaceItem(
                                name = space.name,
                                selected = space.id == activeSpaceId,
                                pendingState = if (pendingSpaceId == space.id) pendingState else null,
                                enabled = pendingSpaceId == null,
                                shape = groupedShape(index, spaces.size),
                                onClick = { onSwitch(space) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.action_abort))
            }
        }
    )
}

private fun groupedShape(index: Int, total: Int): Shape {
    val l = 24.dp
    val s = 8.dp
    return when {
        total == 1 -> RoundedCornerShape(l)
        index == 0 -> RoundedCornerShape(l, l, s, s)
        index == total - 1 -> RoundedCornerShape(s, s, l, l)
        else -> RoundedCornerShape(s)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpaceItem(
    name: String?,
    selected: Boolean = false,
    pendingState: TandoorRequestStateState? = null,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(16.dp),
    onClick: () -> Unit = {},
) {
    val isSkeleton = name == null

    val containerColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerHighest,
        tween(250), label = "containerColor"
    )

    val scale by animateFloatAsState(
        if (selected) 1f else 0.98f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "scale"
    )

    Surface(
        onClick = onClick,
        enabled = enabled && !isSkeleton,
        shape = shape,
        color = containerColor,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leading: radio button OR skeleton circle
            if (isSkeleton) {
                Box(
                    Modifier.size(20.dp).clip(CircleShape)
                        .loadingPlaceHolder(TandoorRequestStateState.LOADING.toErrorLoadingSuccessState())
                )
            } else {
                RadioButton(selected = selected, onClick = null)
            }

            Spacer(Modifier.width(16.dp))

            // Body: name OR skeleton bar
            if (isSkeleton) {
                Box(
                    Modifier.height(20.dp).fillMaxWidth(0.7f)
                        .clip(RoundedCornerShape(4.dp))
                        .loadingPlaceHolder(TandoorRequestStateState.LOADING.toErrorLoadingSuccessState())
                )
            } else {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                AnimatedContent(
                    targetState = pendingState,
                    transitionSpec = {
                        (fadeIn(tween(200)) + scaleIn(initialScale = 0.8f))
                            .togetherWith(fadeOut(tween(150)))
                    },
                    label = "statusIndicator"
                ) { state ->
                    Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                        if (state != null) {
                            IconWithState(
                                imageVector = Icons.Rounded.CheckCircle,
                                state = state.toIconWithState(),
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
    }
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
//            coroutineScope.launch {
//                pendingSpaceId = space.id
//                switchRequestState.wrapRequest {
//                    repo.switch(space.id)
//                    onSwitched()
//                    onDismiss()
//                }
//                hapticFeedback.handleTandoorRequestState(switchRequestState)
//            }
        },
        onDismiss = onDismiss,
    )

    TandoorRequestErrorHandler(switchRequestState)
}
