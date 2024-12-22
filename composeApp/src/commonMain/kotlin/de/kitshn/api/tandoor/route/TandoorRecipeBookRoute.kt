package de.kitshn.api.tandoor.route

import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.getArray
import de.kitshn.api.tandoor.model.TandoorRecipeBook
import de.kitshn.api.tandoor.model.TandoorRecipeBookEntry
import de.kitshn.api.tandoor.postObject
import de.kitshn.json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

class TandoorRecipeBookRoute(client: TandoorClient) : TandoorBaseRoute(client) {

    suspend fun create(
        name: String = "",
        description: String = "",
    ): TandoorRecipeBook {
        val data = buildJsonObject {
            put("name", JsonPrimitive(name))
            put("description", JsonPrimitive(description))
            put("order", JsonPrimitive(0))
            put("filter", JsonNull)
            put("shared", buildJsonArray { })
        }

        val recipeBook = TandoorRecipeBook.parse(
            this.client,
            client.postObject("/recipe-book/", data).toString()
        )

        recipeBook.populate(client = client)
        return recipeBook
    }

    suspend fun list(): List<TandoorRecipeBook> {
        val response = json.decodeFromString<List<TandoorRecipeBook>>(
            client.getArray("/recipe-book/").toString()
        )

        response.forEach { it.populate(client) }
        return response
    }

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