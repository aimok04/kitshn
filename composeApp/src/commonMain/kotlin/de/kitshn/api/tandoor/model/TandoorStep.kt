package de.kitshn.api.tandoor.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import co.touchlab.kermit.Logger
import coil3.request.ImageRequest
import coil3.request.crossfade
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorRequestsError
import de.kitshn.api.tandoor.delete
import de.kitshn.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.api.tandoor.model.recipe.TandoorStepRecipeData
import de.kitshn.api.tandoor.patchObject
import de.kitshn.api.tandoor.putObject
import de.kitshn.formatAmount
import de.kitshn.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class TandoorStepFile(
    val id: Int,
    val name: String,
    val file_download: String,
    val preview: String
)

@Serializable
class TandoorStep(
    val id: Int,
    var name: String,
    var instruction: String = "",
    @SerialName("ingredients")
    val ingredientsRaw: JsonArray,
    var instructions_markdown: String = "",
    var time: Int,
    val order: Int,
    val show_as_header: Boolean,
    val file: TandoorStepFile? = null,
    var step_recipe: Int? = null,
    val step_recipe_data: TandoorStepRecipeData? = null,
    var show_ingredients_table: Boolean = true
) {

    var _destroyed by mutableStateOf(false)

    @Transient
    var ingredients = mutableListOf<TandoorIngredient>()

    init {
        ingredients = json.decodeFromJsonElement(ingredientsRaw)
    }

    @Transient
    var client: TandoorClient? = null

    @Composable
    fun loadFilePreview(): ImageRequest? {
        return if(file?.preview == null || client == null) {
            null
        } else {
            return client!!.media.createImageBuilder(file.preview)
                .crossfade(true)
                .build()
        }
    }

    suspend fun partialUpdate(
        name: String? = null,
        instruction: String? = null,
        instructions_markdown: String? = null,
        order: Int? = null,
        time: Int? = null,
        ingredientsRaw: JsonArray? = null,
        step_recipe: Int? = null,
        show_ingredients_table: Boolean? = null
    ) {
        if(this.client == null) return

        val data = buildJsonObject {
            if(name != null) put("name", JsonPrimitive(name))
            if(instruction != null) put("instruction", JsonPrimitive(instruction))
            if(instructions_markdown != null) put(
                "instructions_markdown",
                JsonPrimitive(instructions_markdown)
            )
            if(order != null) put("order", JsonPrimitive(order))
            if(time != null) put("time", JsonPrimitive(time))
            if(ingredientsRaw != null) put("ingredients", ingredientsRaw)
            if(step_recipe != null) put("step_recipe", JsonPrimitive(step_recipe))
            if(show_ingredients_table != null) put(
                "show_ingredients_table",
                JsonPrimitive(show_ingredients_table)
            )
        }

        client!!.patchObject("/step/${id}/", data)
    }

    suspend fun updateRaw(
        obj: JsonObject
    ): TandoorStep {
        return json.decodeFromJsonElement<TandoorStep>(client!!.putObject("/step/${id}/", obj))
            .also {
                it.client = client
            }
    }

    suspend fun delete() {
        if(client == null) return

        client!!.delete("/step/${id}/")
        this._destroyed = true
    }

    fun getRawIngredientById(id: Int): JsonObject? {
        return ingredientsRaw.firstOrNull {
            it.jsonObject.getValue("id").jsonPrimitive.int == id
        }?.jsonObject
    }

    suspend fun fetchStepRecipe(): TandoorRecipe? {
        if(step_recipe == null) return null

        val recipe = client!!.container.recipe[step_recipe]
        if(recipe != null) return recipe

        try {
            return client!!.recipe.get(step_recipe!!)
        } catch(e: TandoorRequestsError) {
            Logger.e("TandoorStep.kt", e)
            return null
        }
    }

    @Composable
    fun instructionsWithTemplating(
        scale: Double = 1.0,
        fractional: Boolean = true
    ): String {
        var value by rememberSaveable { mutableStateOf("") }
        LaunchedEffect(scale) {
            value =
                instruction
                    .replace(Regex("\\{\\{ *ingredients\\[(\\d+)\\] *\\}\\}")) { // replaces ingredient templates
                        val index = it.destructured.component1().toInt()
                        ingredients.getOrNull(index)?.toString(
                            scale = scale,
                            fractional = fractional
                        ) ?: "Invalid ingredient template"
                    }
                    .replace(Regex("\\{\\{ *ingredients\\[(\\d+)\\]\\.amount *\\}\\}")) { // replaces ingredient amount templates
                        val index = it.destructured.component1().toInt()
                        ingredients.getOrNull(index)?.amount?.formatAmount(
                            fractional = fractional
                        ) ?: "Invalid ingredient template"
                    }
                    .replace(Regex("\\{\\{ *ingredients\\[(\\d+)\\]\\.unit *\\}\\}")) { // replaces ingredient unit templates
                        val index = it.destructured.component1().toInt()
                        val ingredient = ingredients.getOrNull(index)
                        ingredient?.getUnitLabel(amount = ingredient.amount * scale)
                            ?: "Invalid ingredient template"
                    }
                    .replace(Regex("\\{\\{ *ingredients\\[(\\d+)\\]\\.food *\\}\\}")) { // replaces ingredient food templates
                        val index = it.destructured.component1().toInt()
                        val ingredient = ingredients.getOrNull(index)
                        ingredient?.getLabel(amount = ingredient.amount * scale)
                            ?: "Invalid ingredient template"
                    }
                    .replace(Regex("\\{\\{ *ingredients\\[(\\d+)\\]\\.note *\\}\\}")) { // replaces ingredient note templates
                        val index = it.destructured.component1().toInt()
                        ingredients.getOrNull(index)?.note ?: "Invalid ingredient template"
                    }
                    .replace(Regex("\\{\\{ *scale\\(((\\d|\\.)+)\\) *\\}\\}")) { // replaces scale templates
                        val number = it.destructured.component1().toDoubleOrNull()

                        if(number == null) {
                            "Invalid scale template"
                        } else {
                            (number * scale).formatAmount(fractional)
                        }
                    }.replace(
                        Regex("\\{#[\\w\\s.,;:\\-()\\/%°\"„“'’&\\\$€?!*+…]*#\\}"),
                        ""
                    ) // replaces template comments
        }

        return value
    }

    override fun toString(): String {
        return "TandoorStep(id=$id, name='$name', instruction='$instruction', ingredientsRaw=$ingredientsRaw, instructions_markdown='$instructions_markdown', time=$time, order=$order, show_as_header=$show_as_header, file=$file, step_recipe=$step_recipe, step_recipe_data=$step_recipe_data, show_ingredients_table=$show_ingredients_table, ingredients=$ingredients, client=$client)"
    }
}