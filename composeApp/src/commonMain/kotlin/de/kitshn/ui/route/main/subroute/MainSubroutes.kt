package de.kitshn.ui.route.main.subroute

import de.kitshn.ui.route.Animation
import de.kitshn.ui.route.Route
import de.kitshn.ui.route.main.subroute.books.RouteMainSubrouteBooks
import de.kitshn.ui.route.main.subroute.home.RouteMainSubrouteHome
import de.kitshn.ui.route.main.subroute.list.RouteMainSubrouteList
import de.kitshn.ui.route.main.subroute.mealplan.RouteMainSubrouteMealplan
import de.kitshn.ui.route.main.subroute.settings.RouteMainSubrouteSettings
import de.kitshn.ui.route.main.subroute.shopping.RouteMainSubrouteShopping

val mainSubroutes = listOf(
    Route("home", Animation.NONE) { RouteMainSubrouteHome(p = it) },
    Route("mealplan", Animation.NONE) { RouteMainSubrouteMealplan(p = it) },
    Route("shopping", Animation.NONE) { RouteMainSubrouteShopping(p = it) },
    Route("books", Animation.NONE) { RouteMainSubrouteBooks(p = it) },
    Route("settings", Animation.NONE) { RouteMainSubrouteSettings(p = it) },

    Route("list", Animation.SLIDE_HORIZONTAL) { RouteMainSubrouteList(p = it) }
)