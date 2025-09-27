@file:OptIn(ExperimentalTime::class)

package de.kitshn.cache

import coil3.PlatformContext
import com.russhwolf.settings.Settings
import de.kitshn.api.tandoor.TandoorClient
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

open class BaseCache(
    val id: String,
    platformContext: PlatformContext,
    val client: TandoorClient
) {

    val settings = createCache(platformContext, id)

    fun validUntil(time: Long) {
        settings.putLong("VALID_UNTIL", time)
    }

    fun isValid() = Clock.System.now().toEpochMilliseconds() < settings.getLong("VALID_UNTIL", 0L)

}

expect fun createCache(context: PlatformContext, id: String): Settings