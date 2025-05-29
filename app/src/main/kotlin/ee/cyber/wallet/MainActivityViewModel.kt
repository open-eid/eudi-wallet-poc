package ee.cyber.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.cyber.wallet.data.repository.DocumentRepository
import ee.cyber.wallet.data.repository.UserDataRepository
import ee.cyber.wallet.ui.model.UserData
import ee.cyber.wallet.ui.util.toStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    userDataRepository: UserDataRepository,
    documentRepository: DocumentRepository
) : ViewModel() {

    val uiState: StateFlow<MainActivityUiState> = combine(userDataRepository.userData, documentRepository.documents) { userData, _ ->
        MainActivityUiState.Success(userData)
    }.toStateFlow(viewModelScope, MainActivityUiState.Loading)
}

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    data class Success(val userData: UserData) : MainActivityUiState

    fun MainActivityUiState.isSuccess() = this is Success
}
