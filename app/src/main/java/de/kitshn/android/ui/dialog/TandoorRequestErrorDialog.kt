package de.kitshn.android.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.kitshn.android.R
import de.kitshn.android.api.tandoor.TandoorRequestState
import de.kitshn.android.api.tandoor.TandoorRequestStateState
import org.acra.ktx.sendWithAcra

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
        text = {
            Column {
                Text(stringResource(R.string.error_request_description))

                if(state.error != null) {
                    Spacer(Modifier.height(16.dp))

                    Surface(
                        Modifier.heightIn(max = 160.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ) {
                        LazyColumn(
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if(state.error?.volleyError?.message != null) item {
                                Text(
                                    text = state.error?.volleyError?.message ?: ""
                                )
                            }

                            if(state.error?.volleyError != null) item {
                                Text(
                                    text = state.error?.volleyError?.stackTraceToString() ?: ""
                                )
                            }
                        }
                    }
                }
            }
        },
        dismissButton = {
            IconButton(
                onClick = {
                    state.error?.sendWithAcra()
                },
            ) {
                Icon(
                    imageVector = Icons.Rounded.Share,
                    contentDescription = stringResource(R.string.action_share),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
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