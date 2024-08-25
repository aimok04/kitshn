package de.kitshn.android.ui.route

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import de.kitshn.android.ui.route.alerts.RouteAlertInaccessibleInstance
import de.kitshn.android.ui.route.main.RouteMain
import de.kitshn.android.ui.route.onboarding.RouteOnboarding
import de.kitshn.android.ui.route.onboarding.RouteOnboardingSignIn
import de.kitshn.android.ui.route.onboarding.RouteOnboardingWelcome
import de.kitshn.android.ui.route.recipe.RouteRecipePublic
import de.kitshn.android.ui.route.recipe.RouteRecipeView
import de.kitshn.android.ui.route.recipe.cook.RouteRecipeCook

val routes = listOf(
    Route(
        "alert/inaccessibleInstance",
        Animation.SLIDE_VERTICAL
    ) { RouteAlertInaccessibleInstance(p = it) },

    Route("main", Animation.SLIDE_HORIZONTAL) { RouteMain(p = it) },

    Route(
        "recipe/{recipeId}/cook/{servings}",
        Animation.SLIDE_VERTICAL
    ) { RouteRecipeCook(p = it) },
    Route(
        "recipe/{recipeId}/view",
        Animation.SLIDE_VERTICAL
    ) { RouteRecipeView(p = it) },
    Route(
        "recipe/{recipeId}/public/{shareToken}",
        Animation.SLIDE_HORIZONTAL
    ) { RouteRecipePublic(p = it) },

    Route("onboarding", Animation.SLIDE_HORIZONTAL) { RouteOnboarding(p = it) },
    Route("onboarding/signIn", Animation.SLIDE_HORIZONTAL) { RouteOnboardingSignIn(p = it) },
    Route("onboarding/welcome", Animation.SLIDE_HORIZONTAL) { RouteOnboardingWelcome(p = it) }
)

data class Route(
    val route: String,
    val animation: Animation,
    val arguments: List<NamedNavArgument> = emptyList(),
    val deepLinks: List<NavDeepLink> = emptyList(),
    val content: @Composable AnimatedVisibilityScope.(RouteParameters) -> Unit
)

enum class Animation(
    val enterTransition: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = null,
    val exitTransition: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = null,
    val popEnterTransition: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = enterTransition,
    val popExitTransition: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = exitTransition,
) {
    SLIDE_HORIZONTAL(
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(500)
            )
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(500)
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(500)
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(500)
            )
        }
    ),
    SLIDE_VERTICAL(
        enterTransition = {
            slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(500)
            )
        },
        exitTransition = {
            slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(500)
            )
        },
        popEnterTransition = {
            slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(500)
            )
        },
        popExitTransition = {
            slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(500)
            )
        }
    ),
    NONE
}