package ee.cyber.wallet.ui.screens.pin

import android.os.Parcelable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.cyber.wallet.data.datastore.UserSessionDataSource
import ee.cyber.wallet.data.repository.WalletCredentialsRepository
import ee.cyber.wallet.ui.mvi.MviViewModel
import ee.cyber.wallet.ui.mvi.ViewEvent
import ee.cyber.wallet.ui.mvi.ViewSideEffect
import ee.cyber.wallet.ui.mvi.ViewState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import org.slf4j.LoggerFactory

const val PIN_LENGTH = 4
private const val TOTAL_ATTEMPTS = 3
private const val STATE_KEY = "state"

sealed class Effect : ViewSideEffect {
    sealed class Result : Effect() {
        class Success(val pin: String) : Result()
        data object Failure : Result()
        data object Cancel : Result()
    }
}

@Parcelize
sealed class PinData(open val pin: String) : Parcelable {
    @Parcelize
    sealed class ConfirmPin(override val pin: String, open val party: String = "", open val attemptsLeft: Int = TOTAL_ATTEMPTS) : PinData(pin) {
        data class ConfirmPresentation(override val pin: String, override val party: String = "", override val attemptsLeft: Int = TOTAL_ATTEMPTS) :
            ConfirmPin(pin, party, attemptsLeft)

        data class ConfirmIssuance(override val pin: String, override val party: String = "", override val attemptsLeft: Int = TOTAL_ATTEMPTS) :
            ConfirmPin(pin, party, attemptsLeft)
    }

    data class CreatePin(override val pin: String, val confirmation: Boolean) : PinData(pin)
}

@Parcelize
data class PinViewState(
    val isLoading: Boolean = false,
    val pinData: PinData = PinData.ConfirmPin.ConfirmPresentation(""),
    val error: PinError? = null
) : ViewState, Parcelable

@Parcelize
sealed class PinError : Parcelable {
    data class IncorrectPin(val attemptsLeft: Int) : PinError()
    data object NotMatched : PinError()
    data object UnknownError : PinError()
}

sealed class Event : ViewEvent

@HiltViewModel(assistedFactory = PinEntryViewModel.DetailViewModelFactory::class)
class PinEntryViewModel @AssistedInject constructor(
    @Assisted val data: PinData,
    private val savedStateHandle: SavedStateHandle,
    private val userSessionDataSource: UserSessionDataSource,
    private val walletCredentialsRepository: WalletCredentialsRepository
) : MviViewModel<Event, PinViewState, Effect>() {

    @AssistedFactory
    interface DetailViewModelFactory {
        fun create(data: PinData): PinEntryViewModel
    }

    private val logger = LoggerFactory.getLogger("PinEntryViewModel")

    private var lastPin by mutableStateOf("")

    override fun initialState(): PinViewState = savedStateHandle.get<PinViewState>(STATE_KEY) ?: PinViewState(pinData = data)

    override suspend fun handleEvents(event: Event) {}

    private val storedPin = userSessionDataSource.userSession.map { it.pin }

    private suspend fun updatePin(pin: String) =
        userSessionDataSource.updatePin(pin)

    fun onNumPadAction(action: NumPadAction) {
        when (action) {
            is NumPadAction.Cancel -> {
                sendEffect { Effect.Result.Cancel }
            }

            is NumPadAction.Delete -> {
                setState {
                    val pin = pinData.pin.dropLast(1)
                    when (pinData) {
                        is PinData.ConfirmPin.ConfirmPresentation -> copy(pinData = pinData.copy(pin = pin))
                        is PinData.ConfirmPin.ConfirmIssuance -> copy(pinData = pinData.copy(pin = pin))
                        is PinData.CreatePin -> copy(pinData = pinData.copy(pin = pin))
                    }
                }
            }

            is NumPadAction.NumChar -> {
                var pin = state.value.pinData.pin
                if (pin.length < PIN_LENGTH) {
                    pin += action.char
                }
                setPinState(pin)
                if (state.value.pinData.pin.length == PIN_LENGTH) {
                    onPinEntered()
                }
            }
        }
    }

    private fun setPinState(pin: String, error: PinError? = null) {
        setState {
            when (pinData) {
                is PinData.ConfirmPin.ConfirmPresentation -> copy(pinData = pinData.copy(pin = pin), error = error)
                is PinData.ConfirmPin.ConfirmIssuance -> copy(pinData = pinData.copy(pin = pin), error = error)
                is PinData.CreatePin -> copy(pinData = pinData.copy(pin = pin), error = error)
            }
        }
    }

    private fun onPinEntered() {
        setLoading(true)

        viewModelScope.launch {
            // FIXME: do real remote call?
            delay(1000)
            when (val pinData = state.value.pinData) {
                is PinData.ConfirmPin -> {
                    if (pinData.pin != storedPin.first()) {
                        onFailedAttempt()
                    } else {
                        onSuccessConfirm()
                    }
                }

                is PinData.CreatePin -> {
                    if (pinData.confirmation) {
                        if (pinData.pin != lastPin) {
                            onFailedAttempt()
                        } else {
                            onSuccessCreate()
                        }
                    } else {
                        lastPin = pinData.pin
                        setState { copy(pinData = pinData.copy(pin = "", confirmation = true)) }
                    }
                }
            }

            setLoading(false)
        }
    }

    private fun onSuccessConfirm() {
        sendEffect { Effect.Result.Success(state.value.pinData.pin) }
    }

    private suspend fun onSuccessCreate() {
        runCatching {
            walletCredentialsRepository.registerInstance().also {
                logger.info("instance registered!")
                updatePin(state.value.pinData.pin)
                sendEffect { Effect.Result.Success(state.value.pinData.pin) }
            }
        }.onFailure {
            logger.error("instance registration failed!", it)
            setPinState("")
            sendEffect { Effect.Result.Failure }
        }
    }

    private fun onFailedAttempt() {
        when (val pinData = state.value.pinData) {
            is PinData.ConfirmPin -> {
                val attemptsLeft = pinData.attemptsLeft - 1
                when (pinData) {
                    is PinData.ConfirmPin.ConfirmPresentation ->
                        setState { copy(pinData = pinData.copy(pin = "", attemptsLeft = attemptsLeft), error = PinError.IncorrectPin(attemptsLeft)) }

                    is PinData.ConfirmPin.ConfirmIssuance ->
                        setState { copy(pinData = pinData.copy(pin = "", attemptsLeft = attemptsLeft), error = PinError.IncorrectPin(attemptsLeft)) }
                }
                if (attemptsLeft == 0) {
                    sendEffect { Effect.Result.Failure }
                }
            }

            is PinData.CreatePin -> {
                setState { copy(pinData = pinData.copy(pin = ""), error = PinError.NotMatched) }
            }
        }
    }

    private fun setLoading(loading: Boolean) = setState { copy(isLoading = loading) }

    override fun setState(reducer: PinViewState.() -> PinViewState) {
        super.setState(reducer)
        savedStateHandle[STATE_KEY] = state.value
    }
}

sealed interface NumPadAction {
    data object Delete : NumPadAction
    data object Cancel : NumPadAction

    class NumChar(val char: Char) : NumPadAction
}
