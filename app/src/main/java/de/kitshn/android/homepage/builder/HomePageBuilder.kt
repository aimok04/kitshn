package de.kitshn.android.homepage.builder

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.kitshn.android.api.tandoor.TandoorClient
import de.kitshn.android.homepage.model.HomePage
import de.kitshn.android.homepage.model.HomePageSection
import java.time.LocalDateTime

class HomePageBuilder(
    val client: TandoorClient
) {

    var homePage by mutableStateOf(
        HomePage(
            mutableListOf(),
            System.currentTimeMillis() + (1000L * 60L * 15L)
        )
    )

    suspend fun build() {
        val checkData = HomePageSectionEnumCheckData(
            LocalDateTime.now()
        )

        client.keyword.retrieve()
        client.food.retrieve()

        val byWeight = mutableMapOf<Float, MutableList<HomePageSectionEnum>>()
        HomePageSectionEnum.entries.forEach {
            if(!byWeight.containsKey(it.weight)) byWeight[it.weight] = mutableListOf()
            byWeight[it.weight]?.add(it)
        }

        byWeight.forEach {
            byWeight[it.key] = it.value.run {
                shuffle()
                it.value.filter { c -> c.check(checkData) }.toMutableList()
            }
        }

        byWeight.forEach {
            val childSectionList = mutableListOf<HomePageSection>()

            for(sectionEnum in it.value) {
                val section = sectionEnum.toHomePageSection(client)

                val recipeIdList = mutableListOf<Int>()
                section.queryParameters.forEach { qp ->
                    val recipes =
                        client.recipe.list(parameters = qp, pageSize = 20).results.filter { r ->
                            !recipeIdList.contains(r.id)
                        }

                    recipes.forEach { r -> recipeIdList.add(r.id) }
                }

                if(recipeIdList.size < 2) continue

                section.recipeIds.addAll(recipeIdList)
                childSectionList.add(section)

                homePage.sections.add(section)
                homePage.sectionsStateList.add(section)

                if(childSectionList.size == 2) break
            }
        }
    }

}