package ee.cyber.wallet.ui.navigation.main

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavDeepLink
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import ee.cyber.wallet.domain.AppError
import ee.cyber.wallet.domain.credentials.CredentialType
import ee.cyber.wallet.ui.AppRoute
import java.net.URLEncoder

sealed class MainRoute(
    override val route: String,
    override val arguments: List<NamedNavArgument> = listOf(),
    override val deepLinks: List<NavDeepLink> = listOf()
) : AppRoute(route, arguments, deepLinks) {

    companion object {
        const val ARG_URI = "uri"
        const val ARG_PIN = "pin"
    }

    data object Dashboard : MainRoute("dashboard")

    data object MyDocuments : MainRoute(
        route = "documents"
    )

    data object Settings : MainRoute(
        route = "settings"
    )

    data object Language : MainRoute(
        route = "language"
    )

    data object Document : MainRoute(
        route = "documents/{documentId}",
        arguments = listOf(
            navArgument("documentId") {
                type = NavType.StringType
            }
        )
    ) {
        fun view(documentId: String) = route.replace("{documentId}", documentId)
    }

    data object Presentation : MainRoute(
        route = "presentation?$ARG_URI={$ARG_URI}&$ARG_PIN={$ARG_PIN}",
        deepLinks = listOf(
            navDeepLink { uriPattern = "haip://present?client_id={clientId}&request_uri={requestUri}" },
            navDeepLink { uriPattern = "eudi-openid4vp://?client_id={clientId}&request_uri={requestUri}" },
            navDeepLink { uriPattern = "mdoc-openid4vp://?client_id={clientId}&request_uri={requestUri}" }
        ),
        arguments = listOf(
            navArgument(ARG_PIN) {
                nullable = true
                defaultValue = null
                type = NavType.StringType
            },
            navArgument(ARG_URI) {
                nullable = true
                defaultValue = null
                type = NavType.StringType
            },
            navArgument("clientId") {
                nullable = true
                defaultValue = null
                type = NavType.StringType
            },
            navArgument("requestUri") {
                nullable = true
                defaultValue = null
                type = NavType.StringType
            }
        )
    ) {
        fun view(uri: String) = route.replace("{$ARG_URI}", URLEncoder.encode(uri, "UTF-8"))
    }

    data object Scanner : MainRoute("scanner") // NON-NLS

    data object CredentialOffer : MainRoute(
        route = "offer?type={type}",
        arguments = listOf(
            navArgument("type") {
                nullable = false
                type = NavType.EnumType(CredentialType::class.java)
            }
        )
    ) {
        fun offerMdl() = route.replace("{type}", CredentialType.MDL.name)
    }

    data object Issuance : MainRoute(
        route = "issuance?type={type}&issueMockMdl={issueMockMdl}",
        arguments = listOf(
            navArgument("type") {
                nullable = true
                defaultValue = null
                type = NavType.StringType
            },
            navArgument("issueMockMdl") {
                nullable = false
                defaultValue = false
                type = NavType.BoolType
            }
        ),
        deepLinks = listOf(
            navDeepLink { uriPattern = "haip://issue?state={state}&code={code}" }
        )
    ) {
        fun issueMdl() = route.replace("{type}", CredentialType.MDL.name)
        fun issueMockMdl() = route.replace("{type}", CredentialType.MDL.name).replace("{issueMockMdl}", "true")
    }

    data object Error : MainRoute(
        route = "error?error={error}",
        arguments = listOf(
            navArgument("error") {
                nullable = false
                type = NavType.StringType
            }
        )
    ) {
        fun error(error: AppError) = route.replace("{error}", error.name)
    }

    data object ActivityLogs : MainRoute("activity_logs")
    data object ActivityLog : MainRoute(
        route = "activity_log?id={id}",
        arguments = listOf(
            navArgument("id") {
                nullable = false
                type = NavType.StringType
            }
        )
    ) {
        fun viewLog(id: Long) = route.replace("{id}", id.toString())
    }

    data object PidAuthentication : MainRoute("mock_pid_authentication")
    data object PidSharingConsentPin : MainRoute("mock_pid_sharing_consent_pin")
    data object PidSharingSuccess : MainRoute("mock_pid_sharing_success")
}

fun NavHostController.navigate(screen: MainRoute, builder: NavOptionsBuilder.() -> Unit = {}) = navigate(screen.route) { builder(this) }

// fun NavHostController.navigateToTopLevel(screen: MainRoute.TopLevel, builder: NavOptionsBuilder.() -> Unit = {}) {
//    navigate(screen.route) {
//        graph.startDestinationRoute?.let { route ->
//            popUpTo(route) {
//                saveState = true
//            }
//        }
//        launchSingleTop = true
//        restoreState = true
//        builder(this)
//    }
// }
