package ee.cyber.wallet.ui.navigation.boarding

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavDeepLink
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import ee.cyber.wallet.domain.AppError
import ee.cyber.wallet.ui.AppRoute

sealed class BoardingRoute(
    override val route: String,
    override val arguments: List<NamedNavArgument> = listOf(),
    override val deepLinks: List<NavDeepLink> = listOf()
) : AppRoute(route, arguments, deepLinks) {

    data object Welcome : BoardingRoute(route = "welcome")
    data object Activation : BoardingRoute(route = "activation")
    data object CredentialOffer : BoardingRoute(
        route = "boarding_offer",
        deepLinks = listOf(
            navDeepLink { uriPattern = "haip://pid?binding_token={bindingToken}" }
        ),
        arguments = listOf(
            navArgument("bindingToken") {
                nullable = true
                defaultValue = null
                type = NavType.StringType
            }
        )
    )

    data object Issuance : BoardingRoute(
        route = "boarding_issuance?bindingToken={bindingToken}",
        arguments = listOf(
            navArgument("bindingToken") {
                nullable = true
                defaultValue = null
                type = NavType.StringType
            }
        )
    ) {
        fun withBindingToken(bindingToken: String) = route.replace("{bindingToken}", bindingToken)
    }

    data object Error : BoardingRoute(
        route = "boarding_error?error={error}",
        arguments = listOf(
            navArgument("error") {
                nullable = false
                type = NavType.StringType
            }
        )
    ) {
        fun error(error: AppError) = route.replace("{error}", error.name)
    }

    data object CrossDeviceTutorial : BoardingRoute("boarding_cross_device_tutorial")
    data object Scanner : BoardingRoute("boarding_scanner")
    data object AuthenticationMeans : BoardingRoute("boarding_authentication_means")
    data object MobileIdAuthentication : BoardingRoute("boarding_mobile_id_authentication")
    data object MobileIdControlCode : BoardingRoute("boarding_mobile_id_control_code")
    data object MobileIdPinCode : BoardingRoute("boarding_mobile_id_pin_code")
    data object MobileIdSuccess : BoardingRoute("boarding_mobile_id_success")
}
