package ee.cyber.wallet.ui.screens.activity

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.cyber.wallet.data.database.toModel
import ee.cyber.wallet.data.repository.TransactionLogRepository
import ee.cyber.wallet.domain.AppError
import ee.cyber.wallet.domain.credentials.CredentialAttribute
import ee.cyber.wallet.domain.credentials.DocType
import ee.cyber.wallet.ui.util.toStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import javax.inject.Inject

data class LogEntryModel(
    val id: Long = 0,
    val date: Instant,
    val party: String,
    val type: DocType,
    val attributes: Map<CredentialAttribute, String?> = mapOf(),
    val error: AppError? = null
)

sealed class UiState {
    data class Success(val logEntry: LogEntryModel) : UiState()
    data object Loading : UiState()
}

@HiltViewModel
class LogEntryScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    transactionLogRepository: TransactionLogRepository
) : ViewModel() {

    private val logId: String = requireNotNull(savedStateHandle["id"])

    val state: StateFlow<UiState> = transactionLogRepository.getTransactionLog(logId)
        .map { it.toModel() }
        .map { UiState.Success(it) }
        .toStateFlow(viewModelScope, UiState.Loading)
}
