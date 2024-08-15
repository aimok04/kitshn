package de.kitshn.android.ui.modifier

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.placeholder
import com.google.accompanist.placeholder.shimmer
import de.kitshn.android.ui.state.ErrorLoadingSuccessState

@Composable
fun Modifier.loadingPlaceHolder(
    loadingState: ErrorLoadingSuccessState = ErrorLoadingSuccessState.LOADING
): Modifier {
    val backgroundColor: Color = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp)
    val highlightColor: Color = MaterialTheme.colorScheme.primaryContainer

    return this.placeholder(
        visible = loadingState == ErrorLoadingSuccessState.LOADING,
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp),
        highlight = PlaceholderHighlight.shimmer(
            highlightColor = highlightColor
        )
    )
}