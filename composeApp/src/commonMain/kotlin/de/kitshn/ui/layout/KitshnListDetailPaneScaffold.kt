package de.kitshn.ui.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
expect fun kitshnListDetailPaneScaffoldImpl(
    key: String,
    topBar: @Composable (colors: TopAppBarColors) -> Unit,
    floatingActionButton: @Composable () -> Unit = {},
    listContent: @Composable (pv: PaddingValues, selectedId: String?, supportsMultiplePanes: Boolean, background: Color, select: (id: String?) -> Unit) -> Unit,
    content: @Composable (id: String, supportsMultiplePanes: Boolean, expandDetailPane: Boolean, toggleExpandedDetailPane: () -> Unit, close: () -> Unit, back: (() -> Unit)?) -> Unit
): Boolean

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitshnListDetailPaneScaffold(
    key: String,
    topBar: @Composable (colors: TopAppBarColors) -> Unit,
    floatingActionButton: @Composable () -> Unit = {},
    listContent: @Composable (pv: PaddingValues, selectedId: String?, supportsMultiplePanes: Boolean, background: Color, select: (id: String?) -> Unit) -> Unit,
    content: @Composable (id: String, supportsMultiplePanes: Boolean, expandDetailPane: Boolean, toggleExpandedDetailPane: () -> Unit, close: () -> Unit, back: (() -> Unit)?) -> Unit
) {
    if(!kitshnListDetailPaneScaffoldImpl(
        key = key,
        topBar = topBar,
        floatingActionButton = floatingActionButton,
        listContent =listContent,
        content = content
    )) {
        // display alternative content

        var currentSelection by rememberSaveable { mutableStateOf<String?>(null) }
        var expandDetailPane by remember { mutableStateOf(false) }

        if(currentSelection != null) {
            Column(
                Modifier
                    .fillMaxWidth()
            ) {
                content(
                    currentSelection!!, false, expandDetailPane, {
                        expandDetailPane = !expandDetailPane
                    }, {
                        currentSelection = null
                    },
                    {
                        currentSelection = null
                    }
                )
            }
        }else{
            Scaffold(
                topBar = {
                    topBar(
                        TopAppBarDefaults.topAppBarColors()
                    )
                },
                floatingActionButton = floatingActionButton
            ) {
                listContent(it, currentSelection, false, MaterialTheme.colorScheme.background) { id ->
                    currentSelection = id
                }
            }
        }
    }
}