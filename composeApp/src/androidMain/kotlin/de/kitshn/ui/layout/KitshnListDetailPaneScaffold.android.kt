package de.kitshn.ui.layout

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.kitshn.ui.state.rememberForeverListDetailPaneScaffoldNavigation
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.app_name
import kitshn.composeapp.generated.resources.ic_logo
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
actual fun kitshnListDetailPaneScaffoldImpl(
    key: String,
    topBar: @Composable (colors: TopAppBarColors) -> Unit,
    floatingActionButton: @Composable () -> Unit,
    listContent: @Composable (pv: PaddingValues, selectedId: String?, supportsMultiplePanes: Boolean, background: Color, select: (id: String?) -> Unit) -> Unit,
    content: @Composable (id: String, supportsMultiplePanes: Boolean, expandDetailPane: Boolean, toggleExpandedDetailPane: () -> Unit, close: () -> Unit, back: (() -> Unit)?) -> Unit
): Boolean {
    val coroutineScope = rememberCoroutineScope()

    val navigator = rememberForeverListDetailPaneScaffoldNavigation<String>(
        key = key,
        scaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo()).copy(
            verticalPartitionSpacerSize = 0.dp,
            horizontalPartitionSpacerSize = 0.dp
        )
    )

    var supportsMultiplePanes by remember { mutableStateOf(false) }
    LaunchedEffect(navigator.scaffoldValue) {
        supportsMultiplePanes =
            (navigator.scaffoldValue.primary == PaneAdaptedValue.Expanded) && (navigator.scaffoldValue.secondary == PaneAdaptedValue.Expanded)
    }

    var currentSelection by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(navigator.currentDestination) {
        if(navigator.currentDestination == null) return@LaunchedEffect
        currentSelection = navigator.currentDestination!!.content
    }

    var expandDetailPane by remember { mutableStateOf(false) }

    val listPaneAnim = remember { Animatable(1f) }
    val detailPaneAnim = remember { Animatable(1f) }
    BackHandler(navigator.canNavigateBack()) {
        if(expandDetailPane) {
            expandDetailPane = false
            return@BackHandler
        }

        if(!supportsMultiplePanes) {
            coroutineScope.launch {
                listPaneAnim.snapTo(1f)
                detailPaneAnim.animateTo(0f, tween(200))

                navigator.navigateBack()
            }
        } else {
            navigator.navigateBack()
        }
    }

    BackHandler(supportsMultiplePanes && navigator.currentDestination?.content != null) {
        navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, null)
    }

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            if(supportsMultiplePanes && expandDetailPane) return@ListDetailPaneScaffold

            val background = if(supportsMultiplePanes) {
                MaterialTheme.colorScheme.surfaceContainer
            } else {
                MaterialTheme.colorScheme.background
            }

            AnimatedPane(
                modifier = Modifier
                    .preferredWidth(550.dp)
                    .background(background)
            ) {
                Scaffold(
                    modifier = Modifier.alpha(if(supportsMultiplePanes) 1f else listPaneAnim.value),
                    topBar = {
                        topBar(
                            TopAppBarDefaults.topAppBarColors(
                                containerColor = if(supportsMultiplePanes) {
                                    MaterialTheme.colorScheme.surfaceContainer
                                } else {
                                    Color.Unspecified
                                },
                                scrolledContainerColor = if(supportsMultiplePanes) {
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                                } else {
                                    Color.Unspecified
                                }
                            )
                        )
                    },
                    floatingActionButton = floatingActionButton,
                    containerColor = if(supportsMultiplePanes) {
                        MaterialTheme.colorScheme.surfaceContainer
                    } else {
                        MaterialTheme.colorScheme.background
                    }
                ) {
                    listContent(it, currentSelection, supportsMultiplePanes, background) { id ->
                        if(!supportsMultiplePanes) {
                            coroutineScope.launch {
                                detailPaneAnim.snapTo(1f)
                                listPaneAnim.animateTo(0f, tween(200))

                                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, id)
                            }
                        } else {
                            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, id)
                        }
                    }
                }
            }
        },
        detailPane = {
            if(supportsMultiplePanes && currentSelection == null) return@ListDetailPaneScaffold

            AnimatedPane(
                Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .preferredWidth(500.dp)
            ) {
                if(currentSelection != null) {
                    Column(
                        Modifier
                            .alpha(if(supportsMultiplePanes) 1f else detailPaneAnim.value)
                            .fillMaxWidth()
                    ) {
                        content(
                            currentSelection!!, supportsMultiplePanes, expandDetailPane, {
                                expandDetailPane = !expandDetailPane
                            }, {
                                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, null)
                            },
                            (if(supportsMultiplePanes) null else {
                                {
                                    coroutineScope.launch {
                                        listPaneAnim.snapTo(1f)
                                        detailPaneAnim.animateTo(0f, tween(200))

                                        navigator.navigateBack()
                                    }
                                }
                            })
                        )
                    }
                } else {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            modifier = Modifier
                                .height(64.dp)
                                .width(64.dp)
                                .alpha(0.3f),
                            painter = painterResource(Res.drawable.ic_logo),
                            contentDescription = stringResource(Res.string.app_name)
                        )
                    }
                }
            }
        }
    )

    return true
}