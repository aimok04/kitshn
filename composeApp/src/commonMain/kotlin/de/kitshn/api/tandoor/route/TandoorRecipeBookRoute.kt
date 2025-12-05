package de.kitshn.api.tandoor.route

import com.eygraber.uri.Uri
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.getObject
import de.kitshn.api.tandoor.model.TandoorPagedResponse
import de.kitshn.api.tandoor.model.TandoorRecipeBook
import de.kitshn.api.tandoor.model.TandoorRecipeBookEntry
import de.kitshn.api.tandoor.postObject
import de.kitshn.json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement

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
            client.postObject("/recipe-book/", data).toString()
        )

        recipeBook.populate(client = client)
        return recipeBook
    }

    suspend fun list(
        query: String? = null,
        page: Int = 1,
        pageSize: Int? = null
    ): TandoorPagedResponse<TandoorRecipeBook> {
        val builder = Uri.Builder().appendEncodedPath("recipe-book/")
        if(pageSize != null) builder.appendQueryParameter("page_size", pageSize.toString())
        if(query != null) builder.appendQueryParameter("query", query)
        builder.appendQueryParameter("page", page.toString())

        val response = json.decodeFromString<TandoorPagedResponse<TandoorRecipeBook>>(
            client.getObject(builder.build().toString()).toString()
        )

        response.results.forEach { it.populate(client) }
        return response
    }

    suspend fun listAll(): List<TandoorRecipeBook> {
        var page = 1
        val entries = mutableListOf<TandoorRecipeBook>()

        var response: TandoorPagedResponse<TandoorRecipeBook>? = null
        while(response == null || response.next != null) {
            response = list(
                page = page,
                pageSize = 50
            )

            entries.addAll(response.results)
            page++
        }

        entries.forEach { it.populate(client) }
        return entries
    }

    private suspend fun listEntries(
        bookId: Int,
        page: Int = 1,
        pageSize: Int? = null
    ): TandoorPagedResponse<TandoorRecipeBookEntry> {
        val builder = Uri.Builder().appendEncodedPath("recipe-book-entry/")
        if(pageSize != null) builder.appendQueryParameter("page_size", pageSize.toString())
        builder.appendQueryParameter("page", page.toString())
        builder.appendQueryParameter("book", bookId.toString())

        val response = json.decodeFromString<TandoorPagedResponse<TandoorRecipeBookEntry>>(
            client.getObject(builder.build().toString()).toString()
        )

        client.container.recipeBook[bookId]?.apply {
            response.results.forEach { it.populate(this, client) }
        }

        return response
    }

    suspend fun listAllEntries(
        bookId: Int
    ): List<TandoorRecipeBookEntry> {
        var page = 1
        val entries = mutableListOf<TandoorRecipeBookEntry>()

        var response: TandoorPagedResponse<TandoorRecipeBookEntry>? = null
        while(response == null || response.next != null) {
            response = listEntries(
                bookId = bookId,
                page = page,
                pageSize = 50
            )

            entries.addAll(response.results)
            page++
        }

        client.container.recipeBook[bookId]?.let { book ->
            book.entries.clear()
            book.entryByRecipeId.clear()

            entries.forEach {
                book.entries.add(it)
                book.entryByRecipeId[it.recipe] = it
            }
        }

        return entries
    }

}