package ee.cyber.wallet.ui.navigation.main

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import ee.cyber.wallet.domain.AppError
import ee.cyber.wallet.domain.credentials.CredentialType
import ee.cyber.wallet.ui.AppRoute
import ee.cyber.wallet.ui.components.AppContent
import ee.cyber.wallet.ui.navigate
import ee.cyber.wallet.ui.navigation.app.WalletAppState
import ee.cyber.wallet.ui.popUpTo
import ee.cyber.wallet.ui.screen
import ee.cyber.wallet.ui.screens.activity.ActivityLogScreen
import ee.cyber.wallet.ui.screens.activity.LogEntryScreen
import ee.cyber.wallet.ui.screens.boarding.PidAuthenticationScreen
import ee.cyber.wallet.ui.screens.dashboard.DashboardNavigationHandler
import ee.cyber.wallet.ui.screens.dashboard.DashboardScreen
import ee.cyber.wallet.ui.screens.document.DocumentScreen
import ee.cyber.wallet.ui.screens.documents.MyDocumentsScreen
import ee.cyber.wallet.ui.screens.error.AppErrorScreen
import ee.cyber.wallet.ui.screens.issuance.IssuanceScreen
import ee.cyber.wallet.ui.screens.issuance.IssuanceScreenNavigationHandler
import ee.cyber.wallet.ui.screens.offer.CredentialOfferScreen
import ee.cyber.wallet.ui.screens.offer.CredentialOfferScreenNavigationHandler
import ee.cyber.wallet.ui.screens.pin.PinData
import ee.cyber.wallet.ui.screens.pin.PinEntryScreen
import ee.cyber.wallet.ui.screens.pin.PinEntryViewModel
import ee.cyber.wallet.ui.screens.presentation.PresentationRequestScreenNavigationHandler
import ee.cyber.wallet.ui.screens.presentation.PresentationScreen
import ee.cyber.wallet.ui.screens.presentation.SuccessContent
import ee.cyber.wallet.ui.screens.proximity.ProximityScreen
import ee.cyber.wallet.ui.screens.scanner.ScannerScreen
import ee.cyber.wallet.ui.screens.settings.SettingsNavigationHandler
import ee.cyber.wallet.ui.screens.settings.SettingsScreen
import ee.cyber.wallet.ui.screens.settings.language.LanguageScreen

fun NavGraphBuilder.mainNavGraph(appState: WalletAppState) {
    val navController = appState.navController

    navigation(
        startDestination = MainRoute.Dashboard.route,
        route = AppRoute.Main.route
    ) {
        screen(MainRoute.Dashboard) {
            DashboardScreen(
                appState = appState,
                navigationHandler = DashboardNavigationHandler(
                    navigateBack = { navController.navigateUp() },
                    navigateToScanner = { navController.navigate(MainRoute.Scanner) },
                    navigateToProximity = { navController.navigate(MainRoute.Proximity) },
                    navigateToPresenter = { },
                    navigateToMyDocuments = { navController.navigate(MainRoute.MyDocuments) },
                    navigateToActivityLog = { navController.navigate(MainRoute.ActivityLogs) },
                    navigateToSettings = { navController.navigate(MainRoute.Settings) }
                )
            )
        }

        screen(MainRoute.Scanner) {
            ScannerScreen(
                onBack = { navController.navigateUp() },
                onQRCodeScanned = {
                    navController.navigate(MainRoute.Presentation.view(it)) {
                        popUpTo(MainRoute.Scanner.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        screen(MainRoute.Proximity) {
            ProximityScreen(
                onCancel = { navController.navigateUp() },
                onBack = { navController.navigateUp() }
            )
        }

        screen(MainRoute.Presentation) {
            val activity = LocalActivity.current
            PresentationScreen(
                viewModel = hiltViewModel(),
                navigationHandler = PresentationRequestScreenNavigationHandler(
                    onGoBack = { exit ->
                        if (exit) {
                            activity?.setResult(Activity.RESULT_OK)
                            activity?.finish()
                        } else {
                            navController.navigateUp()
                        }
                    },
                    onShowError = { error, retryable ->
                        navController.navigate(MainRoute.Error.error(error)) {
                            if (!retryable) {
                                popUpTo(MainRoute.Presentation) {
                                    inclusive = true
                                }
                            }
                        }
                    }
                )
            )
        }

        screen(MainRoute.Document) {
            DocumentScreen(
                viewModel = hiltViewModel(),
                // TODO: popBackStack conflicted with startDestination change in WalletApp
                onBack = { navController.navigateUp() }
            )
        }

        screen(MainRoute.MyDocuments) {
            MyDocumentsScreen(
                viewModel = hiltViewModel(),
                onDocumentClicked = { navController.navigate(MainRoute.Document.view(it)) },
                onAddDocument = {
                    when (it) {
                        CredentialType.MDL -> navController.navigate(MainRoute.CredentialOffer.offerMdl())
                        CredentialType.AGE_VERIFICATION -> navController.navigate(MainRoute.CredentialOffer.offerAgeVerification())
                        else -> {}
                    }
                },
                onBack = { navController.navigateUp() }
            )
        }

        screen(MainRoute.CredentialOffer) {
            CredentialOfferScreen(
                viewModel = hiltViewModel(),
                navigationHandler = CredentialOfferScreenNavigationHandler(
                    onContinueClicked = { credentialType, _ ->
                        when (credentialType) {
                            CredentialType.MDL -> {
                                navController.navigate(MainRoute.Issuance.issueMdl()) {
                                    popUpTo(MainRoute.CredentialOffer) {
                                        inclusive = true
                                    }
                                }
                            }
                            CredentialType.AGE_VERIFICATION -> {
                                navController.navigate(MainRoute.Issuance.issueMockAgeVerification()) {
                                    popUpTo(MainRoute.CredentialOffer) {
                                        inclusive = true
                                    }
                                }
                            }
                            else -> {}
                        }
                    },
                    onCancelClicked = { navController.navigateUp() }
                )
            )
        }

        screen(MainRoute.Issuance) {
            val uriHandler = LocalUriHandler.current
            IssuanceScreen(
                viewModel = hiltViewModel(),
                navigationHandler = IssuanceScreenNavigationHandler(
                    onContinueClicked = {
                        navController.navigate(MainRoute.MyDocuments) {
                            popUpTo(MainRoute.MyDocuments) {
                                inclusive = true
                            }
                        }
                    },
                    onMockAuth = {
                        navController.navigate(MainRoute.PidAuthentication)
                    },
                    onBackClicked = { navController.navigateUp() },
                    onOpenUri = { uriHandler.openUri(it) },
                    showError = {
                        navController.navigate(MainRoute.Error.error(it)) {
                            popUpTo(MainRoute.Issuance) {
                                inclusive = true
                            }
                        }
                    }
                )
            )
        }

        screen(MainRoute.Settings) {
            SettingsScreen(
                viewModel = hiltViewModel(),
                navigationHandler = SettingsNavigationHandler(
                    navigateBack = { navController.navigateUp() },
                    navigateToLanguage = { navController.navigate(MainRoute.Language) }
                )
            )
        }

        screen(MainRoute.Language) {
            LanguageScreen(
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.navigateUp() }
            )
        }

        screen(MainRoute.Error) {
            val appError = it.arguments?.getString("error")?.let { arg -> AppError.entries.find { err -> arg == err.name } } ?: AppError.UNKNOWN_ERROR
            AppErrorScreen(appError) {
                navController.navigateUp()
            }
        }

        screen(MainRoute.ActivityLogs) {
            ActivityLogScreen(
                viewModel = hiltViewModel(),
                onItemClicked = { navController.navigate(MainRoute.ActivityLog.viewLog(it)) },
                onNavigateBack = { navController.navigateUp() }
            )
        }

        screen(MainRoute.ActivityLog) {
            LogEntryScreen(
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.navigateUp() }
            )
        }

        screen(MainRoute.PidAuthentication) {
            PidAuthenticationScreen(
                viewModel = hiltViewModel(),
                onShare = {
                    navController.navigate(MainRoute.PidSharingConsentPin)
                },
                onCancel = {
                    navController.navigate(MainRoute.MyDocuments) {
                        popUpTo(MainRoute.MyDocuments) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        screen(MainRoute.PidSharingConsentPin) {
            PinEntryScreen(
                viewModel = hiltViewModel<PinEntryViewModel, PinEntryViewModel.DetailViewModelFactory> { factory ->
                    factory.create(PinData.ConfirmPin.ConfirmPresentation("", "tara.ria.ee"))
                },
                onSuccess = {
                    navController.navigate(MainRoute.PidSharingSuccess)
                },
                onFailure = {
                    navController.navigate(MainRoute.MyDocuments) {
                        popUpTo(MainRoute.MyDocuments) {
                            inclusive = true
                        }
                    }
                },
                onCancel = {
                    navController.navigate(MainRoute.MyDocuments) {
                        popUpTo(MainRoute.MyDocuments) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        screen(MainRoute.PidSharingSuccess) {
            AppContent {
                SuccessContent(
                    autoClose = true,
                    onContinueClicked = {
                        navController.navigate(MainRoute.Issuance.issueMockMdl())
                    }
                )
            }
        }
    }
}
