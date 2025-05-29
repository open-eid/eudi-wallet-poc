package ee.cyber.wallet.ui.mvi

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

interface ViewState

interface ViewEvent

interface ViewSideEffect

abstract class MviViewModel<Event : ViewEvent, UiState : ViewState, Effect : ViewSideEffect> : ViewModel() {

    private val initialState: UiState by lazy { initialState() }
    abstract fun initialState(): UiState

    private val _state: MutableState<UiState> by lazy { mutableStateOf(initialState) }
    val state: State<UiState> by lazy { _state }

    private val _event: MutableSharedFlow<Event> = MutableSharedFlow()

    private val _effect: Channel<Effect> = Channel()
    val effect = _effect.receiveAsFlow()

    init {
        subscribeToEvents()
    }

    fun sendEvent(event: Event) {
        viewModelScope.launch { _event.emit(event) }
    }

    protected open fun setState(reducer: UiState.() -> UiState) {
        val newState = state.value.reducer()
        _state.value = newState
    }

    private fun subscribeToEvents() {
        viewModelScope.launch {
            _event.collect {
                handleEvents(it)
            }
        }
    }

    abstract suspend fun handleEvents(event: Event)

    protected fun sendEffect(builder: () -> Effect) {
        val effectValue = builder()
        viewModelScope.launch { _effect.send(effectValue) }
    }
}
