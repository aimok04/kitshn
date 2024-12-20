package de.kitshn.ui.component.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.StarHalf
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.common_review
import org.jetbrains.compose.resources.stringResource

@Composable
fun StarIcon(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    half: Boolean = false
) {
    Icon(
        modifier = modifier,
        imageVector = if(enabled) if(half) Icons.AutoMirrored.Rounded.StarHalf else Icons.Rounded.Star else Icons.Rounded.StarBorder,
        contentDescription = stringResource(Res.string.common_review)
    )
}