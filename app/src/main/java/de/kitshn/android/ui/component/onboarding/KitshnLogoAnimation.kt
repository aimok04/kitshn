package de.kitshn.android.ui.component.onboarding

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.kitshn.android.R

@Composable
fun KitshnLogoAnimation(
    onCompleted: () -> Unit = { }
) {
    KitshnLogoAnimationWrapper(
        onCompleted
    ) { modifier, tint ->
        Icon(
            modifier = modifier
                .height(100.dp)
                .width(100.dp),
            painter = painterResource(id = R.drawable.ic_logo),
            contentDescription = stringResource(id = R.string.app_name),
            tint = tint
        )
    }
}