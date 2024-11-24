package de.kitshn.android.cache

import android.content.Context
import android.content.SharedPreferences
import de.kitshn.android.api.tandoor.TandoorClient

open class BaseCache(
    val id: String,
    val context: Context,
    val client: TandoorClient
) {

    val sp: SharedPreferences = context.getSharedPreferences("CACHE_$id", Context.MODE_PRIVATE)

    fun validUntil(time: Long) {
        sp.edit()
            .putLong("VALID_UNTIL", time)
            .apply()
    }

    fun isValid() = System.currentTimeMillis() < sp.getLong("VALID_UNTIL", 0L)

}