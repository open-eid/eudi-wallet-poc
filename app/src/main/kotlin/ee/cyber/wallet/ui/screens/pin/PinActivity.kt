package ee.cyber.wallet.ui.screens.pin

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import ee.cyber.wallet.ui.theme.WalletTheme

@AndroidEntryPoint
class PinActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        @Suppress("DEPRECATION")
        val input = intent.getParcelableExtra<Input>(PinActivityResultContract.EXTRA_INPUT)

        val flow = input?.flow ?: PinFlow.CONFIRM_PRESENTATION
        setContent {
            WalletTheme(useDarkTheme = isSystemInDarkTheme()) {
                Surface(Modifier.safeDrawingPadding()) {
                    var pin by remember { mutableStateOf("") }
                    if (pin.isEmpty() || flow in arrayOf(PinFlow.CONFIRM_PRESENTATION, PinFlow.CONFIRM_ISSUANCE)) {
                        PinEntryScreen(
                            viewModel = hiltViewModel<PinEntryViewModel, PinEntryViewModel.DetailViewModelFactory> { factory ->
                                val data = when (flow) {
                                    PinFlow.CONFIRM_PRESENTATION -> PinData.ConfirmPin.ConfirmPresentation("", input?.party.orEmpty())
                                    PinFlow.CONFIRM_ISSUANCE -> PinData.ConfirmPin.ConfirmIssuance("", input?.party.orEmpty())
                                    PinFlow.CREATE -> PinData.CreatePin("", false)
                                }
                                factory.create(data)
                            },
                            onSuccess = {
                                when (flow) {
                                    PinFlow.CONFIRM_PRESENTATION, PinFlow.CONFIRM_ISSUANCE -> finishWithResult(Result.Success(pin))
                                    PinFlow.CREATE -> pin = it
                                }
                            },
                            onFailure = { finishWithResult(Result.Failure) },
                            onCancel = { finishWithResult(Result.Cancelled) }
                        )
                    } else {
                        PinCreatedScreen {
                            finishWithResult(Result.Success(pin))
                        }
                    }
                }
            }
        }
    }

    private fun finishWithResult(result: Result) {
        when (result) {
            Result.Cancelled, Result.Failure -> setResult(result.resultCode)
            is Result.Success -> setResult(result.resultCode, Intent().apply { putExtra(PinActivityResultContract.EXTRA_PIN, result.pin) })
        }
        finish()
    }
}
