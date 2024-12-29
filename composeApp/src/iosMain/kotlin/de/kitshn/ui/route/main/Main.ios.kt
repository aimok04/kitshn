package de.kitshn.ui.route.main

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph
import androidx.navigation.NavGraphNavigator
import androidx.navigation.NavHostController
import androidx.navigation.Navigator
import androidx.navigation.NavigatorProvider
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator

private var SaveObj: NavHostController? = null

@Composable
actual fun rememberAlternateNavController(): NavHostController {
    val navController = remember {
        SaveObj ?: createNavController()
    }

    DisposableEffect(Unit) {
        onDispose {
            SaveObj = navController
        }
    }

    return navController
}

private fun createNavController() =
    NavHostController().apply {
        navigatorProvider.addNavigator(ComposeNavGraphNavigator(navigatorProvider))
        navigatorProvider.addNavigator(ComposeNavigator())
        navigatorProvider.addNavigator(DialogNavigator())
    }

internal class ComposeNavGraphNavigator(
    navigatorProvider: NavigatorProvider
) : NavGraphNavigator(navigatorProvider) {
    override fun createDestination(): NavGraph {
        return ComposeNavGraph(this)
    }

    internal class ComposeNavGraph(
        navGraphNavigator: Navigator<out NavGraph>
    ) : NavGraph(navGraphNavigator) {
        internal var enterTransition: (
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = null

        internal var exitTransition: (
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = null

        internal var popEnterTransition: (
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = null

        internal var popExitTransition: (
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = null

        internal var sizeTransform: (
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> SizeTransform?)? = null
    }
}