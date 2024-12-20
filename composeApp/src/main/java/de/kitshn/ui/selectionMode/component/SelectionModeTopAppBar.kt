package de.kitshn.ui.selectionMode.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.kitshn.R
import de.kitshn.ui.selectionMode.SelectionModeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SelectionModeTopAppBar(
    topAppBar: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    state: SelectionModeState<T>
) {
    if(!state.isSelectionModeEnabledState()) {
        topAppBar()
    } else {
        val containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        val contentColor = MaterialTheme.colorScheme.onSurface

        TopAppBar(
            navigationIcon = {
                IconButton(onClick = {
                    state.disable()
                }) {
                    Icon(Icons.Rounded.Close, stringResource(id = R.string.action_close))
                }
            },
            title = {
                Text(state.selectedItems.size.toString())
            },
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = containerColor,
                titleContentColor = contentColor,
                navigationIconContentColor = contentColor,
                actionIconContentColor = contentColor
            )
        )
    }
}