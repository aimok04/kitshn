package de.kitshn.android.api.tandoor.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import coil.request.ImageRequest
import de.kitshn.android.api.tandoor.TandoorClient
import de.kitshn.android.api.tandoor.TandoorRequestsError
import de.kitshn.android.api.tandoor.delete
import de.kitshn.android.api.tandoor.model.recipe.TandoorRecipeOverview
import de.kitshn.android.api.tandoor.patchObject
import de.kitshn.android.api.tandoor.postObject
import de.kitshn.android.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.json.JSONObject

@Serializable
class TandoorRecipeBook(
    val id: Int,
    val name: String,
    val description: String,
    val order: Int
) {

    @Transient
    private var client: TandoorClient? = null

    @Transient
    val entries = mutableStateListOf<TandoorRecipeBookEntry>()

    @Transient
    val entryByRecipeId = mutableStateMapOf<Int, TandoorRecipeBookEntry>()

    @Composable
    fun loadThumbnail(): ImageRequest? {
        if(entries.size == 0) return null
        return entries[0].loadThumbnail()
    }

    @Throws(TandoorRequestsError::class)
    suspend fun listEntries(): List<TandoorRecipeBookEntry>? {
        if(client == null) return null
        return client!!.recipeBook.listEntries(id)
    }

    @Throws(TandoorRequestsError::class)
    suspend fun delete(): String {
        client?.container?.recipeBook?.remove(id)
        return client?.delete("/recipe-book/${id}/") ?: "unknown"
    }

    @Throws(TandoorRequestsError::class)
    suspend fun partialUpdate(
        name: String? = null,
        description: String? = null
    ) {
        if(this.client == null) return

        val data = JSONObject().apply {
            if(name != null) put("name", name)
            if(description != null) put("description", description)
        }

        client!!.patchObject("/recipe-book/${id}/", data)
    }

    @Throws(TandoorRequestsError::class)
    suspend fun createEntry(recipeId: Int): TandoorRecipeBookEntry {
        val data = JSONObject().apply {
            put("book", id)
            put("recipe", recipeId)
            put("order", 0)
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

    @Throws(TandoorRequestsError::class)
    suspend fun delete(): String {
        // remove from entry lists
        client?.container?.recipeBook?.get(book)?.let {
            it.entries.removeIf { entry -> entry.id == id }
            it.entryByRecipeId.remove(recipe)
        }

        return client?.delete("/recipe-book-entry/${id}/") ?: "unknown"
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