package de.kitshn.ui.route.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.kitshn.ui.dialog.version.TandoorServerVersionCompatibilityDialog
import de.kitshn.ui.route.RouteParameters
import de.kitshn.ui.route.main.subroute.MainSubrouteNavigation
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.navigation_books
import kitshn.composeapp.generated.resources.navigation_home
import kitshn.composeapp.generated.resources.navigation_meal_plan
import kitshn.composeapp.generated.resources.navigation_settings
import kitshn.composeapp.generated.resources.navigation_shopping
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

enum class AppDestinations(
    val label: StringResource,
    val icon: ImageVector,
    val route: String
) {
    HOME(Res.string.navigation_home, Icons.Default.Home, "home"),
    MEAL_PLAN(Res.string.navigation_meal_plan, Icons.Default.CalendarMonth, "mealplan"),
    SHOPPING(Res.string.navigation_shopping, Icons.Default.ShoppingCart, "shopping"),
    BOOKS(Res.string.navigation_books, Icons.Default.Book, "books"),
    SETTINGS(Res.string.navigation_settings, Icons.Default.Settings, "settings"),
}

@Composable
fun RouteMain(p: RouteParameters) {
    if(p.vm.tandoorClient == null) return

    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    val mainSubNavHostController = rememberNavController()

    val destination by mainSubNavHostController.currentBackStackEntryAsState()
    LaunchedEffect(destination) {
        val route = destination?.destination?.route ?: ""

        AppDestinations.entries.forEach {
            if(!route.startsWith(it.route)) return@forEach
            currentDestination = it
        }
    }

    NavigationSuiteScaffold(
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationRailContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            imageVector = it.icon,
                            contentDescription = stringResource(it.label)
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(it.label),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    selected = it == currentDestination,
                    onClick = {
                        currentDestination = it
                        mainSubNavHostController.navigate(it.route)
                    }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        MainSubrouteNavigation(p.vm, mainSubNavHostController)
    }

    TandoorServerVersionCompatibilityDialog(vm = p.vm)
}