package de.kitshn.homepage.builder

import de.kitshn.R
import de.kitshn.api.tandoor.route.TandoorRecipeQueryParameters
import de.kitshn.api.tandoor.route.TandoorRecipeQueryParametersSortOrder
import de.kitshn.cache.FoodNameIdMapCache
import de.kitshn.cache.KeywordNameIdMapCache
import de.kitshn.homepage.model.HomePageSection
import java.time.LocalDateTime

data class HomePageSectionEnumCheckData(
    val dateTime: LocalDateTime
)

enum class HomePageSectionEnum(
    val title: Int,
    val weight: Float,
    val check: (d: HomePageSectionEnumCheckData) -> Boolean = { true },
    val queryParameters: List<HomePageQueryParameters>
) {
    SEASONAL_CHRISTMAS(
        title = R.string.home_section_seasonal_christmas,
        weight = 2f,
        check = {
            it.dateTime.hour in 12..24

                    &&

                    (
                            it.dateTime.month == java.time.Month.NOVEMBER && it.dateTime.dayOfMonth > 20
                                    || it.dateTime.month == java.time.Month.DECEMBER
                            )
        },
        queryParameters = listOf(
            HomePageQueryParameters(
                keywords = listOf(
                    "christmas", "xmas", "winter", "weihnachten", "festtag", "deftig"
                ),
                sortOrder = TandoorRecipeQueryParametersSortOrder.NEGATIVE_RATING
            )
        )
    ),

    BREAKFAST(
        title = R.string.home_section_enjoy_breakfast,
        weight = 1f,
        check = {
            it.dateTime.hour in 5..11
        },
        queryParameters = listOf(
            HomePageQueryParameters(
                keywords = listOf("breakfast", "frühstück", "oatmeal", "porridge")
            )
        )
    ),
    LUNCH(
        title = R.string.home_section_lunch_time,
        weight = 1f,
        check = {
            it.dateTime.hour in 11..13
        },
        queryParameters = listOf(
            HomePageQueryParameters(
                keywords = listOf("hauptgericht", "schnell", "einfach", "mittagessen", "lunch")
            )
        )
    ),
    AFTERNOON(
        title = R.string.home_section_baking,
        weight = 1f,
        check = {
            it.dateTime.hour in 14..16
        },
        queryParameters = listOf(
            HomePageQueryParameters(
                keywords = listOf("baking", "bake", "cake", "kuchen", "gebäck")
            )
        )
    ),
    DINNER(
        title = R.string.home_section_dinner,
        weight = 1f,
        check = {
            it.dateTime.hour in 17..21
        },
        queryParameters = listOf(
            HomePageQueryParameters(
                keywords = listOf(
                    "dinner",
                    "abendessen",
                    "abendbrot",
                    "hauptgericht",
                    "main-dish",
                    "main"
                )
            )
        )
    ),
    FAST(
        title = R.string.home_section_fast_food,
        weight = 1f,
        check = {
            it.dateTime.hour in 22..24
                    || it.dateTime.hour in 0..4
        },
        queryParameters = listOf(
            HomePageQueryParameters(
                keywords = listOf("fast", "schnell", "easy", "einfach")
            )
        )
    ),

    GO_TOS(
        title = R.string.home_section_go_tos,
        weight = 0.8f,
        queryParameters = listOf(
            HomePageQueryParameters(
                sortOrder = TandoorRecipeQueryParametersSortOrder.NEGATIVE_TIMES_COOKED
            )
        )
    ),
    NEW(
        title = R.string.home_section_new_recipes,
        weight = 0.8f,
        queryParameters = listOf(
            HomePageQueryParameters(
                new = true
            )
        )
    ),
    UNCOOKED(
        title = R.string.home_section_never_cooked,
        weight = 0.8f,
        queryParameters = listOf(
            HomePageQueryParameters(
                timescooked = 0
            )
        )
    ),
    FIVE_STARS(
        title = R.string.home_section_five_stars,
        weight = 0.8f,
        queryParameters = listOf(
            HomePageQueryParameters(
                rating = 5
            )
        )
    ),

    MEXICAN(
        title = R.string.home_section_mexican,
        weight = 0.5f,
        queryParameters = listOf(
            HomePageQueryParameters(
                keywords = listOf("mexiko", "mexico", "mexican", "mexikanisch")
            )
        )
    ),
    AMERICAN(
        title = R.string.home_section_american,
        weight = 0.5f,
        queryParameters = listOf(
            HomePageQueryParameters(
                keywords = listOf(
                    "american",
                    "america",
                    "american",
                    "usa",
                    "united states",
                    "amerika",
                    "amerikanisch",
                    "freedom"
                )
            )
        )
    ),
    CHINESE(
        title = R.string.home_section_chinese,
        weight = 0.5f,
        queryParameters = listOf(
            HomePageQueryParameters(
                keywords = listOf("china", "chinese", "chinesisch")
            )
        )
    ),
    INDIAN(
        title = R.string.home_section_indian,
        weight = 0.5f,
        queryParameters = listOf(
            HomePageQueryParameters(
                keywords = listOf("india", "indian", "indisch")
            )
        )
    ),
    ASIAN(
        title = R.string.home_section_asian,
        weight = 0.5f,
        queryParameters = listOf(
            HomePageQueryParameters(
                keywords = listOf("asia", "asian", "asiatisch", "asiatische rezepte")
            )
        )
    ),
    GERMAN(
        title = R.string.home_section_german,
        weight = 0.5f,
        queryParameters = listOf(
            HomePageQueryParameters(
                keywords = listOf("german", "germany", "deutsch", "deutschland")
            )
        )
    ),
    AUSTRIAN(
        title = R.string.home_section_austrian,
        weight = 0.5f,
        queryParameters = listOf(
            HomePageQueryParameters(
                keywords = listOf("austria", "austrian", "österreich", "österreichisch")
            )
        )
    ),
    VEGAN(
        title = R.string.home_section_vegan,
        weight = 0.5f,
        queryParameters = listOf(
            HomePageQueryParameters(
                query = "vegan"
            ),
            HomePageQueryParameters(
                keywords = listOf("vegan")
            )
        )
    ),
    VEGETARIAN(
        title = R.string.home_section_vegetarian,
        weight = 0.5f,
        queryParameters = listOf(
            HomePageQueryParameters(
                query = "vegetarian"
            ),
            HomePageQueryParameters(
                keywords = listOf("vegetarian")
            ),
            HomePageQueryParameters(
                query = "vegetarisch"
            ),
            HomePageQueryParameters(
                keywords = listOf("vegetarisch")
            )
        )
    ),

    BURGER(
        title = R.string.home_section_burger,
        weight = 0.2f,
        queryParameters = listOf(
            HomePageQueryParameters(
                query = "burger"
            ),
            HomePageQueryParameters(
                keywords = listOf("burger")
            )
        )
    ),
    NOODLES(
        title = R.string.home_section_noodles,
        weight = 0.2f,
        queryParameters = listOf(
            HomePageQueryParameters(
                query = "nudeln"
            ),
            HomePageQueryParameters(
                foods = listOf(
                    "vollkornnudeln",
                    "nudeln",
                    "nudel",
                    "vollkornnudel",
                    "spaghetti",
                    "noodles",
                    "noodle"
                )
            ),
            HomePageQueryParameters(
                keywords = listOf(
                    "noodles", "noodle", "nudeln", "nudel", "spaghetti"
                )
            )
        )
    );

    fun toHomePageSection(
        keywordNameIdMapCache: KeywordNameIdMapCache,
        foodNameIdMapCache: FoodNameIdMapCache
    ): HomePageSection {
        val queryParametersList = mutableListOf<TandoorRecipeQueryParameters>()

        queryParameters.forEach { qp ->
            val keywordList =
                qp.keywords?.mapNotNull { keywordNameIdMapCache.retrieve(it) } ?: listOf()
            val foodList = qp.foods?.mapNotNull { foodNameIdMapCache.retrieve(it) } ?: listOf()

            // don't add empty query parameters (empty qps will just list all recipes)
            if(qp.query == null && qp.new == null && qp.random == null && qp.rating == null && qp.timescooked == null && qp.sortOrder == null)
                if(keywordList.isEmpty() && foodList.isEmpty()) return@forEach

            queryParametersList.add(
                TandoorRecipeQueryParameters(
                    query = qp.query,
                    new = qp.new,
                    random = qp.random,
                    keywords = keywordList,
                    foods = foodList,
                    rating = qp.rating,
                    timescooked = qp.timescooked,
                    sortOrder = qp.sortOrder
                )
            )
        }

        return HomePageSection(
            this.title,
            queryParametersList
        )
    }
}