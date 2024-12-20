package de.kitshn.ui.layout

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun kitshnListDetailPaneScaffoldImpl(
    key: String,
    topBar: @Composable (colors: TopAppBarColors) -> Unit,
    floatingActionButton: @Composable () -> Unit,
    listContent: @Composable (pv: PaddingValues, selectedId: String?, supportsMultiplePanes: Boolean, background: Color, select: (id: String?) -> Unit) -> Unit,
    content: @Composable (id: String, supportsMultiplePanes: Boolean, expandDetailPane: Boolean, toggleExpandedDetailPane: () -> Unit, close: () -> Unit, back: (() -> Unit)?) -> Unit
): Boolean {
    return false
}