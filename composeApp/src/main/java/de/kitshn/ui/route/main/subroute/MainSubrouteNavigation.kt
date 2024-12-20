package de.kitshn.ui.route.main.subroute

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import de.kitshn.KitshnViewModel
import de.kitshn.ui.route.RouteParameters

@Composable
fun MainSubrouteNavigation(
    vm: KitshnViewModel,
    controller: NavHostController
) {
    vm.mainSubNavHostController = controller

    NavHost(
        navController = controller,
        startDestination = "home"
    ) {
        mainSubroutes.forEach { route ->
            composable(
                route = route.route,
                arguments = route.arguments,
                deepLinks = route.deepLinks,
                content = {
                    val p = RouteParameters(vm, it)
                    route.content(this, p)
                },
                enterTransition = route.animation.enterTransition,
                exitTransition = route.animation.exitTransition,
                popEnterTransition = route.animation.popEnterTransition,
                popExitTransition = route.animation.popExitTransition
            )
        }
    }
}