package de.kitshn.ui.component.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.ui.dialog.SpaceSwitchDialog
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_switch_space
import org.jetbrains.compose.resources.stringResource

@Composable
fun SpaceSwitchIconButton(
    client: TandoorClient?,
    onRefresh: () -> Unit
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }

    IconButton(
        onClick = { showDialog = true }
    ) {
        Icon(Icons.Rounded.SwapHoriz, stringResource(Res.string.action_switch_space))
    }

    if(showDialog) SpaceSwitchDialog(
        client = client,
        onRefresh = onRefresh,
        onDismiss = {
            showDialog = false
        }
    )
}