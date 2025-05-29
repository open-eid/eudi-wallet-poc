package ee.cyber.wallet

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.util.Consumer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import ee.cyber.wallet.MainActivityUiState.Loading.isSuccess
import ee.cyber.wallet.ui.model.DarkThemeConfig
import ee.cyber.wallet.ui.navigation.app.WalletApp
import ee.cyber.wallet.ui.navigation.app.WalletAppViewModel
import ee.cyber.wallet.ui.theme.WalletTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<MainActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        var uiState: MainActivityUiState by mutableStateOf(MainActivityUiState.Loading)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.onEach {
                    delay(100)
                    uiState = it
                }.collect()
            }
        }

        splashScreen.setKeepOnScreenCondition { !uiState.isSuccess() }

        setContent {
            val newIntent by onNewIntent()
            if (newIntent != null) {
                // reset intent dues to issues with Nav
                intent = Intent()
            }
            val darkTheme = shouldUseDarkTheme(uiState)
            WalletTheme(useDarkTheme = darkTheme) {
                val appViewModel by viewModels<WalletAppViewModel>()
                Surface(
                    modifier = Modifier
                        .safeDrawingPadding()
                        .fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WalletApp(viewModel = appViewModel, newIntent)
                }
            }
        }
    }

    @Composable
    @SuppressLint("ProduceStateDoesNotAssignValue") // TODO: fix this
    private fun onNewIntent() = produceState(initialValue = intent) {
        val consumer = Consumer<Intent> {
            this.value = it
        }
        addOnNewIntentListener(consumer)
        awaitDispose {
            removeOnNewIntentListener(consumer)
        }
    }
}

@Composable
private fun shouldUseDarkTheme(
    uiState: MainActivityUiState
) = when (uiState) {
    MainActivityUiState.Loading -> isSystemInDarkTheme()
    is MainActivityUiState.Success -> when (uiState.userData.prefs.darkThemeConfig) {
        DarkThemeConfig.FOLLOW_SYSTEM -> isSystemInDarkTheme()
        DarkThemeConfig.LIGHT -> false
        DarkThemeConfig.DARK -> true
    }
}
