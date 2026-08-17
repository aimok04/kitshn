@file:OptIn(ExperimentalTime::class)

package de.kitshn.android.homepage.builder

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorRequestsError
import de.kitshn.cache.KeywordNameIdMapCache
import de.kitshn.homepage.builder.HomePageSectionEnum
import de.kitshn.homepage.builder.HomePageSectionEnumCheckData
import de.kitshn.homepage.model.HomePage
import de.kitshn.homepage.model.HomePageSection
import de.kitshn.repo.FoodRepo
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class HomePageBuilder(
    val client: TandoorClient
) {

    var homePage by mutableStateOf(
        HomePage(
            mutableListOf(),
            Clock.System.now().toEpochMilliseconds() + (1000L * 60L * 15L)
        )
    )

    suspend fun build(
        keywordNameIdMapCache: KeywordNameIdMapCache,
        foodRepo: FoodRepo
    ) {
        val checkData = HomePageSectionEnumCheckData(
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        )

        val byWeight = mutableMapOf<Float, MutableList<HomePageSectionEnum>>()
        HomePageSectionEnum.entries.forEach {
            if (!byWeight.containsKey(it.weight)) byWeight[it.weight] = mutableListOf()
            byWeight[it.weight]?.add(it)
        }

        byWeight.forEach {
            byWeight[it.key] = it.value.run {
                shuffle()
                it.value.filter { c -> c.check(checkData) }.toMutableList()
            }
        }

        var firstFailure: Exception? = null
        byWeight.forEach {
            val childSectionList = mutableListOf<HomePageSection>()

            for (sectionEnum in it.value) {
                val section = try {
                    val s = sectionEnum.toHomePageSection(
                        keywordNameIdMapCache,
                        foodRepo
                    )

                    if (!s.populate(client)) continue
                    s
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    if (firstFailure == null) firstFailure = e
                    continue
                }
                childSectionList.add(section)

                homePage.sections.add(section)
                homePage.sectionsStateList.add(section)

                if (childSectionList.size == 2) break
            }
        }

        val failure = firstFailure
        if (failure != null && homePage.sections.isEmpty()) throw when (failure) {
            is TandoorRequestsError -> failure
            else -> TandoorRequestsError(failure, null)
        }
    }
}