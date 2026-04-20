package de.kitshn.ui.state

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldDefaults
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldAdaptStrategies
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldDestinationItem
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private val SaveMap = mutableMapOf<String, ThreePaneScaffoldDestinationItem<*>?>()

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun <T> rememberForeverListDetailPaneScaffoldNavigation(
    key: String,
    scaffoldDirective: PaneScaffoldDirective =
        calculatePaneScaffoldDirective(currentWindowAdaptiveInfo()),
    adaptStrategies: ThreePaneScaffoldAdaptStrategies =
        ListDetailPaneScaffoldDefaults.adaptStrategies(),
    isDestinationHistoryAware: Boolean = true,
    initialDestinationHistory: List<ThreePaneScaffoldDestinationItem<T>>? = null
): ThreePaneScaffoldNavigator<T> {
    val savedInitialDestinationHistory = remember {
        mutableListOf<ThreePaneScaffoldDestinationItem<T>>().apply {
            add(ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.List))
            SaveMap[key]?.let { add(it as ThreePaneScaffoldDestinationItem<T>) }
        }
    }

    val navigator = rememberListDetailPaneScaffoldNavigator(
        scaffoldDirective = scaffoldDirective,
        adaptStrategies = adaptStrategies,
        isDestinationHistoryAware = isDestinationHistoryAware,
        initialDestinationHistory = initialDestinationHistory ?: savedInitialDestinationHistory
    )

    DisposableEffect(Unit) {
        onDispose {
            SaveMap[key] = navigator.currentDestination
        }
    }
    return navigator
}