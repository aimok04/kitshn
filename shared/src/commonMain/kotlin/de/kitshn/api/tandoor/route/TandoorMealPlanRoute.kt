package de.kitshn.api.tandoor.route

import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.getObject
import de.kitshn.api.tandoor.model.TandoorMealPlan
import de.kitshn.api.tandoor.model.TandoorMealType
import de.kitshn.api.tandoor.model.TandoorPagedResponse
import de.kitshn.api.tandoor.model.recipe.TandoorRecipeOverview
import de.kitshn.api.tandoor.postObject
import de.kitshn.json
import de.kitshn.toStartOfDayString
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement

private const val PATH = "meal-plan/"

open class TandoorMealPlanRoute(client: TandoorClient) : TandoorBaseRoute(client) {


    suspend fun create(
        title: String = "",
        recipe: TandoorRecipeOverview? = null,
        servings: Double,
        note: String = "",
        from_date: LocalDate,
        to_date: LocalDate? = null,
        meal_type: TandoorMealType,
        addshopping: Boolean = false,
        shared: List<TandoorUser>? = null
    ): TandoorMealPlan {
        val data = buildJsonObject {
            put("title", JsonPrimitive(title))
            if(recipe != null) put("recipe", json.encodeToJsonElement(recipe))
            put("servings", JsonPrimitive(servings))
            put("note", JsonPrimitive(note))
            put("from_date", JsonPrimitive(from_date.toStartOfDayString()))
            if(to_date != null) put("to_date", JsonPrimitive(to_date.toStartOfDayString()))
            put("meal_type", json.encodeToJsonElement(meal_type))
            put("addshopping", JsonPrimitive(addshopping))
            shared?.let {
                val sharedObj = json.encodeToJsonElement(shared)
                put("shared", sharedObj)
            }
        }

        val mealPlan = TandoorMealPlan.parse(
            this.client,
            client.postObject("/${PATH}", data).toString()
        )

        mealPlan.client = client
        client.container.mealPlan[mealPlan.id] = mealPlan

        return mealPlan
    }

    suspend fun list(
        page: Int = 1,
        pageSize: Int? = 100,
        from: LocalDate? = null,
        to: LocalDate? = null,
        meal_type: Int? = null,
    ): TandoorPagedResponse<TandoorMealPlan> {
        val response = listPage<TandoorMealPlan>(
            path = PATH,
            page = page,
            pageSize = pageSize,
            extraParams = buildListExtraParams(from, to, meal_type)
        )

        processPage(response.results)

        return response
    }

    suspend fun listAll(
        pageSize: Int = 100,
        from: LocalDate? = null,
        to: LocalDate? = null,
        meal_type: Int? = null,
        onPageReceived: (suspend (List<TandoorMealPlan>) -> Boolean)? = null,
    ): TandoorPagedResponse<TandoorMealPlan> {
        val response = listAllPages<TandoorMealPlan>(
            path = PATH,
            pageSize = pageSize,
            extraParams = buildListExtraParams(from, to, meal_type)
        ) { page ->
            processPage(page)
            onPageReceived?.invoke(page) ?: false
        }

        return response
    }

    suspend fun retrieve(
        id: Int
    ): TandoorMealPlan {
        val mealPlan = TandoorMealPlan.parse(
            this.client,
            client.getObject("/meal-plan/${id}/").toString()
        )

        cache(mealPlan)
        return mealPlan
    }

    @OptIn(FormatStringsInDatetimeFormats::class)
    private fun buildListExtraParams(
        from: LocalDate? = null,
        to: LocalDate? = null,
        meal_type: Int? = null,
    ): List<Pair<String, String>> {
        val dateFormat = LocalDate.Format {
            byUnicodePattern("yyyy-MM-dd")
        }

        return buildList {
            from?.let { add("from_date" to dateFormat.format(it)) }
            to?.let { add("to_date" to dateFormat.format(it)) }
            meal_type?.let { add("meal_type" to it.toString()) }
        }
    }

    private fun processPage(page: List<TandoorMealPlan>) {
        page.forEach {
            it.client = client
            it.recipe?.client = client
            cache(it)
        }
    }

    private fun cache(mealPlan: TandoorMealPlan){
        client.container.mealType[mealPlan.meal_type.id] = mealPlan.meal_type
        client.container.mealPlan[mealPlan.id] = mealPlan
    }
}
