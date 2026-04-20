package de.kitshn.cache

import coil3.PlatformContext
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings

actual fun createCache(context: PlatformContext, id: String): Settings {
    return NSUserDefaultsSettings.Factory().create(name = "CACHE_$id")
}