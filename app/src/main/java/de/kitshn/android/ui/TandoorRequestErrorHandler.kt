package de.kitshn.android.ui

import androidx.compose.runtime.Composable
import de.kitshn.android.api.tandoor.TandoorRequestState
import de.kitshn.android.ui.dialog.TandoorRequestErrorDialog

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