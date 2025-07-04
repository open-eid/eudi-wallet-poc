package ee.cyber.wallet.ui.navigation.boarding

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.ui.platform.LocalUriHandler
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import ee.cyber.wallet.AppConfig
import ee.cyber.wallet.domain.AppError
import ee.cyber.wallet.ui.AppRoute
import ee.cyber.wallet.ui.navigate
import ee.cyber.wallet.ui.navigation.app.WalletAppState
import ee.cyber.wallet.ui.popUpTo
import ee.cyber.wallet.ui.screen
import ee.cyber.wallet.ui.screens.activation.ActivationFlow
import ee.cyber.wallet.ui.screens.activation.ActivationScreen
import ee.cyber.wallet.ui.screens.boarding.AuthenticationMeansScreen
import ee.cyber.wallet.ui.screens.boarding.MobileIdAuthenticationScreen
import ee.cyber.wallet.ui.screens.boarding.MobileIdControlCodeScreen
import ee.cyber.wallet.ui.screens.boarding.MobileIdPin1Screen
import ee.cyber.wallet.ui.screens.boarding.MobileIdSuccessScreen
import ee.cyber.wallet.ui.screens.crossdevice.CrossDeviceTutorialScreen
import ee.cyber.wallet.ui.screens.error.AppErrorScreen
import ee.cyber.wallet.ui.screens.issuance.IssuanceScreen
import ee.cyber.wallet.ui.screens.issuance.IssuanceScreenNavigationHandler
import ee.cyber.wallet.ui.screens.offer.CredentialOfferScreen
import ee.cyber.wallet.ui.screens.offer.CredentialOfferScreenNavigationHandler
import ee.cyber.wallet.ui.screens.pin.Input
import ee.cyber.wallet.ui.screens.pin.PinActivityResultContract
import ee.cyber.wallet.ui.screens.pin.PinFlow
import ee.cyber.wallet.ui.screens.scanner.ScannerScreen
import ee.cyber.wallet.ui.screens.welcome.WelcomeScreen
import io.ktor.http.Url
import java.net.URLEncoder

fun NavGraphBuilder.boardingNavGraph(appState: WalletAppState) {
    val navController = appState.navController

    navigation(
        startDestination = if (appState.isRegistered) BoardingRoute.Activation.route else BoardingRoute.Welcome.route,
        route = AppRoute.Boarding.route
    ) {
        screen(BoardingRoute.Welcome) {
            val pinLauncher = rememberLauncherForActivityResult(contract = PinActivityResultContract()) {}
            WelcomeScreen(viewModel = hiltViewModel()) {
                pinLauncher.launch(Input(PinFlow.CREATE))
            }
        }

        screen(BoardingRoute.Activation) {
            val uriHandler = LocalUriHandler.current
            ActivationScreen {
                when (it) {
                    ActivationFlow.SAME_DEVICE -> {
                        if (AppConfig.useMocks) {
                            navController.navigate(BoardingRoute.AuthenticationMeans)
                        } else {
                            uriHandler.openUri(AppConfig.pidProviderUrl)
                        }
                    }

                    ActivationFlow.CROSS_DEVICE -> navController.navigate(BoardingRoute.CrossDeviceTutorial)
                }
            }
        }

        screen(BoardingRoute.CredentialOffer) {
            CredentialOfferScreen(
                viewModel = hiltViewModel(),
                navigationHandler = CredentialOfferScreenNavigationHandler(
                    onContinueClicked = { _, bindingToken -> navController.navigate(BoardingRoute.Issuance.withBindingToken(bindingToken!!)) },
                    onCancelClicked = { navController.navigateUp() }
                )
            )
        }

        screen(BoardingRoute.Issuance) {
            IssuanceScreen(
                viewModel = hiltViewModel(),
                navigationHandler = IssuanceScreenNavigationHandler(
                    onContinueClicked = {},
                    onOpenUri = {},
                    onMockAuth = {},
                    onBackClicked = { navController.navigateUp() },
                    showError = {
                        navController.navigate(BoardingRoute.Error.error(it)) {
                            popUpTo(BoardingRoute.Issuance) {
                                inclusive = true
                            }
                        }
                    }
                )
            )
        }

        screen(BoardingRoute.Error) {
            val appError = it.arguments?.getString("error")?.let { arg -> AppError.entries.find { err -> arg == err.name } } ?: AppError.UNKNOWN_ERROR
            AppErrorScreen(appError) {
                navController.navigateUp()
            }
        }

        screen(BoardingRoute.CrossDeviceTutorial) {
            CrossDeviceTutorialScreen(
                onBack = { navController.navigateUp() },
                onQRCodeOpen = { navController.navigate(BoardingRoute.Scanner) }
            )
        }

        screen(BoardingRoute.Scanner) {
            ScannerScreen(
                onBack = { navController.navigateUp() },
                onQRCodeScanned = {
                    val bindingToken = Url(it).parameters["binding_token"]?.let { token -> URLEncoder.encode(token, "UTF-8") }
                    if (bindingToken != null) {
                        navController.navigate(BoardingRoute.Issuance.withBindingToken(bindingToken)) {
                            popUpTo(BoardingRoute.Scanner) {
                                inclusive = true
                            }
                        }
                    }
                }
            )
        }
        screen(BoardingRoute.AuthenticationMeans) {
            AuthenticationMeansScreen(
                navigateToMobileIdAuth = { navController.navigate(BoardingRoute.MobileIdAuthentication) }
            )
        }
        screen(BoardingRoute.MobileIdAuthentication) {
            MobileIdAuthenticationScreen(
                viewModel = hiltViewModel(),
                onContinue = { navController.navigate(BoardingRoute.MobileIdControlCode) },
                onCancel = { navController.navigate(BoardingRoute.AuthenticationMeans) }
            )
        }
        screen(BoardingRoute.MobileIdControlCode) {
            MobileIdControlCodeScreen(
                onContinue = { navController.navigate(BoardingRoute.MobileIdPinCode) },
                onCancel = { navController.navigate(BoardingRoute.AuthenticationMeans) }
            )
        }
        screen(BoardingRoute.MobileIdPinCode) {
            MobileIdPin1Screen(
                onContinue = { navController.navigate(BoardingRoute.MobileIdSuccess) },
                onCancel = { navController.navigate(BoardingRoute.AuthenticationMeans) }
            )
        }
        screen(BoardingRoute.MobileIdSuccess) {
            MobileIdSuccessScreen(
                onContinue = { navController.navigate(BoardingRoute.Issuance.withBindingToken("mock_binding_token")) }
            )
        }
    }
}
