package de.kitshn.cache

import coil3.PlatformContext
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.TandoorKeyword
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class KeywordNameIdMapCache(
    context: PlatformContext,
    client: TandoorClient
) : BaseCache("KEYWORD_NAME_ID_MAP", context, client) {

    suspend fun update(coroutineScope: CoroutineScope) {
        var res = client.keyword.list(pageSize = 500)

        apply(res.results)
        delay(100)

        if(res.next != null) coroutineScope.launch {
            var page = 1
            val keywords = mutableListOf<TandoorKeyword>()

            while(res.next != null) {
                page++

                res = client.keyword.list(page = page, pageSize = 500)
                keywords.addAll(res.results)
            }

            apply(keywords)
        }
    }

    fun apply(keywords: List<TandoorKeyword>) {
        keywords.forEach {
            settings.putInt("keyword_${it.name.lowercase().hashCode().hashCode()}", it.id)
        }

        validUntil(Clock.System.now().toEpochMilliseconds() + 259200000 /* 3 days in millis */)
    }

    fun retrieve(name: String) =
        settings.getInt("keyword_${name.lowercase().hashCode()}", -1).takeIf { it != -1 }

}