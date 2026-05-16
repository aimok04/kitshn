package de.kitshn.api.tandoor.route

import com.eygraber.uri.Uri
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.getObject
import de.kitshn.api.tandoor.model.TandoorPagedResponse
import de.kitshn.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

/** Base for typed routes; shares the paginated-list machinery. */
abstract class TandoorBaseRoute(
    open val client: TandoorClient
) {
    /** Fetches a single page. Endpoint-specific filters go in [extraParams]. */
    protected suspend inline fun <reified T> listPage(
        path: String,
        page: Int = 1,
        pageSize: Int? = null,
        query: String? = null,
        extraParams: Iterable<Pair<String, String?>> = emptyList(),
    ): TandoorPagedResponse<T> = withContext(Dispatchers.IO) {
        val builder = Uri.Builder().appendEncodedPath(path.trimStart('/'))
        builder.appendQueryParameter("page", page.toString())
        if (pageSize != null) builder.appendQueryParameter("page_size", pageSize.toString())
        if (query != null) builder.appendQueryParameter("query", query)
        extraParams.forEach { (k, v) -> if (v != null) builder.appendQueryParameter(k, v) }

        json.decodeFromString<TandoorPagedResponse<T>>(client.getObject(builder.build().toString()).toString())
    }

    /** Walks every page, aggregating results; return `true` from [onPageReceived] to stop early. */
    protected suspend inline fun <reified T> listAllPages(
        path: String,
        pageSize: Int = 200,
        query: String? = null,
        extraParams: Iterable<Pair<String, String?>> = emptyList(),
        crossinline onPageReceived: suspend (List<T>) -> Boolean,
    ): TandoorPagedResponse<T> = withContext(Dispatchers.IO) {
        var page = 1
        val all = mutableListOf<T>()
        var first: TandoorPagedResponse<T>? = null
        while (true) {
            val resp = listPage<T>(path, page, pageSize, query, extraParams)
            if (first == null) first = resp
            all.addAll(resp.results)
            val stop = onPageReceived(resp.results)
            if (stop || resp.next == null) break
            page++
        }
        first.copy(results = all, next = null, count = all.size)
    }
}
