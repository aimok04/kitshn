package de.kitshn.ui.component.alert

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.kitshn.R
import de.kitshn.ui.state.ErrorLoadingSuccessState

@Composable
fun LoadingErrorAlertPaneWrapper(
    pv: PaddingValues = PaddingValues(0.dp),
    loadingState: ErrorLoadingSuccessState,
    content: @Composable () -> Unit
) {
    if(loadingState == ErrorLoadingSuccessState.ERROR) {
        Box(
            Modifier
                .padding(pv)
                .fillMaxSize()
        ) {
            FullSizeAlertPane(
                imageVector = Icons.Rounded.CloudOff,
                contentDescription = stringResource(R.string.error_couldnt_connect_to_tandoor),
                text = stringResource(R.string.error_couldnt_connect_to_tandoor)
            )
        }
    } else {
        content()
    }
}