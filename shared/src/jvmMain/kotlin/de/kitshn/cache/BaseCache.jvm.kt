package de.kitshn.cache

import coil3.PlatformContext
import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings

actual fun createCache(context: PlatformContext, id: String): Settings {
    return PreferencesSettings.Factory().create(name = "CACHE_$id")
}