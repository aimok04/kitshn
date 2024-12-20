package de.kitshn.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import de.kitshn.ui.state.ErrorLoadingSuccessState

@Composable
fun LoadingGradientWrapper(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    loadingState: ErrorLoadingSuccessState = ErrorLoadingSuccessState.LOADING,
    errorReplacement: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Box(
        modifier.fillMaxSize()
    ) {
        if(loadingState == ErrorLoadingSuccessState.ERROR && errorReplacement != null) {
            errorReplacement()
        } else {
            content()
        }

        if(loadingState == ErrorLoadingSuccessState.LOADING) Box(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .align(Alignment.BottomStart)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            backgroundColor
                        )
                    )
                )
        )
    }
}