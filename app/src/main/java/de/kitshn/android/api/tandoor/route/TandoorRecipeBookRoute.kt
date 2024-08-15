package de.kitshn.android.api.tandoor.route

import de.kitshn.android.api.tandoor.TandoorClient
import de.kitshn.android.api.tandoor.TandoorRequestsError
import de.kitshn.android.api.tandoor.getArray
import de.kitshn.android.api.tandoor.model.TandoorRecipeBook
import de.kitshn.android.api.tandoor.model.TandoorRecipeBookEntry
import de.kitshn.android.api.tandoor.postObject
import de.kitshn.android.json
import org.json.JSONArray
import org.json.JSONObject

class TandoorRecipeBookRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    @Throws(TandoorRequestsError::class)
    suspend fun create(
        name: String = "",
        description: String = "",
    ): TandoorRecipeBook {
        val data = JSONObject().apply {
            put("name", name)
            put("description", description)
            put("order", 0)
            put("filter", null)
            put("shared", JSONArray())
        }

        val recipeBook = TandoorRecipeBook.parse(
            this.client,
            client.postObject("/recipe-book/", data).toString()
        )

        recipeBook.populate(client = client)
        return recipeBook
    }

    @Throws(TandoorRequestsError::class)
    suspend fun list(): List<TandoorRecipeBook> {
        val response = json.decodeFromString<List<TandoorRecipeBook>>(
            client.getArray("/recipe-book/").toString()
        )

        response.forEach { it.populate(client) }
        return response
    }

    @Throws(TandoorRequestsError::class)
    suspend fun listEntries(bookId: Int): List<TandoorRecipeBookEntry> {
        val response = json.decodeFromString<List<TandoorRecipeBookEntry>>(
            client.getArray("/recipe-book-entry/?book=$bookId").toString()
        )

        client.container.recipeBook[bookId]?.apply {
            entryByRecipeId.clear()
            entries.clear()

            response.forEach { it.populate(this, client) }
        }

        return response
    }

}