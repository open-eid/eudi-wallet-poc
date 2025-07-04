package ee.cyber.wallet.ui.screens.documents

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.cyber.wallet.data.repository.DocumentRepository
import ee.cyber.wallet.domain.credentials.CredentialType
import ee.cyber.wallet.domain.documents.CredentialDocument
import ee.cyber.wallet.ui.mvi.MviViewModel
import ee.cyber.wallet.ui.mvi.ViewEvent
import ee.cyber.wallet.ui.mvi.ViewSideEffect
import ee.cyber.wallet.ui.mvi.ViewState
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UiState(
    val documents: List<CredentialDocument> = listOf(),
    val supportedTypes: List<CredentialType> = listOf()
) : ViewState

sealed class Event : ViewEvent

sealed class Effect : ViewSideEffect

@HiltViewModel
class MyDocumentsViewModel @Inject constructor(
    documentRepository: DocumentRepository
) : MviViewModel<Event, UiState, Effect>() {

    private val documents = documentRepository.documents

    init {
        viewModelScope.launch {
            documents.collect { documents ->
                setState {
                    copy(
                        documents = documents,
                        supportedTypes = getSupportedTypes(documents)
                    )
                }
            }
        }
    }

    private fun getSupportedTypes(documents: List<CredentialDocument>): List<CredentialType> =
        CredentialType.entries.filter { type -> documents.none { it.credentialType() == type } }
            .filter { // filter out MDL if MDOC PID is not issued yet
                when (it) {
                    CredentialType.MDL -> documents.any { doc -> doc.credentialType() == CredentialType.PID_MDOC }
                    CredentialType.PID_SD_JWT, CredentialType.PID_MDOC, CredentialType.AGE_VERIFICATION -> true
                    else -> false
                }
            }

    override fun initialState(): UiState = UiState()

    override suspend fun handleEvents(event: Event) = TODO()
}
