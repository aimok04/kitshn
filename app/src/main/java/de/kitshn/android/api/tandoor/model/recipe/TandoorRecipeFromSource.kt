package de.kitshn.android.api.tandoor.model.recipe

import de.kitshn.android.api.tandoor.TandoorClient
import de.kitshn.android.api.tandoor.TandoorRequestsError
import de.kitshn.android.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.encodeToString
import org.json.JSONArray
import org.json.JSONObject

@Serializable
data class TandoorRecipeImportResponse(
    val link: String? = null,
    val recipeFromSource: TandoorRecipeFromSource? = null
) {
    companion object {
        fun parse(client: TandoorClient, data: String): TandoorRecipeImportResponse {
            val obj = json.decodeFromString<TandoorRecipeImportResponse>(data)
            obj.recipeFromSource?.let { it.client = client }
            return obj
        }
    }
}

@Serializable
class TandoorRecipeFromSource(
    @SerialName("recipe_json")
    val recipeJson: TandoorRecipeFromSourceRecipeJson,
    @SerialName("recipe_images")
    val recipeImages: List<String>,
) {

    @Transient
    var client: TandoorClient? = null

    @Throws(TandoorRequestsError::class)
    suspend fun create(
        imageUrl: String = recipeJson.image,
        keywords: List<String> = recipeJson.keywords.map { it.name ?: "" },
        splitSteps: Boolean = true
    ): TandoorRecipe {
        // fixes issue where part of note wasn't detected
        recipeJson.steps.map { it.ingredients }.forEach {
            it.forEach forEachIngredient@{ ingredient ->
                val split = ingredient.food.name.split(" (")
                if(split.size == 1) return@forEachIngredient

                ingredient.food = TandoorRecipeFromSourceFood(split[0])
                ingredient.note =
                    split.drop(1).joinToString(separator = " (", postfix = " (") + ingredient.note
            }
        }

        val data = JSONObject(json.encodeToString(recipeJson)).apply {
            put("image", imageUrl)

            put("keywords", JSONArray().apply {
                recipeJson.keywords.filter { keywords.contains(it.name) }
                    .forEach { put(JSONObject(json.encodeToString(it))) }
            })

            if(splitSteps && recipeJson.steps.size == 1) put("steps", JSONArray().apply {
                val instructions = recipeJson.steps[0].instruction.split("\n")

                instructions.forEachIndexed { index, s ->
                    if(index == 0) {
                        put(JSONObject(json.encodeToString(recipeJson.steps[0].apply {
                            instruction = s
                        })))
                    } else {
                        put(
                            JSONObject(
                                json.encodeToString(
                                    TandoorRecipeFromSourceStep(
                                        instruction = s,
                                        ingredients = listOf(),
                                        showIngredientsTable = true
                                    )
                                )
                            )
                        )
                    }
                }
            })
        }

        return client!!.recipe.create(data = data)
    }
}

@Serializable
data class TandoorRecipeFromSourceRecipeJson(
    val steps: List<TandoorRecipeFromSourceStep>,
    val internal: Boolean,
    @SerialName("source_url")
    val sourceUrl: String,
    val name: String,
    val description: String,
    val servings: Long,
    @SerialName("servings_text")
    val servingsText: String,
    @SerialName("working_time")
    val workingTime: Long,
    @SerialName("waiting_time")
    val waitingTime: Long,
    val image: String,
    val keywords: List<TandoorRecipeFromSourceKeyword>
)

@Serializable
data class TandoorRecipeFromSourceStep(
    var instruction: String,
    val ingredients: List<TandoorRecipeFromSourceIngredient>,
    @SerialName("show_ingredients_table")
    val showIngredientsTable: Boolean,
)

@Serializable
data class TandoorRecipeFromSourceIngredient(
    val amount: Double,
    var food: TandoorRecipeFromSourceFood,
    val unit: TandoorRecipeFromSourceUnit?,
    var note: String,
    @SerialName("original_text")
    val originalText: String,
)

@Serializable
data class TandoorRecipeFromSourceFood(
    var name: String,
)

@Serializable
data class TandoorRecipeFromSourceUnit(
    val name: String,
)

@Serializable
data class TandoorRecipeFromSourceKeyword(
    val label: String? = null,
    val name: String? = null,
    val id: Long? = null,
)