package de.kitshn.cache

import coil3.PlatformContext
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.TandoorFood
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class FoodNameIdMapCache(
    context: PlatformContext,
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
        foods.forEach {
            settings.putInt("food_${it.name.lowercase().hashCode()}", it.id)
        }

        validUntil(Clock.System.now().toEpochMilliseconds() + 259200000 /* 3 days in millis */)
    }

    fun retrieve(name: String) = settings.getInt("food_${name.lowercase().hashCode()}", -1).takeIf { it != -1 }

}