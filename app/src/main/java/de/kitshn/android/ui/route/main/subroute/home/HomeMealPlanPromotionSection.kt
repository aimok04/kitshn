package de.kitshn.android.ui.route.main.subroute.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.kitshn.android.R
import de.kitshn.android.api.tandoor.TandoorClient
import de.kitshn.android.api.tandoor.model.TandoorMealPlan
import de.kitshn.android.api.tandoor.model.recipe.TandoorRecipeOverview
import de.kitshn.android.api.tandoor.rememberTandoorRequestState
import de.kitshn.android.parseIsoTime
import de.kitshn.android.ui.TandoorRequestErrorHandler
import de.kitshn.android.ui.component.model.mealplan.HorizontalMealPlanCard
import de.kitshn.android.ui.modifier.loadingPlaceHolder
import de.kitshn.android.ui.state.ErrorLoadingSuccessState
import de.kitshn.android.ui.state.foreverRememberMutableStateList
import de.kitshn.android.ui.theme.Typography
import java.time.LocalDate
import java.time.LocalDateTime

@Composable
fun RouteMainSubrouteHomeMealPlanPromotionSection(
    client: TandoorClient,
    loadingState: ErrorLoadingSuccessState = ErrorLoadingSuccessState.SUCCESS,
    select: (recipeOverview: TandoorRecipeOverview, servings: Double) -> Unit
) {
    val mealPlans =
        foreverRememberMutableStateList<TandoorMealPlan>(key = "RouteMainSubrouteHome/mealPlanPromotion")

    val mainFetchRequest = rememberTandoorRequestState()
    LaunchedEffect(Unit) {
        mainFetchRequest.wrapRequest {
            client.mealPlan.fetch(
                LocalDate.now(),
                LocalDate.now()
            ).let {
                val today = LocalDateTime.now()

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
                .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                .loadingPlaceHolder(loadingState),
            text = stringResource(R.string.home_meal_plan_promotion_title),
            style = Typography.titleLarge
        )

        Card(
            Modifier
                .padding(16.dp)
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