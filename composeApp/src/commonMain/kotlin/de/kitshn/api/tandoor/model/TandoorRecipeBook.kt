package de.kitshn.api.tandoor.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import coil3.request.ImageRequest
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.delete
import de.kitshn.api.tandoor.model.recipe.TandoorRecipeOverview
import de.kitshn.api.tandoor.patchObject
import de.kitshn.api.tandoor.postObject
import de.kitshn.api.tandoor.route.TandoorRecipeQueryParameters
import de.kitshn.api.tandoor.route.TandoorRecipeRouteListResponse
import de.kitshn.json
import de.kitshn.removeIf
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

@Serializable
class TandoorRecipeBook(
    val id: Int,
    val name: String,
    val description: String,
    val order: Int = 0,
    val filter: TandoorRecipeFilter? = null
) {

    @Transient
    private var client: TandoorClient? = null

    @Transient
    val entries = mutableStateListOf<TandoorRecipeBookEntry>()

    @Transient
    val filterEntries = mutableStateListOf<TandoorRecipeOverview>()

    @Transient
    val entryByRecipeId = mutableStateMapOf<Int, TandoorRecipeBookEntry>()

    @Composable
    fun loadThumbnail(): ImageRequest? {
        if(entries.size == 0)
            return filterEntries.firstOrNull { (it.image ?: "").isNotBlank() }?.loadThumbnail()

        return entries.firstOrNull { (it.recipe_content.image ?: "").isNotBlank() }?.loadThumbnail()
    }

    suspend fun listEntries(): List<TandoorRecipeBookEntry>? {
        if(client == null) return null
        return client!!.recipeBook.listEntries(id)
    }

    suspend fun listFilterEntries(
        page: Int
    ): TandoorRecipeRouteListResponse {
        val recipes = client!!.recipe.list(
            page = page,
            parameters = TandoorRecipeQueryParameters(
                filter = filter!!.id
            )
        )

        filterEntries.clear()
        filterEntries.addAll(recipes.results)

        return recipes
    }

    suspend fun delete(): String {
        client?.container?.recipeBook?.remove(id)
        return client?.delete("/recipe-book/${id}/")?.status?.value?.toString() ?: "unknown"
    }

    suspend fun partialUpdate(
        name: String? = null,
        description: String? = null
    ) {
        if(this.client == null) return

        val data = buildJsonObject {
            if(name != null) put("name", JsonPrimitive(name))
            if(description != null) put("description", JsonPrimitive(description))
        }

        client!!.patchObject("/recipe-book/${id}/", data)
    }

    suspend fun createEntry(recipeId: Int): TandoorRecipeBookEntry {
        val data = buildJsonObject {
            put("book", JsonPrimitive(id))
            put("recipe", JsonPrimitive(recipeId))
            put("order", JsonPrimitive(0))
        }

        val response = json.decodeFromString<TandoorRecipeBookEntry>(
            client!!.postObject("/recipe-book-entry/", data).toString()
        )

        response.populate(this, client)
        return response
    }

    companion object {
        fun parse(client: TandoorClient, data: String): TandoorRecipeBook {
            val obj = json.decodeFromString<TandoorRecipeBook>(data)
            obj.client = client
            return obj
        }
    }

    fun populate(client: TandoorClient) {
        this.client = client
        client.container.recipeBook[id] = this
    }

}

@Serializable
class TandoorRecipeBookEntry(
    val id: Int,
    val book: Int,
    var book_content: TandoorRecipeBook,
    val recipe: Int,
    var recipe_content: TandoorRecipeOverview
) {

    @Transient
    private var client: TandoorClient? = null

    @Composable
    fun loadThumbnail(): ImageRequest? {
        return recipe_content.loadThumbnail()
    }

    suspend fun delete(): String {
        // remove from entry lists
        client?.container?.recipeBook?.get(book)?.let {
            it.entries.removeIf { it.id == id }
            it.entryByRecipeId.remove(recipe)
        }

        return client?.delete("/recipe-book-entry/${id}/")?.status?.value?.toString() ?: "unknown"
    }

    fun populate(book: TandoorRecipeBook?, client: TandoorClient?) {
        this.client = client

        client?.container?.recipeOverview?.get(recipe)?.let { recipe_content = it }
        client?.container?.recipeBookEntry?.put(id, this)

        book?.apply {
            book_content = this

            entries.add(this@TandoorRecipeBookEntry)
            entryByRecipeId[recipe] = this@TandoorRecipeBookEntry
        }

        recipe_content.client = client
    }

}