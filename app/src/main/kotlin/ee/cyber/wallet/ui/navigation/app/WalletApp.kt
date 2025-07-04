package ee.cyber.wallet.ui.navigation.app

import android.content.Intent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import ee.cyber.wallet.BuildConfig
import ee.cyber.wallet.domain.documents.CredentialDocument
import ee.cyber.wallet.domain.documents.fullName
import ee.cyber.wallet.domain.provider.wallet.WalletInstanceCredentials
import ee.cyber.wallet.ui.AppRoute
import ee.cyber.wallet.ui.model.UserData
import ee.cyber.wallet.ui.navigation.boarding.boardingNavGraph
import ee.cyber.wallet.ui.navigation.main.mainNavGraph
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import org.slf4j.LoggerFactory

data class WalletAppState(
    val walletCredentials: WalletInstanceCredentials?,
    val userData: UserData?,
    val documents: List<CredentialDocument>?,
    val navController: NavHostController
) {
    val isRegistered get() = walletCredentials?.isRegistered() == true && isPinCreated
    val isPinCreated get() = userData?.isPinCreated == true
    val isActivated get() = isPinCreated && documents?.isNotEmpty() == true
    val isLoaded get() = documents != null && userData != null

    val fullName: String
        get() = documents?.firstOrNull()?.fullName() ?: ""
}

@Composable
fun rememberAppState(
    walletCredentials: WalletInstanceCredentials? = null,
    user: UserData? = null,
    documents: List<CredentialDocument>? = null,
    navController: NavHostController = rememberNavController()
): WalletAppState = remember(user, documents, navController) {
    WalletAppState(
        walletCredentials = walletCredentials,
        userData = user,
        documents = documents,
        navController = navController
    )
}

private val logger = LoggerFactory.getLogger("WalletApp")

@Composable
fun WalletApp(viewModel: WalletAppViewModel, intent: Intent?) {
    val walletCredentials by viewModel.walletCredentials.collectAsStateWithLifecycle()
    val userData by viewModel.userData.collectAsStateWithLifecycle()
    val documents by viewModel.documents.collectAsStateWithLifecycle()
    val appState = rememberAppState(
        walletCredentials = walletCredentials,
        user = userData,
        documents = documents
    )
    DisposableEffect(Unit) {
        val navController = appState.navController
        val listener = NavController.OnDestinationChangedListener { _, destination, arguments ->
            if (BuildConfig.DEBUG) logger.debug("onDestinationChanged: {}, {}", destination, arguments)
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    if (appState.isLoaded) {
        val currentIntent = rememberUpdatedState(newValue = intent)
        LaunchedEffect(Unit) {
            snapshotFlow { currentIntent.value }.filterNotNull()
                .collect {
                    it.dataString?.toUri()?.also { deeplink ->
                        delay(300)
                        runCatching {
                            // handleDeepLink does not
                            appState.navController.navigate(deeplink)
                        }.onFailure { e ->
                            logger.warn("could not navigate to a deeplink: $deeplink", e)
                        }
                    }
                }
        }
        NavHost(
            navController = appState.navController,
            startDestination = if (appState.isActivated) AppRoute.Main.route else AppRoute.Boarding.route,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            boardingNavGraph(appState)
            mainNavGraph(appState)
        }
    }
}
