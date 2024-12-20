package de.kitshn.cache

import android.content.Context
import coil3.PlatformContext
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

actual fun createCache(context: PlatformContext, id: String): Settings {
    val delegate = context.getSharedPreferences("CACHE_$id", Context.MODE_PRIVATE)
    return SharedPreferencesSettings(delegate)
}