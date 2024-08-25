package de.kitshn.android.ui.route.main

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.kitshn.android.R
import de.kitshn.android.ui.dialog.version.TandoorServerVersionCompatibilityDialog
import de.kitshn.android.ui.route.RouteParameters
import de.kitshn.android.ui.route.main.subroute.MainSubrouteNavigation

enum class AppDestinations(
    val label: Int,
    val icon: ImageVector,
    val route: String
) {
    HOME(R.string.navigation_home, Icons.Default.Home, "home"),
    MEAL_PLAN(R.string.navigation_meal_plan, Icons.Default.CalendarMonth, "mealplan"),
    SHOPPING(R.string.navigation_shopping, Icons.Default.ShoppingCart, "shopping"),
    BOOKS(R.string.navigation_books, Icons.Default.Book, "books"),
    SETTINGS(R.string.navigation_settings, Icons.Default.Settings, "settings"),
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
                            contentDescription = stringResource(id = it.label)
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(id = it.label),
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