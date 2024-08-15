package de.kitshn.android.ui.component.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.StarHalf
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.kitshn.android.R

@Composable
fun StarIcon(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    half: Boolean = false
) {
    Icon(
        modifier = modifier,
        imageVector = if(enabled) if(half) Icons.AutoMirrored.Rounded.StarHalf else Icons.Rounded.Star else Icons.Rounded.StarBorder,
        contentDescription = stringResource(R.string.common_review)
    )
}