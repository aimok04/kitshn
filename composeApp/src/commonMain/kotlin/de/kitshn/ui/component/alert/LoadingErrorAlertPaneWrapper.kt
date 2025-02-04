package de.kitshn.ui.component.alert

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.kitshn.ui.state.ErrorLoadingSuccessState
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.error_couldnt_connect_to_tandoor
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoadingErrorAlertPaneWrapper(
    pv: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier
        .padding(pv)
        .fillMaxSize(),
    alertPaneModifier: Modifier = Modifier.fillMaxSize(),
    loadingState: ErrorLoadingSuccessState,
    content: @Composable () -> Unit
) {
    if(loadingState == ErrorLoadingSuccessState.ERROR) {
        Box(modifier) {
            FullSizeAlertPane(
                modifier = alertPaneModifier,
                imageVector = Icons.Rounded.CloudOff,
                contentDescription = stringResource(Res.string.error_couldnt_connect_to_tandoor),
                text = stringResource(Res.string.error_couldnt_connect_to_tandoor)
            )
        }
    } else {
        content()
    }
}