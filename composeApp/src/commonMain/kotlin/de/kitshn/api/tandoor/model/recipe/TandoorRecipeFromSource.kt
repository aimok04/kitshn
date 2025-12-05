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
class TandoorRecipeFromSource(
    val recipe: TandoorRecipeFromSourceRecipeJson? = null,
    @SerialName("recipe_id")
    val recipeId: Int? = null,
    val images: List<String>,
    val error: Boolean = false,
    val msg: String? = "",
    val duplicates: List<TandoorRecipeFromSourceDuplicateRecipe>
) {

    @Transient
    var client: TandoorClient? = null

    suspend fun create(
        imageUrl: String,
        keywords: List<String> = recipe!!.keywords.map { it.name ?: "" },
        splitSteps: Boolean = true,
        autoSortIngredients: Boolean = true
    ): TandoorRecipe {
        // fixes issue where part of note wasn't detected
        recipe!!.steps.map { it.ingredients }.forEach {
            it.forEach forEachIngredient@{ ingredient ->
                val split = ingredient.food.name.split(" (")
                if(split.size == 1) return@forEachIngredient

                ingredient.food = TandoorRecipeFromSourceFood(split[0])
                ingredient.note =
                    split.drop(1).joinToString(separator = " (", postfix = " (") + ingredient.note
            }
        }

        val steps = recipe.steps.toMutableList()

        if(splitSteps && recipe.steps.size == 1) {
            val motherStep = recipe.steps[0]

            steps.clear()
            motherStep.instruction.split("\n").forEachIndexed { index, s ->
                if(index == 0) {
                    motherStep.instruction = s
                    steps.add(motherStep)
                } else if(s.isNotBlank()) {
                    steps.add(
                        TandoorRecipeFromSourceStep(
                            instruction = s,
                            ingredients = listOf(),
                            showIngredientsTable = true
                        )
                    )
                }
            }
        }

        if(autoSortIngredients && steps.size > 1) {
            val allIngredients = steps.flatMap { it.ingredients }.toMutableList()

            repeat(steps.size) { index ->
                if(index == 0) return@repeat

                val step = steps[index]
                val lowerCaseInstruction = step.instruction.lowercase()

                val assignedIngredients = allIngredients.filter {
                    lowerCaseInstruction.contains(
                        it.food.name.trim().lowercase()
                    )
                }
                allIngredients.removeAll(assignedIngredients)

                steps[index] = step.copy(ingredients = assignedIngredients)
            }

            // all remaining ingredients will be added to the first step
            val firstStep = steps.first()
            steps[0] = firstStep.copy(
                ingredients = allIngredients
            )
        }

        if(recipe.servings == null) recipe.servings = 1
        if(recipe.servingsText.length < 6) recipe.servingsText = ""

        val data = JsonObject(json.encodeToJsonElement(recipe).jsonObject.toMutableMap().apply {
            put("image", JsonPrimitive(imageUrl))

            put("keywords", buildJsonArray {
                recipe.keywords.filter { keywords.contains(it.name) }
                    .forEach { add(json.encodeToJsonElement(it)) }
            })

            put("steps", json.encodeToJsonElement(steps))
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
    var sourceUrl: String,
    val name: String,
    val description: String,
    var servings: Long? = null,
    @SerialName("servings_text")
    var servingsText: String,
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