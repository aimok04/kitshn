package de.kitshn.android.ui.component.buttons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.kitshn.android.R

enum class BackButtonType {
    DEFAULT,
    CLOSE
}

@Composable
fun BackButton(
    onBack: (() -> Unit)?,
    overlay: Boolean = false,
    type: BackButtonType = BackButtonType.DEFAULT
) {
    if(onBack == null) return

    @Composable
    fun Icon() {
        when(type) {
            BackButtonType.DEFAULT ->
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.action_back))

            BackButtonType.CLOSE ->
                Icon(Icons.Rounded.Close, stringResource(R.string.action_close))
        }
    }

    if(overlay) {
        FilledIconButton(
            onClick = onBack,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) { Icon() }
        return
    }

    IconButton(onClick = onBack) { Icon() }
}