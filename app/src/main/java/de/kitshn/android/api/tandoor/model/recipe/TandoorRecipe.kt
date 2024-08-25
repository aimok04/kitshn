package de.kitshn.android.api.tandoor.model.recipe

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import coil.request.ImageRequest
import de.kitshn.android.JsonAsStringSerializer
import de.kitshn.android.api.tandoor.TandoorClient
import de.kitshn.android.api.tandoor.TandoorRequestsError
import de.kitshn.android.api.tandoor.delete
import de.kitshn.android.api.tandoor.getObject
import de.kitshn.android.api.tandoor.model.TandoorKeyword
import de.kitshn.android.api.tandoor.model.TandoorStep
import de.kitshn.android.api.tandoor.patchObject
import de.kitshn.android.api.tandoor.putBitmap
import de.kitshn.android.api.tandoor.putMultipart
import de.kitshn.android.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.decodeFromJsonElement
import org.json.JSONArray
import org.json.JSONObject

@Serializable
class TandoorRecipe(
    var id: Int,
    // lower than 128 characters
    val name: String,
    // lower than 512 characters
    val description: String? = null,
    // url to an image
    val image: String? = null,
    val keywords: List<TandoorKeyword>,
    @SerialName("steps")
    val stepsRaw: JsonArray,
    val working_time: Int,
    val waiting_time: Int,
    @Serializable(with = JsonAsStringSerializer::class) val created_by: String,
    val created_at: String,
    val updated_at: String,
    val source_url: String? = null,
    val internal: Boolean,
    val show_ingredient_overview: Boolean = true,
    val servings: Int,
    // lower or equal than 32 characters
    val servings_text: String,
    val file_path: String = "",
    val private: Boolean = false,
    val rating: Double? = null,
    val last_cooked: String? = null,
    val new: Boolean = false,
    val recent: String? = null
) {

    var destroyed = false

    @Transient
    var steps = mutableListOf<TandoorStep>()

    init {
        steps = json.decodeFromJsonElement(stepsRaw)
    }

    @Transient
    var client: TandoorClient? = null

    @Composable
    fun loadThumbnail(): ImageRequest? {
        return if(image == null || client == null) {
            null
        } else {
            return client!!.media.createImageBuilder(image)
                .crossfade(true)
                .build()
        }
    }

    @Throws(TandoorRequestsError::class)
    suspend fun combineSteps(steps: List<TandoorStep>) {
        val base = steps[0]
        val remainingSteps = steps.drop(1)

        val ingredientsRaw = JSONArray(base.ingredientsRaw.toString())

        remainingSteps.forEach {
            if(it.name.isNotBlank()) {
                if(base.name.isBlank()) {
                    base.name = it.name
                } else {
                    base.name += " + " + it.name
                }
            }

            base.instruction += " " + it.instruction
            base.instructions_markdown += " " + it.instructions_markdown
            it.ingredientsRaw.forEach { i -> ingredientsRaw.put(JSONObject(i.toString())) }
            base.ingredients.addAll(it.ingredients)
            base.time += it.time

            base.show_ingredients_table = true

            if(it.step_recipe != null && base.step_recipe == null)
                base.step_recipe = it.step_recipe
        }

        // save combined step
        base.partialUpdate(
            name = base.name,
            instruction = base.instruction,
            instructions_markdown = base.instructions_markdown,
            ingredientsRaw = ingredientsRaw,
            time = base.time,
            show_ingredients_table = base.show_ingredients_table,
            step_recipe = base.step_recipe
        )

        // delete all other steps
        remainingSteps.forEach {
            deleteStep(it)
        }
    }

    @Throws(TandoorRequestsError::class)
    suspend fun delete() {
        if(client == null) return

        client!!.delete("/recipe/${id}/")

        client!!.container.recipe.remove(id)
        client!!.container.recipeOverview.remove(id)

        destroyed = true
        return
    }

    @Throws(TandoorRequestsError::class)
    suspend fun deleteStep(step: TandoorStep) {
        step.delete()
        steps = steps.filter { it.id != step.id }.toMutableList()
    }

    @Throws(TandoorRequestsError::class)
    suspend fun partialUpdate(
        name: String? = null,
        description: String? = null,
        keywords: List<TandoorKeyword>? = null,
        working_time: Int? = null,
        waiting_time: Int? = null,
        source_url: String? = null,
        servings: Int? = null,
        servings_text: String? = null
    ) {
        if(this.client == null) return

        val data = JSONObject().apply {
            if(name != null) put("name", name)
            if(description != null) put("description", description)
            if(keywords != null) {
                val keywordArray = JSONArray()
                keywords.forEach {
                    val keywordObject = JSONObject()
                    keywordObject.put("name", it.label)
                    keywordObject.put("description", it.description)
                    keywordArray.put(keywordObject)
                }
                put("keywords", keywordArray)
            }
            if(working_time != null) put("working_time", working_time)
            if(waiting_time != null) put("waiting_time", waiting_time)
            if(source_url != null) put("source_url", source_url)
            if(servings != null) put("servings", servings)
            if(servings_text != null) put("servings_text", servings_text)
        }

        Log.d("TandoorRecipe / Edit", data.toString(4))
        client!!.patchObject("/recipe/${id}/", data)
    }

    @Throws(TandoorRequestsError::class)
    suspend fun uploadImage(image: Bitmap) {
        client?.putBitmap("/recipe/${id}/image/", image)
    }

    @Throws(TandoorRequestsError::class)
    suspend fun setImageUrl(imageUrl: String) {
        client?.putMultipart("/recipe/${id}/image/", mutableMapOf<String, String>().apply {
            put("image_url", imageUrl)
        })
    }

    @Throws(TandoorRequestsError::class)
    suspend fun retrieveShareLink(): String? {
        if(this.client == null) return null
        return client!!.getObject("/share-link/${id}")!!.getString("link")
    }

    fun sortSteps(): List<TandoorStep> {
        if(steps.firstOrNull { it.order != 0 } == null) return steps

        return steps.toMutableList().apply {
            sortBy { it.order }
        }
    }

    fun toOverview(): TandoorRecipeOverview {
        val overview = json.decodeFromString<TandoorRecipeOverview>(json.encodeToString(this))
        overview.client = client
        return overview
    }

    fun showIngredientAllocationActionChipSync(): Boolean {
        if(steps.size < 2) return false
        if(steps.first().ingredients.isEmpty()) return false

        repeat(steps.size) {
            if(it == 0) return@repeat
            if(steps[it].ingredients.isNotEmpty()) return false
        }

        return true
    }

    @Composable
    fun showIngredientAllocationActionChip(): Boolean {
        var value by remember { mutableStateOf(showIngredientAllocationActionChipSync()) }
        LaunchedEffect(steps.toList()) { value = showIngredientAllocationActionChipSync() }
        return value
    }

    companion object {
        fun parse(client: TandoorClient, data: String): TandoorRecipe {
            val obj = json.decodeFromString<TandoorRecipe>(data)
            obj.client = client

            obj.steps.forEach { it.client = client }

            return obj
        }
    }

}