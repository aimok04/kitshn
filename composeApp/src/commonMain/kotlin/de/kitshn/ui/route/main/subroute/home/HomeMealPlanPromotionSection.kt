package de.kitshn.ui.route.main.subroute.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.TandoorMealPlan
import de.kitshn.api.tandoor.model.recipe.TandoorRecipeOverview
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.parseIsoTime
import de.kitshn.ui.TandoorRequestErrorHandler
import de.kitshn.ui.component.model.mealplan.HorizontalMealPlanCard
import de.kitshn.ui.modifier.loadingPlaceHolder
import de.kitshn.ui.state.ErrorLoadingSuccessState
import de.kitshn.ui.state.foreverRememberMutableStateList
import de.kitshn.ui.theme.Typography
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.home_meal_plan_promotion_title
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

@Composable
fun RouteMainSubrouteHomeMealPlanPromotionSection(
    client: TandoorClient,
    loadingState: ErrorLoadingSuccessState = ErrorLoadingSuccessState.SUCCESS,
    titlePadding: PaddingValues = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp),
    contentPadding: PaddingValues = PaddingValues(16.dp),
    select: (recipeOverview: TandoorRecipeOverview, servings: Double) -> Unit
) {
    val mealPlans =
        foreverRememberMutableStateList<TandoorMealPlan>(key = "RouteMainSubrouteHome/mealPlanPromotion")

    val mainFetchRequest = rememberTandoorRequestState()
    LaunchedEffect(Unit) {
        mainFetchRequest.wrapRequest {
            client.mealPlan.fetch(
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            ).let {
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

                val filteredMealPlans = it.filter { mealplan ->
                    // filter already cooked recipes

                    if(mealplan.recipe?.id == null) return@wrapRequest
                    val recipe = client.recipe.get(mealplan.recipe.id)

                    recipe.last_cooked.run {
                        if(this == null) {
                            true
                        } else {
                            val lastCooked = recipe.last_cooked!!.parseIsoTime()
                            !(today.year == lastCooked.year && today.dayOfYear == lastCooked.dayOfYear)
                        }
                    }
                }.sortedBy { mp ->
                    mp.meal_type.order
                }

                mealPlans.clear()
                mealPlans.addAll(filteredMealPlans)
            }
        }
    }

    if(mealPlans.size == 0 && loadingState != ErrorLoadingSuccessState.LOADING) return

    Column {
        Text(
            modifier = Modifier
                .padding(titlePadding)
                .loadingPlaceHolder(loadingState),
            text = stringResource(Res.string.home_meal_plan_promotion_title),
            style = Typography().titleLarge
        )

        Card(
            Modifier
                .padding(contentPadding)
                .loadingPlaceHolder(loadingState)
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if(loadingState == ErrorLoadingSuccessState.LOADING) {
                    HorizontalMealPlanCard(loadingState = loadingState)
                } else {
                    mealPlans.forEach { mealplan ->
                        if(mealplan.recipe == null) return@forEach

                        HorizontalMealPlanCard(mealPlan = mealplan) {
                            select(mealplan.recipe, mealplan.servings)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }

    TandoorRequestErrorHandler(state = mainFetchRequest)
}