package de.kitshn.ui.theme.custom

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import de.kitshn.ui.theme.backgroundDark
import de.kitshn.ui.theme.backgroundLight
import de.kitshn.ui.theme.errorContainerDark
import de.kitshn.ui.theme.errorContainerLight
import de.kitshn.ui.theme.errorDark
import de.kitshn.ui.theme.errorLight
import de.kitshn.ui.theme.inverseOnSurfaceDark
import de.kitshn.ui.theme.inverseOnSurfaceLight
import de.kitshn.ui.theme.inversePrimaryDark
import de.kitshn.ui.theme.inversePrimaryLight
import de.kitshn.ui.theme.inverseSurfaceDark
import de.kitshn.ui.theme.inverseSurfaceLight
import de.kitshn.ui.theme.onBackgroundDark
import de.kitshn.ui.theme.onBackgroundLight
import de.kitshn.ui.theme.onErrorContainerDark
import de.kitshn.ui.theme.onErrorContainerLight
import de.kitshn.ui.theme.onErrorDark
import de.kitshn.ui.theme.onErrorLight
import de.kitshn.ui.theme.onPrimaryContainerDark
import de.kitshn.ui.theme.onPrimaryContainerLight
import de.kitshn.ui.theme.onPrimaryDark
import de.kitshn.ui.theme.onPrimaryLight
import de.kitshn.ui.theme.onSecondaryContainerDark
import de.kitshn.ui.theme.onSecondaryContainerLight
import de.kitshn.ui.theme.onSecondaryDark
import de.kitshn.ui.theme.onSecondaryLight
import de.kitshn.ui.theme.onSurfaceDark
import de.kitshn.ui.theme.onSurfaceLight
import de.kitshn.ui.theme.onSurfaceVariantDark
import de.kitshn.ui.theme.onSurfaceVariantLight
import de.kitshn.ui.theme.onTertiaryContainerDark
import de.kitshn.ui.theme.onTertiaryContainerLight
import de.kitshn.ui.theme.onTertiaryDark
import de.kitshn.ui.theme.onTertiaryLight
import de.kitshn.ui.theme.outlineDark
import de.kitshn.ui.theme.outlineLight
import de.kitshn.ui.theme.outlineVariantDark
import de.kitshn.ui.theme.outlineVariantLight
import de.kitshn.ui.theme.primaryContainerDark
import de.kitshn.ui.theme.primaryContainerLight
import de.kitshn.ui.theme.primaryDark
import de.kitshn.ui.theme.primaryLight
import de.kitshn.ui.theme.scrimDark
import de.kitshn.ui.theme.scrimLight
import de.kitshn.ui.theme.secondaryContainerDark
import de.kitshn.ui.theme.secondaryContainerLight
import de.kitshn.ui.theme.secondaryDark
import de.kitshn.ui.theme.secondaryLight
import de.kitshn.ui.theme.surfaceBrightDark
import de.kitshn.ui.theme.surfaceBrightLight
import de.kitshn.ui.theme.surfaceContainerDark
import de.kitshn.ui.theme.surfaceContainerHighDark
import de.kitshn.ui.theme.surfaceContainerHighLight
import de.kitshn.ui.theme.surfaceContainerHighestDark
import de.kitshn.ui.theme.surfaceContainerHighestLight
import de.kitshn.ui.theme.surfaceContainerLight
import de.kitshn.ui.theme.surfaceContainerLowDark
import de.kitshn.ui.theme.surfaceContainerLowLight
import de.kitshn.ui.theme.surfaceContainerLowestDark
import de.kitshn.ui.theme.surfaceContainerLowestLight
import de.kitshn.ui.theme.surfaceDark
import de.kitshn.ui.theme.surfaceDimDark
import de.kitshn.ui.theme.surfaceDimLight
import de.kitshn.ui.theme.surfaceLight
import de.kitshn.ui.theme.surfaceTintDark
import de.kitshn.ui.theme.surfaceTintLight
import de.kitshn.ui.theme.surfaceVariantDark
import de.kitshn.ui.theme.surfaceVariantLight
import de.kitshn.ui.theme.tertiaryContainerDark
import de.kitshn.ui.theme.tertiaryContainerLight
import de.kitshn.ui.theme.tertiaryDark
import de.kitshn.ui.theme.tertiaryLight

val defaultLightColorScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    surfaceTint = surfaceTintLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

val defaultDarkColorScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    surfaceTint = surfaceTintDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)