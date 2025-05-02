package de.kitshn.api.tandoor.model.recipe

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import co.touchlab.kermit.Logger
import coil3.request.ImageRequest
import coil3.request.crossfade
import de.kitshn.JsonAsStringSerializer
import de.kitshn.KITSHN_KEYWORD_FLAG__HIDE_INGREDIENT_ALLOCATION_ACTION_CHIP
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.delete
import de.kitshn.api.tandoor.getObject
import de.kitshn.api.tandoor.model.TandoorFoodProperty
import de.kitshn.api.tandoor.model.TandoorKeyword
import de.kitshn.api.tandoor.model.TandoorMealPlan
import de.kitshn.api.tandoor.model.TandoorStep
import de.kitshn.api.tandoor.patchObject
import de.kitshn.api.tandoor.put
import de.kitshn.api.tandoor.putMultipart
import de.kitshn.json
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

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
    val properties: List<TandoorRecipeProperty>,
    @SerialName("food_properties")
    val foodPropertiesRaw: JsonObject,
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

    private var hideIngredientAllocationWarning by mutableStateOf(false)

    @Transient
    var steps = mutableListOf<TandoorStep>()

    @Transient
    var food_properties = mutableListOf<TandoorFoodProperty>()

    init {
        steps = json.decodeFromJsonElement(stepsRaw)
        hideIngredientAllocationWarning =
            keywords.firstOrNull { it.name == KITSHN_KEYWORD_FLAG__HIDE_INGREDIENT_ALLOCATION_ACTION_CHIP } != null

        foodPropertiesRaw.values.forEach {
            food_properties.add(json.decodeFromJsonElement(it))
        }
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

    suspend fun fetchSteps(): TandoorRecipe {
        val newRecipe = client!!.recipe.get(id = id)
        steps.clear()
        steps.addAll(newRecipe.steps)
        return this
    }

    suspend fun combineSteps(steps: List<TandoorStep>) {
        val base = steps[0]
        val remainingSteps = steps.drop(1)

        val ingredientsRaw = base.ingredientsRaw.toMutableList()

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
            it.ingredientsRaw.forEach { i -> ingredientsRaw.add(i) }
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
            ingredientsRaw = JsonArray(ingredientsRaw),
            time = base.time,
            show_ingredients_table = base.show_ingredients_table,
            step_recipe = base.step_recipe
        )

        // delete all other steps
        remainingSteps.forEach {
            deleteStep(it)
        }
    }

    suspend fun shopping(
        ingredients: List<Int>? = null,
        mealplan: TandoorMealPlan? = null,
        servings: Double? = null
    ) {
        if(client == null) return

        try {
            client!!.put("/recipe/${id}/shopping/", buildJsonObject {
                if(ingredients != null) put("ingredients", buildJsonArray {
                    ingredients.forEach { add(JsonPrimitive(it)) }
                })
                if(mealplan != null) put("mealplan", buildJsonObject {
                    put("id", mealplan.id)
                })
                if(servings != null) put("servings", servings)
            })
        } catch (ex: Exception) {
            // prevent HTTP 204 had non-zero Content-Length exception
            if(ex.message?.contains("HTTP 204") == true) return
            throw ex
        }
    }

    suspend fun delete() {
        if(client == null) return

        client!!.delete("/recipe/${id}/")

        client!!.container.recipe.remove(id)
        client!!.container.recipeOverview.remove(id)

        destroyed = true
        return
    }

    suspend fun deleteStep(step: TandoorStep) {
        step.delete()
        steps = steps.filter { it.id != step.id }.toMutableList()
    }

    suspend fun partialUpdate(
        name: String? = null,
        description: String? = null,
        keywords: List<TandoorKeyword>? = null,
        working_time: Int? = null,
        waiting_time: Int? = null,
        source_url: String? = null,
        servings: Int? = null,
        servings_text: String? = null,
        steps: JsonArray? = null
    ): TandoorRecipe {
        val data = buildJsonObject {
            if(name != null) put("name", name)
            if(description != null) put("description", description)
            if(keywords != null) {
                put("keywords", buildJsonArray {
                    keywords.forEach {
                        add(buildJsonObject {
                            put("name", it.name)
                            put("description", it.description)
                        })
                    }
                })
            }
            if(working_time != null) put("working_time", working_time)
            if(waiting_time != null) put("waiting_time", waiting_time)
            if(source_url != null) put("source_url", source_url)
            if(servings != null) put("servings", servings)
            if(servings_text != null) put("servings_text", servings_text)
            if(steps != null) put("steps", steps)
        }

        Logger.d("TandoorRecipe / Edit") { data.toString() }
        return parse(client!!, client!!.patchObject("/recipe/${id}/", data).toString())
    }

    /**
     * Helper function which adds a "flag" keyword to the recipe.
     * Used to extend Tandoor with custom functionality for kitshn.
     */
    suspend fun addFlag(
        name: String,
        description: String
    ) {
        partialUpdate(
            keywords = keywords.toMutableList().apply {
                add(
                    TandoorKeyword(
                        id = -1,
                        name = name,
                        description = description,
                        created_at = "",
                        updated_at = ""
                    )
                )
            }
        )

        hideIngredientAllocationWarning = true
    }

    suspend fun uploadImage(image: ByteArray) {
        client?.putMultipart("/recipe/${id}/image/") {
            append("image", value = image, headers = Headers.build {
                append(HttpHeaders.ContentType, "image/png")
                append(HttpHeaders.ContentDisposition, "filename=test.png")
            })
        }
    }

    suspend fun setImageUrl(imageUrl: String) {
        client?.putMultipart("/recipe/${id}/image/") {
            append("image_url", imageUrl)
        }
    }

    suspend fun retrieveShareLink(): String? {
        if(this.client == null) return null
        return client!!.getObject("/share-link/${id}")["link"]?.jsonPrimitive?.content
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
        if(hideIngredientAllocationWarning) return false
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
        if(hideIngredientAllocationWarning) return false

        var value by remember { mutableStateOf(showIngredientAllocationActionChipSync()) }
        LaunchedEffect(steps.toList()) { value = showIngredientAllocationActionChipSync() }
        return value
    }

    fun getRelevantFoodProperties(): List<TandoorFoodProperty> {
        return food_properties.toMutableList().filter {
            (it.food_values?.size ?: 0) != 0
                    && it.total_value > 0.0
        }
    }

    fun getRelevantRecipeProperties(): List<TandoorRecipeProperty> {
        return properties.toMutableList().filter {
            it.property_amount > 0.0
        }
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