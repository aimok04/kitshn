package de.kitshn.utils

import coil3.ImageLoader
import coil3.PlatformContext
import de.kitshn.api.tandoor.TandoorCredentials

expect fun createImageLoader(
    context: PlatformContext,
    credentials: TandoorCredentials
): ImageLoader
