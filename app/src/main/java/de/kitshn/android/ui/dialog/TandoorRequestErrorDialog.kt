package de.kitshn.android.ui.dialog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import de.kitshn.android.R
import de.kitshn.android.api.tandoor.TandoorRequestState
import de.kitshn.android.api.tandoor.TandoorRequestStateState

@Composable
fun TandoorRequestErrorDialog(
    state: TandoorRequestState,
    title: String = stringResource(R.string.error_request),
    onDismiss: () -> Unit = { }
) {
    var shown by remember { mutableStateOf(false) }

    LaunchedEffect(state.state) {
        if(state.state != TandoorRequestStateState.ERROR) return@LaunchedEffect
        shown = true
    }

    if(!shown) return

    AlertDialog(
        onDismissRequest = {
            shown = false
            onDismiss()
        },
        icon = {
            Icon(
                imageVector = Icons.Rounded.ErrorOutline,
                contentDescription = stringResource(R.string.error),
                tint = MaterialTheme.colorScheme.error
            )
        },
        containerColor = MaterialTheme.colorScheme.errorContainer,
        iconContentColor = MaterialTheme.colorScheme.error,
        titleContentColor = MaterialTheme.colorScheme.error,
        textContentColor = MaterialTheme.colorScheme.onErrorContainer,
        title = { Text(title) },
        text = { Text(stringResource(R.string.error_request_description)) },
        confirmButton = {
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                onClick = {
                    shown = false
                    onDismiss()
                }
            ) {
                Text(stringResource(id = R.string.common_okay))
            }
        }
    )
}