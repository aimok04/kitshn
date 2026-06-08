package de.kitshn.ui.view.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CarCrash
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import de.kitshn.closeAppHandler
import de.kitshn.ui.component.buttons.BackButton
import de.kitshn.ui.component.settings.SettingsListItem
import de.kitshn.ui.component.settings.SettingsListItemPosition
import de.kitshn.ui.view.ViewParameters

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ViewSettingsDebug(
    p: ViewParameters
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(state = rememberTopAppBarState())
    var showResetConfirm by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val closeApp = closeAppHandler()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = { BackButton(p.back) },
                title = { Text(text = "Debug settings") },
                scrollBehavior = scrollBehavior
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(it)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
        ) {
            item {
                SettingsListItem(
                    position = SettingsListItemPosition.TOP,
                    label = { Text("Test crash reporting") },
                    description = { Text("Simulate app crash") },
                    icon = Icons.Rounded.CarCrash,
                    contentDescription = "Test crash reporting",
                ) {
                    null!!
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
            }

            item {
                SettingsListItem(
                    position = SettingsListItemPosition.TOP,
                    label = { Text("Reset local database") },
                    description = { Text("Clear all cached data and re-sync from server") },
                    icon = Icons.Rounded.DeleteSweep,
                    contentDescription = "Reset local database",
                ) {
                    showResetConfirm = true
                }
            }

            item {
                SettingsListItem(
                    position = SettingsListItemPosition.BOTTOM,
                    label = { Text("Delete database file") },
                    description = { Text("Delete the database file and close the app. Reopening starts fresh.") },
                    icon = Icons.Rounded.DeleteForever,
                    contentDescription = "Delete database file",
                ) {
                    showDeleteConfirm = true
                }
            }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            icon = { Icon(Icons.Rounded.DeleteSweep, null) },
            title = { Text("Reset local database?") },
            text = {
                Text(
                    "Clears all cached repos and triggers a fresh sync from the server. " +
                        "Pending offline changes will be lost."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showResetConfirm = false
                    p.vm.resetLocalDatabase()
                }) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text("Cancel") }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = { Icon(Icons.Rounded.DeleteForever, null) },
            title = { Text("Delete database file?") },
            text = {
                Text(
                    "Deletes the database file and closes the app. " +
                        "The next launch starts with a completely empty database."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    p.vm.deleteLocalDatabase()
                    closeApp()
                }) { Text("Delete & close") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}
