package ee.cyber.wallet.ui.screens.boarding

import android.content.Context
import android.os.Parcelable
import androidx.core.content.edit
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ee.cyber.wallet.domain.provider.pid.MOCK_APP_PREFS
import ee.cyber.wallet.domain.provider.pid.MOCK_USER_PID
import ee.cyber.wallet.ui.mvi.MviViewModel
import ee.cyber.wallet.ui.mvi.ViewEvent
import ee.cyber.wallet.ui.mvi.ViewSideEffect
import ee.cyber.wallet.ui.mvi.ViewState
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
class PidAuthenticationViewModel @Inject constructor(
    @ApplicationContext
    private val context: Context
) : MviViewModel<Event, UiState, Effect>() {

    override fun initialState(): UiState {
        val pid = context.getSharedPreferences(MOCK_APP_PREFS, Context.MODE_PRIVATE).getString(MOCK_USER_PID, "")
        return UiState(pid = pid!!)
    }

    override suspend fun handleEvents(event: Event) {
        when (event) {
            is Event.SetMockPid -> {
                setState { copy(pid = event.pid) }
                context.getSharedPreferences(MOCK_APP_PREFS, Context.MODE_PRIVATE).edit { putString(MOCK_USER_PID, event.pid) }
            }
        }
    }
}

@Parcelize
data class UiState(
    val pid: String = ""
) : ViewState, Parcelable

sealed class Effect : ViewSideEffect

sealed class Event : ViewEvent {
    data class SetMockPid(val pid: String) : Event()
}
