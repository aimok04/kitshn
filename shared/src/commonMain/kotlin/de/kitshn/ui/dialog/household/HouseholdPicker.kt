package de.kitshn.ui.dialog.household

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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
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
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_MEDIUM_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import de.kitshn.api.tandoor.TandoorRequestStateState
import de.kitshn.api.tandoor.model.TandoorHousehold
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.handleTandoorRequestState
import de.kitshn.repo.HouseholdRepo
import de.kitshn.ui.TandoorRequestErrorHandler
import de.kitshn.ui.component.alert.LoadingErrorAlertPaneWrapper
import de.kitshn.ui.modifier.loadingPlaceHolder
import de.kitshn.ui.state.ErrorLoadingSuccessState
import kitshn.shared.generated.resources.Res
import kitshn.shared.generated.resources.action_abort
import kitshn.shared.generated.resources.action_close
import kitshn.shared.generated.resources.action_create_household
import kitshn.shared.generated.resources.action_switch_household
import kitshn.shared.generated.resources.common_user
import kitshn.shared.generated.resources.error_household_delete_blocked_by_user
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

data class HouseholdPickerState(
    val households: List<TandoorHousehold> = emptyList(),
    val activeHouseholdId: Int? = null,
    val syncState: TandoorRequestStateState = TandoorRequestStateState.IDLE,
    val pendingHouseholdId: Int? = null,
    val pendingState: TandoorRequestStateState = TandoorRequestStateState.IDLE,
    val memberCounts: Map<Int, Int> = emptyMap(),
    val errorMessage: String? = null,
)

sealed interface HouseholdPickerEvent {
    data class Switch(val household: TandoorHousehold) : HouseholdPickerEvent
    data class Edit(val household: TandoorHousehold) : HouseholdPickerEvent
    data object Create : HouseholdPickerEvent
    data object DismissError : HouseholdPickerEvent
    data object Dismiss : HouseholdPickerEvent
}

/**
 * Stateful convenience wrapper around [de.kitshn.ui.dialog.space.SpaceSwitchDialog]
 */
@Composable
fun HouseholdPicker(
    repo: HouseholdRepo,
    onSwitched: (TandoorHousehold) -> Unit,
    onDismiss: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current

    val syncRequestState = rememberTandoorRequestState()
    val switchRequestState = rememberTandoorRequestState()
    val saveRequestState = rememberTandoorRequestState()
    val deleteRequestState = rememberTandoorRequestState()

    val activeHousehold by repo.current.collectAsState(null)
    val households by repo.households.collectAsState()
    val members by repo.members.collectAsState()
    val sortedHouseholds = remember(households) { households.sortedBy { it.id } }
    val memberCounts = remember(members) { members.mapValues { it.value.size } }
    var pendingHouseholdId by remember { mutableStateOf<Int?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var dialogState by remember { mutableStateOf(HouseholdCreationAndEditDialogState()) }

    LaunchedEffect(repo) {
        repo.sync(force = true)
        repo.syncMembers()
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            delay(3999)
            errorMessage = null
        }
    }

    val state = HouseholdPickerState(
        households = sortedHouseholds,
        activeHouseholdId = activeHousehold?.id,
        syncState = syncRequestState.state,
        pendingHouseholdId = pendingHouseholdId,
        pendingState = switchRequestState.state,
        memberCounts = memberCounts,
        errorMessage = errorMessage,
    )

    HouseholdPicker(
        state = state,
        onEvent = { event ->
            when (event) {
                is HouseholdPickerEvent.Switch -> {
                    if (activeHousehold?.id == event.household.id) return@HouseholdPicker
                    coroutineScope.launch {
                        pendingHouseholdId = event.household.id
                        switchRequestState.wrapRequest {
                            repo.switch(event.household.id)
                            onSwitched(event.household)
                            onDismiss()
                        }
                        hapticFeedback.handleTandoorRequestState(switchRequestState)

                        if (switchRequestState.isError()) pendingHouseholdId = null
                    }
                }

                is HouseholdPickerEvent.Edit -> {
                    dialogState = HouseholdCreationAndEditDialogState(
                        shown = true,
                        editing = event.household,
                        name = event.household.name,
                    )
                }

                HouseholdPickerEvent.Create -> {
                    dialogState = HouseholdCreationAndEditDialogState(shown = true)
                }

                HouseholdPickerEvent.DismissError -> {
                    errorMessage = null
                }

                HouseholdPickerEvent.Dismiss -> onDismiss()
            }
        },
    )

    HouseholdCreationAndEditDialog(
        state = dialogState.copy(
            saveState = saveRequestState.state,
            deleteState = deleteRequestState.state,
        ),
        onEvent = { event ->
            when (event) {
                is HouseholdCreationAndEditDialogEvent.NameChange -> {
                    dialogState = dialogState.copy(name = event.name)
                }

                is HouseholdCreationAndEditDialogEvent.Save -> {
                    if (saveRequestState.state == TandoorRequestStateState.LOADING) return@HouseholdCreationAndEditDialog
                    val editing = dialogState.editing
                    coroutineScope.launch {
                        val saved = saveRequestState.wrapRequest {
                            if (editing != null) {
                                repo.rename(editing.id, event.household.name)
                            } else {
                                repo.create(event.household.name)
                            }
                        }
                        hapticFeedback.handleTandoorRequestState(saveRequestState)

                        if (saved != null) {
                            dialogState = HouseholdCreationAndEditDialogState()
                        }

                        repo.syncMembers()
                    }
                }

                HouseholdCreationAndEditDialogEvent.RequestDelete -> {
                    dialogState = dialogState.copy(deleteConfirmation = dialogState.editing)
                }

                HouseholdCreationAndEditDialogEvent.CancelDelete -> {
                    dialogState = dialogState.copy(deleteConfirmation = null)
                }

                is HouseholdCreationAndEditDialogEvent.ConfirmDelete -> {
                    if (deleteRequestState.state == TandoorRequestStateState.LOADING) return@HouseholdCreationAndEditDialog
                    dialogState = dialogState.copy(deleteConfirmation = null)
                    coroutineScope.launch {
                        val deleted = deleteRequestState.wrapRequest {
                            repo.delete(event.household.id)
                        } == true
                        hapticFeedback.handleTandoorRequestState(deleteRequestState)

                        dialogState = HouseholdCreationAndEditDialogState()
                        if (!deleted) {
                            val blockingMember = repo.members.value[event.household.id]
                                ?.firstOrNull()
                            if (blockingMember != null) {
                                errorMessage = getString(
                                    Res.string.error_household_delete_blocked_by_user,
                                    blockingMember.display_name.ifBlank { blockingMember.username }
                                )
                            }
                        }
                    }
                }

                HouseholdCreationAndEditDialogEvent.Dismiss -> {
                    dialogState = HouseholdCreationAndEditDialogState()
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
fun HouseholdPicker(
    state: HouseholdPickerState,
    onEvent: (HouseholdPickerEvent) -> Unit,
) {

    val isCompact = !currentWindowAdaptiveInfoV2().windowSizeClass.isAtLeastBreakpoint(
        WIDTH_DP_MEDIUM_LOWER_BOUND,
        HEIGHT_DP_MEDIUM_LOWER_BOUND,
    )

    if (isCompact) {
        ModalBottomSheet(
            onDismissRequest = { onEvent(HouseholdPickerEvent.Dismiss) },
        ) {
            Column {
                Text(
                    text = stringResource(Res.string.action_switch_household),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(PaddingValues(16.dp, 0.dp))
                )

                HouseholdPickerContent(state = state, onEvent = onEvent)
            }
        }
    } else {
        AlertDialog(
            onDismissRequest = { onEvent(HouseholdPickerEvent.Dismiss) },
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
                    text = stringResource(Res.string.action_switch_household),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                HouseholdPickerContent(state = state, onEvent = onEvent)
            },
            confirmButton = {
                TextButton(onClick = { onEvent(HouseholdPickerEvent.Dismiss) }) {
                    Text(stringResource(Res.string.action_abort))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HouseholdPickerContent(
    state: HouseholdPickerState,
    onEvent: (HouseholdPickerEvent) -> Unit,
) {
    val isLoading = state.syncState == TandoorRequestStateState.LOADING
    val motion = MaterialTheme.motionScheme

    Column(modifier = Modifier.fillMaxWidth()) {
        AnimatedVisibility(visible = state.errorMessage != null) {
            Snackbar(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                action = {
                    TextButton(onClick = { onEvent(HouseholdPickerEvent.DismissError) }) {
                        Text(stringResource(Res.string.action_close))
                    }
                }
            ) {
                Text(state.errorMessage ?: "")
            }
        }

        LoadingErrorAlertPaneWrapper(
            modifier = Modifier.fillMaxWidth(),
            alertPaneModifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
            loadingState = state.syncState.toErrorLoadingSuccessState()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
            ) {
                if (isLoading && state.households.isEmpty()) {
                    val skeletonCount = 3
                    items(skeletonCount, key = { "skeleton_$it" }) { index ->
                        Box(
                            modifier = Modifier.animateItem(
                                fadeInSpec = motion.fastEffectsSpec(),
                                fadeOutSpec = motion.fastEffectsSpec(),
                                placementSpec = motion.fastSpatialSpec()
                            )
                        ) {
                            HouseholdItemSkeleton(index = index, count = skeletonCount)
                        }
                    }
                } else {
                    itemsIndexed(state.households, key = { _, it -> it.id }) { index, household ->
                        Box(
                            modifier = Modifier.animateItem(
                                fadeInSpec = tween(durationMillis = 200, delayMillis = index * 30),
                                fadeOutSpec = motion.fastEffectsSpec(),
                                placementSpec = motion.fastSpatialSpec(),
                            )
                        ) {
                            HouseholdItem(
                                name = household.name,
                                selected = household.id == state.activeHouseholdId,
                                pendingState = if (state.pendingHouseholdId == household.id) state.pendingState else null,
                                enabled = state.pendingHouseholdId == null,
                                memberCount = state.memberCounts[household.id],
                                onClick = { onEvent(HouseholdPickerEvent.Switch(household)) },
                                onEditClick = { onEvent(HouseholdPickerEvent.Edit(household)) },
                                index = index,
                                count = state.households.size
                            )
                        }
                    }
                }

                if (isLoading || state.households.isNotEmpty()) {
                    item(key = "bottom_spacer") {
                        Spacer(
                            Modifier.height(16.dp)
                                .animateItem(placementSpec = motion.fastSpatialSpec())
                        )
                    }
                }

                item(key = "create_household_button") {
                    Box(modifier = Modifier.animateItem(placementSpec = motion.fastSpatialSpec())) {
                        SegmentedListItem(
                            onClick = { onEvent(HouseholdPickerEvent.Create) },

                            shapes = ListItemDefaults.segmentedShapes(
                                0, 1, ListItemDefaults.shapes(shape = RoundedCornerShape(16.dp))
                            ),

                            content = {
                                Text(stringResource(Res.string.action_create_household))
                            },

                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = stringResource(Res.string.action_create_household)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HouseholdItemSkeleton(
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
private fun HouseholdItem(
    index: Int,
    count: Int,
    name: String,
    selected: Boolean = false,
    enabled: Boolean = true,
    pendingState: TandoorRequestStateState? = null,
    memberCount: Int? = null,
    onClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
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
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                )
            )
        },

        leadingContent = {
            val motion = MaterialTheme.motionScheme

            Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                // Base layer: The RadioButton
                RadioButton(selected = selected, onClick = null)

                // Overlay layer: Status Indicators
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

