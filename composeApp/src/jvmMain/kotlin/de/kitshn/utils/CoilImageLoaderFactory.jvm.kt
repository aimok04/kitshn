package de.kitshn.utils

import coil3.ImageLoader
import coil3.PlatformContext
import de.kitshn.api.tandoor.TandoorCredentials

actual fun createImageLoader(
    context: PlatformContext,
    credentials: TandoorCredentials
): ImageLoader {
    // JVM/Desktop: Default ImageLoader (no client certificate support yet)
    return ImageLoader.Builder(context).build()
}
