package de.kitshn.api.tandoor.route

import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.TandoorPagedResponse
import de.kitshn.api.tandoor.model.TandoorRecipeBook
import de.kitshn.api.tandoor.model.TandoorRecipeBookEntry
import de.kitshn.api.tandoor.postObject
import de.kitshn.json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement

private const val PATH = "recipe-book/"
private const val ENTRY_PATH = "recipe-book-entry/"

class TandoorRecipeBookRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    suspend fun create(
        name: String = "",
        description: String = "",
        shared: List<TandoorUser> = listOf()
    ): TandoorRecipeBook {
        val data = buildJsonObject {
            put("name", JsonPrimitive(name))
            put("description", JsonPrimitive(description))
            put("order", JsonPrimitive(0))
            put("filter", JsonNull)
            put("shared", json.encodeToJsonElement(shared))
        }

        val recipeBook = TandoorRecipeBook.parse(
            this.client,
            client.postObject("/${PATH}", data).toString()
        )

        recipeBook.populate(client = client)
        return recipeBook
    }

    suspend fun list(
        query: String? = null,
        page: Int = 1,
        pageSize: Int? = null
    ): TandoorPagedResponse<TandoorRecipeBook> {
        val response = listPage<TandoorRecipeBook>(
            path = PATH,
            page = page,
            pageSize = pageSize,
            query = query,
            extraParams = listOf(
                // TODO visit this again once implemented the repo
                "order_direction" to "asc",
                "order_field" to "id",
            )
        )

        response.results.forEach { it.populate(client) }

        return response
    }

    suspend fun listAll(
        query: String? = null,
        onPageReceived: (suspend (List<TandoorRecipeBook>) -> Boolean)? = null,
    ): TandoorPagedResponse<TandoorRecipeBook> = listAllPages<TandoorRecipeBook>(
        path = PATH,
        pageSize = 50,
        query = query
    ) { page ->
        page.forEach { it.populate(client) }
        onPageReceived?.invoke(page) ?: false
    }

    suspend fun listEntries(
        bookId: Int?,
        recipeId: Int? = null,
        page: Int = 1,
        pageSize: Int? = null
    ): TandoorPagedResponse<TandoorRecipeBookEntry> {
        val extraParams = buildList {
            if (bookId != null) add("book" to bookId.toString())
            if (recipeId != null) add("recipe" to recipeId.toString())
        }

        val response = listPage<TandoorRecipeBookEntry>(
            path = ENTRY_PATH,
            page = page,
            pageSize = pageSize,
            extraParams = extraParams
        )

        client.container.recipeBook[bookId]?.apply {
            response.results.forEach { it.populate(this, client) }
        }

        return response
    }

    suspend fun listAllEntries(
        bookId: Int
    ): List<TandoorRecipeBookEntry> {
        val response = listAllPages<TandoorRecipeBookEntry>(
            path = ENTRY_PATH,
            pageSize = 50,
            extraParams = listOf("book" to bookId.toString())
        ) { page ->
            client.container.recipeBook[bookId]?.let { book ->
                page.forEach { it.populate(book, client) }
            }
            false
        }

        val entries = response.results

        client.container.recipeBook[bookId]?.let { book ->
            book.entries.clear()

            entries.forEach {
                book.entries.add(it)
            }
        }

        return entries
    }

}