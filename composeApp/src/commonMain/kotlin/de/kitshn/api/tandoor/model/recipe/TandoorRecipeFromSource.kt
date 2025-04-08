package de.kitshn.api.tandoor.model.recipe

import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

@Serializable
data class TandoorRecipeImportResponse(
    val link: String? = null,
    var recipeFromSource: TandoorRecipeFromSource? = null
) {
    companion object {
        fun parse(client: TandoorClient, data: String): TandoorRecipeImportResponse {
            val obj = json.decodeFromString<TandoorRecipeImportResponse>(data)

            if(obj.link == null)
                obj.recipeFromSource = json.decodeFromString<TandoorRecipeFromSource>(data)

            obj.recipeFromSource?.let { it.client = client }
            return obj
        }
    }
}

@Serializable
class TandoorRecipeFromSource(
    val recipe: TandoorRecipeFromSourceRecipeJson,
    val images: List<String>,
    val duplicates: List<TandoorRecipeFromSourceDuplicateRecipe>
) {

    @Transient
    var client: TandoorClient? = null

    suspend fun create(
        imageUrl: String,
        keywords: List<String> = recipe.keywords.map { it.name ?: "" },
        splitSteps: Boolean = true
    ): TandoorRecipe {
        // fixes issue where part of note wasn't detected
        recipe.steps.map { it.ingredients }.forEach {
            it.forEach forEachIngredient@{ ingredient ->
                val split = ingredient.food.name.split(" (")
                if(split.size == 1) return@forEachIngredient

                ingredient.food = TandoorRecipeFromSourceFood(split[0])
                ingredient.note =
                    split.drop(1).joinToString(separator = " (", postfix = " (") + ingredient.note
            }
        }

        val data = JsonObject(json.encodeToJsonElement(recipe).jsonObject.toMutableMap().apply {
            put("image", JsonPrimitive(imageUrl))

            put("keywords", buildJsonArray {
                recipe.keywords.filter { keywords.contains(it.name) }
                    .forEach { add(json.encodeToJsonElement(it)) }
            })

            if(splitSteps && recipe.steps.size == 1) put("steps", buildJsonArray {
                val instructions = recipe.steps[0].instruction.split("\n")

                instructions.forEachIndexed { index, s ->
                    if(index == 0) {
                        add(
                            json.encodeToJsonElement(recipe.steps[0].apply {
                                instruction = s
                            })
                        )
                    } else if (s.isNotBlank()) {
                        add(
                            json.encodeToJsonElement(
                                TandoorRecipeFromSourceStep(
                                    instruction = s,
                                    ingredients = listOf(),
                                    showIngredientsTable = true
                                )
                            )
                        )
                    }
                }
            })
        })

        return client!!.recipe.create(data = data)
    }
}

@Serializable
data class TandoorRecipeFromSourceDuplicateRecipe(
    val id: Int,
    val name: String
)

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
    @SerialName("image_url")
    val imageUrl: String,
    val keywords: List<TandoorRecipeFromSourceKeyword>
)

@Serializable
data class TandoorRecipeFromSourceStep(
    var instruction: String,
    val ingredients: List<TandoorRecipeFromSourceIngredient>,
    @SerialName("show_ingredients_table")
    val showIngredientsTable: Boolean = true
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