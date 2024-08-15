package de.kitshn.android.api.tandoor.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.kitshn.android.api.tandoor.TandoorClient
import de.kitshn.android.api.tandoor.TandoorRequestsError
import de.kitshn.android.api.tandoor.delete
import de.kitshn.android.api.tandoor.model.recipe.TandoorRecipe
import de.kitshn.android.api.tandoor.model.recipe.TandoorStepRecipeData
import de.kitshn.android.api.tandoor.patchObject
import de.kitshn.android.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.json.JSONArray
import org.json.JSONObject

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
    var instruction: String,
    @SerialName("ingredients")
    val ingredientsRaw: JsonArray,
    var instructions_markdown: String,
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

    @Throws(TandoorRequestsError::class)
    suspend fun partialUpdate(
        name: String? = null,
        instruction: String? = null,
        instructions_markdown: String? = null,
        order: Int? = null,
        time: Int? = null,
        ingredientsRaw: JSONArray? = null,
        step_recipe: Int? = null,
        show_ingredients_table: Boolean? = null
    ) {
        if(this.client == null) return

        val data = JSONObject().apply {
            if(name != null) put("name", name)
            if(instruction != null) put("instruction", instruction)
            if(instructions_markdown != null) put("instructions_markdown", instructions_markdown)
            if(order != null) put("order", order)
            if(time != null) put("time", time)
            if(ingredientsRaw != null) put("ingredients", ingredientsRaw)
            if(step_recipe != null) put("step_recipe", step_recipe)
            if(show_ingredients_table != null) put("show_ingredients_table", show_ingredients_table)
        }

        client!!.patchObject("/step/${id}/", data)
    }

    @Throws(TandoorRequestsError::class)
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

        val recipe = client!!.container.recipe.getOrDefault(step_recipe, null)
        if(recipe != null) return recipe

        try {
            return client!!.recipe.get(step_recipe!!)
        } catch(e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    override fun toString(): String {
        return "TandoorStep(id=$id, name='$name', instruction='$instruction', ingredientsRaw=$ingredientsRaw, instructions_markdown='$instructions_markdown', time=$time, order=$order, show_as_header=$show_as_header, file=$file, step_recipe=$step_recipe, step_recipe_data=$step_recipe_data, show_ingredients_table=$show_ingredients_table, ingredients=$ingredients, client=$client)"
    }


}