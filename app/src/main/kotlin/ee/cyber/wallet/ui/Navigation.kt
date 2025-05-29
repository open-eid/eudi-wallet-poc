package ee.cyber.wallet.ui

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.PopUpToBuilder
import androidx.navigation.compose.composable

abstract class AppRoute(
    open val route: String,
    open val arguments: List<NamedNavArgument>,
    open val deepLinks: List<NavDeepLink>
) {
    data object Boarding : AppRoute("boarding", listOf(), listOf())
    data object Main : AppRoute("main", listOf(), listOf())
}

fun NavGraphBuilder.screen(screen: AppRoute, content: @Composable (AnimatedContentScope.(NavBackStackEntry) -> Unit)) =
    composable(
        route = screen.route,
        deepLinks = screen.deepLinks,
        arguments = screen.arguments,
        content = content
    )

fun NavHostController.navigate(screen: AppRoute, builder: NavOptionsBuilder.() -> Unit = {}) =
    navigate(screen.route) { builder(this) }

fun NavOptionsBuilder.popUpTo(route: AppRoute, popUpToBuilder: PopUpToBuilder.() -> Unit = {}) = popUpTo(route.route, popUpToBuilder)
