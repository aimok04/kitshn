package de.kitshn.homepage.builder

import de.kitshn.api.tandoor.route.TandoorRecipeQueryParameters
import de.kitshn.api.tandoor.route.TandoorRecipeQueryParametersSortOrder
import de.kitshn.cache.FoodNameIdMapCache
import de.kitshn.cache.KeywordNameIdMapCache
import de.kitshn.homepage.model.HomePageSection
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.home_section_american
import kitshn.composeapp.generated.resources.home_section_asian
import kitshn.composeapp.generated.resources.home_section_austrian
import kitshn.composeapp.generated.resources.home_section_baking
import kitshn.composeapp.generated.resources.home_section_burger
import kitshn.composeapp.generated.resources.home_section_chinese
import kitshn.composeapp.generated.resources.home_section_dinner
import kitshn.composeapp.generated.resources.home_section_enjoy_breakfast
import kitshn.composeapp.generated.resources.home_section_fast_food
import kitshn.composeapp.generated.resources.home_section_five_stars
import kitshn.composeapp.generated.resources.home_section_german
import kitshn.composeapp.generated.resources.home_section_go_tos
import kitshn.composeapp.generated.resources.home_section_indian
import kitshn.composeapp.generated.resources.home_section_lunch_time
import kitshn.composeapp.generated.resources.home_section_mexican
import kitshn.composeapp.generated.resources.home_section_never_cooked
import kitshn.composeapp.generated.resources.home_section_new_recipes
import kitshn.composeapp.generated.resources.home_section_noodles
import kitshn.composeapp.generated.resources.home_section_seasonal_christmas
import kitshn.composeapp.generated.resources.home_section_vegan
import kitshn.composeapp.generated.resources.home_section_vegetarian
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.StringResource

data class HomePageSectionEnumCheckData(
    val dateTime: LocalDateTime
)

enum class HomePageSectionEnum(
    val title: StringResource,
    val weight: Float,
    val check: (d: HomePageSectionEnumCheckData) -> Boolean = { true },
    val queryParameters: List<HomePageQueryParameters>
) {
    SEASONAL_CHRISTMAS(
        title = Res.string.home_section_seasonal_christmas,
        weight = 2f,
        check = {
            it.dateTime.hour in 12..24

                    &&

                    (
                            it.dateTime.month == kotlinx.datetime.Month.NOVEMBER && it.dateTime.dayOfMonth > 20
                                    || it.dateTime.month == kotlinx.datetime.Month.DECEMBER
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
        title = Res.string.home_section_enjoy_breakfast,
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
        title = Res.string.home_section_lunch_time,
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
        title = Res.string.home_section_baking,
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
        title = Res.string.home_section_dinner,
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
        title = Res.string.home_section_fast_food,
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
        title = Res.string.home_section_go_tos,
        weight = 0.8f,
        queryParameters = listOf(
            HomePageQueryParameters(
                sortOrder = TandoorRecipeQueryParametersSortOrder.NEGATIVE_TIMES_COOKED
            )
        )
    ),
    NEW(
        title = Res.string.home_section_new_recipes,
        weight = 0.8f,
        queryParameters = listOf(
            HomePageQueryParameters(
                new = true
            )
        )
    ),
    UNCOOKED(
        title = Res.string.home_section_never_cooked,
        weight = 0.8f,
        queryParameters = listOf(
            HomePageQueryParameters(
                timescooked = 0
            )
        )
    ),
    FIVE_STARS(
        title = Res.string.home_section_five_stars,
        weight = 0.8f,
        queryParameters = listOf(
            HomePageQueryParameters(
                rating = 5
            )
        )
    ),

    MEXICAN(
        title = Res.string.home_section_mexican,
        weight = 0.5f,
        queryParameters = listOf(
            HomePageQueryParameters(
                keywords = listOf("mexiko", "mexico", "mexican", "mexikanisch")
            )
        )
    ),
    AMERICAN(
        title = Res.string.home_section_american,
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
        title = Res.string.home_section_chinese,
        weight = 0.5f,
        queryParameters = listOf(
            HomePageQueryParameters(
                keywords = listOf("china", "chinese", "chinesisch")
            )
        )
    ),
    INDIAN(
        title = Res.string.home_section_indian,
        weight = 0.5f,
        queryParameters = listOf(
            HomePageQueryParameters(
                keywords = listOf("india", "indian", "indisch")
            )
        )
    ),
    ASIAN(
        title = Res.string.home_section_asian,
        weight = 0.5f,
        queryParameters = listOf(
            HomePageQueryParameters(
                keywords = listOf("asia", "asian", "asiatisch", "asiatische rezepte")
            )
        )
    ),
    GERMAN(
        title = Res.string.home_section_german,
        weight = 0.5f,
        queryParameters = listOf(
            HomePageQueryParameters(
                keywords = listOf("german", "germany", "deutsch", "deutschland")
            )
        )
    ),
    AUSTRIAN(
        title = Res.string.home_section_austrian,
        weight = 0.5f,
        queryParameters = listOf(
            HomePageQueryParameters(
                keywords = listOf("austria", "austrian", "österreich", "österreichisch")
            )
        )
    ),
    VEGAN(
        title = Res.string.home_section_vegan,
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
        title = Res.string.home_section_vegetarian,
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
        title = Res.string.home_section_burger,
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
        title = Res.string.home_section_noodles,
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
            this.title.key,
            queryParametersList
        )
    }
}