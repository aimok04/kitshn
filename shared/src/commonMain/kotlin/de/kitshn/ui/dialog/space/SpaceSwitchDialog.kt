package de.kitshn.ui.dialog.space

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ViewCarousel
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.TandoorRequestStateState
import de.kitshn.api.tandoor.model.TandoorSpace
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.handleTandoorRequestState
import de.kitshn.repo.SpaceRepo
import de.kitshn.ui.TandoorRequestErrorHandler
import de.kitshn.ui.component.alert.LoadingErrorAlertPaneWrapper
import de.kitshn.ui.modifier.loadingPlaceHolder
import de.kitshn.ui.state.ErrorLoadingSuccessState
import kitshn.shared.generated.resources.Res
import kitshn.shared.generated.resources.action_abort
import kitshn.shared.generated.resources.action_create_space
import kitshn.shared.generated.resources.action_switch_space
import kitshn.shared.generated.resources.common_user
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

data class SpaceSwitchDialogState(
    val spaces: List<TandoorSpace> = emptyList(),
    val activeSpaceId: Int? = null,
    val syncState: TandoorRequestStateState = TandoorRequestStateState.IDLE,
    val pendingSpaceId: Int? = null,
    val pendingState: TandoorRequestStateState = TandoorRequestStateState.IDLE,
    val memberCounts: Map<Int, Int> = emptyMap(),
)

sealed interface SpaceSwitchDialogEvent {
    data class Switch(val space: TandoorSpace) : SpaceSwitchDialogEvent
    data class Edit(val space: TandoorSpace) : SpaceSwitchDialogEvent
    data object Create : SpaceSwitchDialogEvent
    data object Dismiss : SpaceSwitchDialogEvent
}

/**
 * Stateful convenience wrapper around [SpaceSwitchDialog]
 */
@Composable
fun SpaceSwitchDialog(
    repo: SpaceRepo,
    onSwitched: (TandoorSpace) -> Unit,
    onDismiss: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current

    val syncRequestState = rememberTandoorRequestState()
    val switchRequestState = rememberTandoorRequestState()
    val saveRequestState = rememberTandoorRequestState()
    val deleteRequestState = rememberTandoorRequestState()

    val activeSpace by repo.current.collectAsState()
    val spaces by repo.spaces.collectAsState()
    val sortedSpaces = remember(spaces) { spaces.sortedBy { it.id } }
    val memberCounts = remember(spaces) { spaces.associate { it.id to it.user_count } }
    var pendingSpaceId by remember { mutableStateOf<Int?>(null) }

    var dialogState by remember { mutableStateOf(SpaceCreationAndEditDialogState()) }

    LaunchedEffect(repo) {
        syncRequestState.wrapRequest {
            repo.sync(force = true)
            repo.syncSpaces()
        }
    }

    val state = SpaceSwitchDialogState(
        spaces = sortedSpaces,
        activeSpaceId = activeSpace?.id,
        syncState = syncRequestState.state,
        pendingSpaceId = pendingSpaceId,
        pendingState = switchRequestState.state,
        memberCounts = memberCounts,
    )

    SpaceSwitchDialog(
        state = state,
        onEvent = { event ->
            when (event) {
                is SpaceSwitchDialogEvent.Switch -> {
                    if (activeSpace?.id == event.space.id) return@SpaceSwitchDialog

                    coroutineScope.launch {
                        pendingSpaceId = event.space.id
                        switchRequestState.wrapRequest {
                            repo.switch(event.space.id)
                            onSwitched(event.space)
                            onDismiss()
                        }

                        hapticFeedback.handleTandoorRequestState(switchRequestState)

                        if (switchRequestState.isError()) pendingSpaceId = null
                    }
                }

                is SpaceSwitchDialogEvent.Edit -> {
                    dialogState = SpaceCreationAndEditDialogState(
                        shown = true,
                        editing = event.space,
                        name = event.space.name,
                        message = event.space.message,
                    )
                }

                SpaceSwitchDialogEvent.Create -> {
                    dialogState = SpaceCreationAndEditDialogState(shown = true)
                }

                SpaceSwitchDialogEvent.Dismiss -> onDismiss()
            }
        }
    )

    val client = repo.client
    SpaceCreationAndEditDialog(
        state = dialogState.copy(
            saveState = saveRequestState.state,
            deleteState = deleteRequestState.state,
        ),
        currentImage = { space ->
            space.image?.preview?.let { client?.media?.createImageBuilder(it)?.build() }
        },
        onEvent = { event ->
            when (event) {
                is SpaceCreationAndEditDialogEvent.NameChange -> {
                    dialogState = dialogState.copy(name = event.name)
                }

                is SpaceCreationAndEditDialogEvent.MessageChange -> {
                    dialogState = dialogState.copy(message = event.message)
                }

                is SpaceCreationAndEditDialogEvent.ImageReplace -> {
                    dialogState = dialogState.copy(image = SpaceImageEdit.Replaced(event.bytes))
                }

                SpaceCreationAndEditDialogEvent.ImageReset -> {
                    dialogState = dialogState.copy(
                        image = when {
                            dialogState.image is SpaceImageEdit.Unchanged
                                && dialogState.editing?.image != null -> SpaceImageEdit.Cleared

                            else -> SpaceImageEdit.Unchanged
                        }
                    )
                }

                is SpaceCreationAndEditDialogEvent.Save -> {
                    if (saveRequestState.state == TandoorRequestStateState.LOADING) return@SpaceCreationAndEditDialog
                    val editing = dialogState.editing
                    coroutineScope.launch {
                        val saved = saveRequestState.wrapRequest {
                            if (editing != null) {
                                repo.update(editing.id, event.partial)
                            } else {
                                repo.create(event.partial)
                            }
                        }
                        hapticFeedback.handleTandoorRequestState(saveRequestState)

                        if (saved != null) {
                            // TODO: persist image edits once the upload route is wired up.
                            dialogState = SpaceCreationAndEditDialogState()
                        }
                    }
                }

                SpaceCreationAndEditDialogEvent.RequestDelete -> {
                    dialogState = dialogState.copy(deleteConfirmation = dialogState.editing)
                }

                SpaceCreationAndEditDialogEvent.CancelDelete -> {
                    dialogState = dialogState.copy(deleteConfirmation = null)
                }

                is SpaceCreationAndEditDialogEvent.ConfirmDelete -> {
                    if (deleteRequestState.state == TandoorRequestStateState.LOADING) return@SpaceCreationAndEditDialog
                    dialogState = dialogState.copy(deleteConfirmation = null)
                    coroutineScope.launch {
                        deleteRequestState.wrapRequest { repo.delete(event.space.id) }
                        hapticFeedback.handleTandoorRequestState(deleteRequestState)
                        dialogState = SpaceCreationAndEditDialogState()
                    }
                }

                SpaceCreationAndEditDialogEvent.Dismiss -> {
                    dialogState = SpaceCreationAndEditDialogState()
                }
            }
        },
    )

    TandoorRequestErrorHandler(switchRequestState)
    TandoorRequestErrorHandler(saveRequestState)
    TandoorRequestErrorHandler(deleteRequestState)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpaceSwitchDialog(
    state: SpaceSwitchDialogState,
    onEvent: (SpaceSwitchDialogEvent) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onEvent(SpaceSwitchDialogEvent.Dismiss) },
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
            SpaceSwitchDialogContent(state, onEvent)
        },
        confirmButton = {
            TextButton(onClick = { onEvent(SpaceSwitchDialogEvent.Dismiss) }) {
                Text(stringResource(Res.string.action_abort))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SpaceSwitchDialogContent(
    state: SpaceSwitchDialogState,
    onEvent: (SpaceSwitchDialogEvent) -> Unit,
) {
    val isLoading = state.syncState == TandoorRequestStateState.LOADING
    val motion = MaterialTheme.motionScheme

    LoadingErrorAlertPaneWrapper(
        loadingState = state.syncState.toErrorLoadingSuccessState()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
        ) {
            if (isLoading && state.spaces.isEmpty()) {
                val skeletons = 3
                items(skeletons, key = { "skeleton_$it" }) { idx ->
                    Box(
                        modifier = Modifier.animateItem(
                            fadeInSpec = motion.fastEffectsSpec(),
                            fadeOutSpec = motion.fastEffectsSpec(),
                            placementSpec = motion.fastSpatialSpec()
                        )
                    ) {
                        SpaceItemSkeleton(index = idx, count = skeletons)
                    }
                }
            } else {
                itemsIndexed(state.spaces, key = { _, it -> it.id }) { index, space ->
                    Box(
                        modifier = Modifier.animateItem(
                            fadeInSpec = tween(durationMillis = 200, delayMillis = index * 30),
                            fadeOutSpec = motion.fastEffectsSpec(),
                            placementSpec = motion.fastSpatialSpec()
                        )
                    ) {
                        SpaceItem(
                            name = space.name,
                            selected = space.id == state.activeSpaceId,
                            pendingState = if (state.pendingSpaceId == space.id) state.pendingState else null,
                            enabled = state.pendingSpaceId == null,
                            memberCount = state.memberCounts[space.id],
                            onClick = { onEvent(SpaceSwitchDialogEvent.Switch(space)) },
                            onEditClick = { onEvent(SpaceSwitchDialogEvent.Edit(space)) },
                            index = index,
                            count = state.spaces.size
                        )
                    }
                }
            }

            if (isLoading || state.spaces.isNotEmpty()) {
                item(key = "bottom_spacer") {
                    Spacer(
                        Modifier.height(16.dp)
                            .animateItem(placementSpec = motion.fastSpatialSpec())
                    )
                }
            }

            item(key = "create_space_button") {
                Box(modifier = Modifier.animateItem(placementSpec = motion.fastSpatialSpec())) {
                    SegmentedListItem(
                        onClick = { onEvent(SpaceSwitchDialogEvent.Create) },
                        shapes = ListItemDefaults.segmentedShapes(
                            0, 1, ListItemDefaults.shapes(shape = RoundedCornerShape(16.dp))
                        ),
                        content = {
                            Text(stringResource(Res.string.action_create_space))
                        },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = stringResource(Res.string.action_create_space)
                            )
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SpaceItemSkeleton(
    index: Int,
    count: Int,
) {
    SegmentedListItem(
        onClick = { },
        shapes = ListItemDefaults.segmentedShapes(
            index = index,
            count = count,
            defaultShapes = ListItemDefaults.shapes(
                shape = if (count == 1) RoundedCornerShape(16.dp) else null
            )
        ),
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .loadingPlaceHolder(
                        loadingState = ErrorLoadingSuccessState.LOADING,
                        shape = CircleShape
                    )
            )
        },
        content = {
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .fillMaxWidth()
                    .loadingPlaceHolder(ErrorLoadingSuccessState.LOADING)
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SpaceItem(
    index: Int,
    count: Int,
    name: String,
    selected: Boolean = false,
    pendingState: TandoorRequestStateState? = null,
    enabled: Boolean = true,
    memberCount: Int? = null,
    onEditClick: () -> Unit = {},
    onClick: () -> Unit = {},
) {
    var displayedPendingState by remember { mutableStateOf<TandoorRequestStateState?>(null) }
    LaunchedEffect(pendingState) {
        if (pendingState == TandoorRequestStateState.LOADING) {
            displayedPendingState = null
            delay(150)
            displayedPendingState = TandoorRequestStateState.LOADING
        } else {
            displayedPendingState = pendingState
        }
    }

    val statusBackgroundColor = when {
        displayedPendingState == null -> Color.Transparent
        selected -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainer
    }

    SegmentedListItem(
        selected = selected,
        enabled = enabled,
        onClick = onClick,
        onLongClick = onEditClick,
        shapes = ListItemDefaults.segmentedShapes(
            index = index,
            count = count,
            defaultShapes = ListItemDefaults.shapes(
                shape = if (count == 1) {
                    RoundedCornerShape(16.dp)
                } else {
                    null
                }
            )
        ),

        content = {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                ),
            )
        },

        leadingContent = {
            val motion = MaterialTheme.motionScheme

            Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                RadioButton(selected = selected, onClick = null)

                AnimatedContent(
                    targetState = displayedPendingState,
                    transitionSpec = {
                        val spec = motion.fastEffectsSpec<Float>()
                        (fadeIn(spec) + scaleIn(spec, initialScale = 0.8f))
                            .togetherWith(fadeOut(spec) + scaleOut(spec, targetScale = 0.8f))
                    },
                    label = "statusIndicator"
                ) { state ->
                    if (state != null) {
                        StatusOverlay(
                            backgroundColor = statusBackgroundColor
                        )
                    }
                }
            }
        },

        trailingContent = {
            AnimatedVisibility(
                visible = memberCount != null,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = memberCount?.toString() ?: "",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Icon(
                        imageVector = Icons.Rounded.AccountCircle,
                        contentDescription = stringResource(Res.string.common_user),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StatusOverlay(
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor, CircleShape)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        CircularWavyProgressIndicator(Modifier.size(24.dp))
    }
}
