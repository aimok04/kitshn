package de.kitshn.ui.component.onboarding

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.app_name
import kitshn.composeapp.generated.resources.ic_logo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

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
            painter = painterResource(Res.drawable.ic_logo),
            contentDescription = stringResource(Res.string.app_name),
            tint = tint
        )
    }
}