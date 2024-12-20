package de.kitshn.cache

import android.content.Context
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.TandoorFood
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FoodNameIdMapCache(
    context: Context,
    client: TandoorClient
) : BaseCache("FOOD_NAME_ID_MAP", context, client) {

    suspend fun update(coroutineScope: CoroutineScope) {
        var res = client.food.list(pageSize = 500)

        apply(res.results)
        delay(100)

        if(res.next != null) coroutineScope.launch {
            var page = 1
            val foods = mutableListOf<TandoorFood>()

            while(res.next != null) {
                page++

                res = client.food.list(page = page, pageSize = 500)
                foods.addAll(res.results)
            }

            apply(foods)
        }
    }

    fun apply(foods: List<TandoorFood>) {
        sp.edit().apply {
            foods.forEach {
                putInt("food_${it.name.lowercase()}", it.id)
            }
        }.apply()

        validUntil(System.currentTimeMillis() + 259200000 /* 3 days in millis */)
    }

    fun retrieve(name: String) = sp.getInt("food_${name.lowercase()}", -1).takeIf { it != -1 }

}