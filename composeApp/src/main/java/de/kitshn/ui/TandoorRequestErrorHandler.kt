package de.kitshn.ui

import androidx.compose.runtime.Composable
import de.kitshn.api.tandoor.TandoorRequestState
import de.kitshn.ui.dialog.TandoorRequestErrorDialog

@Composable
fun TandoorRequestErrorHandler(
    state: TandoorRequestState,
    onDismissDialog: () -> Unit = { }
) {
    TandoorRequestErrorDialog(
        state = state,
        onDismiss = onDismissDialog
    )
}