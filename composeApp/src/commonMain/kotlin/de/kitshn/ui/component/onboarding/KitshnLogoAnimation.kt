package de.kitshn.ui.component.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import de.kitshn.ui.randomBackgroundShape
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.app_name
import kitshn.composeapp.generated.resources.ic_logo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun KitshnLogoAnimation(
    onCompleted: () -> Unit = { }
) {
    KitshnLogoAnimationWrapper(
        onCompleted = onCompleted
    ) { modifier ->
        Box(
            modifier
                .size(164.dp)
                .background(
                    LoadingIndicatorDefaults.containedContainerColor,
                    remember {
                        MaterialShapes.randomBackgroundShape()
                    }.toShape(0)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = modifier
                    .height(100.dp)
                    .width(100.dp),
                painter = painterResource(Res.drawable.ic_logo),
                contentDescription = stringResource(Res.string.app_name),
                tint = LoadingIndicatorDefaults.containedIndicatorColor
            )
        }
    }
}