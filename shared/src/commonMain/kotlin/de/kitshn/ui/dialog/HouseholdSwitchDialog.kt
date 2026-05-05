package de.kitshn.ui.dialog

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import de.kitshn.api.tandoor.TandoorRequestStateState
import de.kitshn.api.tandoor.model.TandoorHousehold
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.handleTandoorRequestState
import de.kitshn.repo.HouseholdRepo
import de.kitshn.ui.TandoorRequestErrorHandler
import de.kitshn.ui.component.alert.LoadingErrorAlertPaneWrapper
import de.kitshn.ui.component.icons.IconWithState
import de.kitshn.ui.modifier.loadingPlaceHolder
import de.kitshn.ui.state.ErrorLoadingSuccessState
import kitshn.shared.generated.resources.Res
import kitshn.shared.generated.resources.action_abort
import kitshn.shared.generated.resources.action_create_household
import kitshn.shared.generated.resources.action_edit_household
import kitshn.shared.generated.resources.action_switch_household
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

data class HouseholdSwitchState(
    val households: List<TandoorHousehold> = emptyList(),
    val activeHouseholdId: Int? = null,
    val syncState: TandoorRequestStateState = TandoorRequestStateState.IDLE,
    val pendingHouseholdId: Int? = null,
    val pendingState: TandoorRequestStateState = TandoorRequestStateState.IDLE,
)

sealed interface HouseholdSwitchEvent {
    data class Switch(val household: TandoorHousehold) : HouseholdSwitchEvent
    data class Edit(val household: TandoorHousehold) : HouseholdSwitchEvent
    data object Create : HouseholdSwitchEvent
    data object Dismiss : HouseholdSwitchEvent
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseholdSwitchPicker(
    state: HouseholdSwitchState,
    onEvent: (HouseholdSwitchEvent) -> Unit,
) {

    val isCompact = !currentWindowAdaptiveInfoV2().windowSizeClass.isWidthAtLeastBreakpoint(
        WIDTH_DP_MEDIUM_LOWER_BOUND
    )

    if (isCompact) {
        ModalBottomSheet(
            onDismissRequest = { onEvent(HouseholdSwitchEvent.Dismiss) },
        ) {
            Column {
                Text(
                    text = stringResource(Res.string.action_switch_household),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(PaddingValues(16.dp, 0.dp))
                )

                HouseholdSwitchContent(state = state, onEvent = onEvent)
            }
        }
    } else {
        AlertDialog(
            onDismissRequest = { onEvent(HouseholdSwitchEvent.Dismiss) },
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
                HouseholdSwitchContent(state = state, onEvent = onEvent)
            },
            confirmButton = {
                TextButton(onClick = { onEvent(HouseholdSwitchEvent.Dismiss) }) {
                    Text(stringResource(Res.string.action_abort))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HouseholdSwitchContent(
    state: HouseholdSwitchState,
    onEvent: (HouseholdSwitchEvent) -> Unit,
) {
    val isLoading = state.syncState == TandoorRequestStateState.LOADING
    val motion = MaterialTheme.motionScheme

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
                            placementSpec = motion.fastEffectsSpec()
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
                            onClick = { onEvent(HouseholdSwitchEvent.Switch(household)) },
                            onEditClick = { onEvent(HouseholdSwitchEvent.Edit(household)) },
                            index = index,
                            count = state.households.size
                        )
                    }
                }
            }

            if (isLoading || state.households.isNotEmpty()) {
                item(key = "bottom_spacer") {
                    Spacer(
                        Modifier.height(16.dp).animateItem(placementSpec = motion.fastSpatialSpec())
                    )
                }
            }

            item(key = "create_household_button") {
                Box(modifier = Modifier.animateItem(placementSpec = motion.fastSpatialSpec())) {
                    SegmentedListItem(
                        onClick = { onEvent(HouseholdSwitchEvent.Create) },

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
    onLongClick: () -> Unit = {},
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
        onLongClick = onLongClick,
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
            }        },
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

/**
 * Stateful convenience wrapper around [SpaceSwitchDialog]
 */
@Composable
fun HouseholdSwitchPicker(
    repo: HouseholdRepo,
    onSwitched: (TandoorHousehold) -> Unit,
    onDismiss: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current

    val syncRequestState = rememberTandoorRequestState()
    val switchRequestState = rememberTandoorRequestState()

    val activeHousehold by repo.current.collectAsState(null)
    val households by repo.households.collectAsState()
    val sortedHouseholds = remember(households){ households.sortedBy { it.id } }
    var pendingHouseholdId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(repo) {
        repo.sync(force = true)
    }

    val state = HouseholdSwitchState(
        households = sortedHouseholds,
        activeHouseholdId = activeHousehold?.id,
        syncState = syncRequestState.state,
        pendingHouseholdId = pendingHouseholdId,
        pendingState = switchRequestState.state,
    )

    HouseholdSwitchPicker(
        state = state,
        onEvent = { event ->
            when (event) {
                is HouseholdSwitchEvent.Switch -> {
                    if (activeHousehold?.id == event.household.id) return@HouseholdSwitchPicker
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

                is HouseholdSwitchEvent.Edit -> {
                    // TODO: launch edit flow
                }

                HouseholdSwitchEvent.Create -> {
                    // TODO: launch create flow
                }

                HouseholdSwitchEvent.Dismiss -> onDismiss()
            }
        },
    )

    TandoorRequestErrorHandler(switchRequestState)
}
