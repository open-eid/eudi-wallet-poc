package ee.cyber.wallet.ui.screens.issuance

import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.cyber.wallet.AppConfig
import ee.cyber.wallet.data.repository.DocumentRepository
import ee.cyber.wallet.domain.AppError
import ee.cyber.wallet.domain.credentials.CredentialType
import ee.cyber.wallet.domain.credentials.OpenId4VCIManager
import ee.cyber.wallet.domain.documents.CredentialDocument
import ee.cyber.wallet.domain.documents.CredentialToDocumentMapper
import ee.cyber.wallet.domain.provider.Attestation
import ee.cyber.wallet.domain.provider.IssuePidUseCase
import ee.cyber.wallet.domain.provider.mdl.MdlProviderServiceMock
import ee.cyber.wallet.ui.mvi.MviViewModel
import ee.cyber.wallet.ui.mvi.ViewEvent
import ee.cyber.wallet.ui.mvi.ViewSideEffect
import ee.cyber.wallet.ui.mvi.ViewState
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.net.URLDecoder
import java.util.UUID
import javax.inject.Inject

data class UiState(
    val documents: List<CredentialDocument> = listOf(),
    val isLoading: Boolean = false
) : ViewState

sealed class Event : ViewEvent {
    data object AcceptDocument : Event()
    data object UserAuthenticated : Event()
}

sealed class Effect : ViewSideEffect {
    data class ShowError(val error: AppError) : Effect()
    data class NavigateToUri(val uri: String) : Effect()
    data class AuthenticateWithPin(val credentialType: CredentialType) : Effect()
    data object AuthenticateWithMockAuthFlow : Effect()
    data object Complete : Effect()
}

@HiltViewModel
class IssuanceViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val documentRepository: DocumentRepository,
    private val openId4VCIManager: OpenId4VCIManager,
    private val issuePidUseCase: IssuePidUseCase,
    private val mdlProviderServiceMock: MdlProviderServiceMock,
    private val credentialToDocumentMapper: CredentialToDocumentMapper
) : MviViewModel<Event, UiState, Effect>() {

    private val logger = LoggerFactory.getLogger("IssuanceViewModel")

    private val bindingToken: String? = savedStateHandle["bindingToken"]
    private val issueMockMdl: Boolean? = savedStateHandle["issueMockMdl"]

    private val credentialType: CredentialType? = CredentialType.entries.find { it.name == savedStateHandle["type"] }
    private val intent: Intent? = savedStateHandle[NavController.KEY_DEEP_LINK_INTENT]
    private val deepLinked: Boolean = intent?.data?.toString()?.startsWith(AppConfig.deepLinkSchema) == true
    private val uri = intent?.data?.let { URLDecoder.decode(it.toString(), "UTF-8") }

    private val authenticated = MutableStateFlow(false)

    init {
        logger.debug("bindingToken: \"{}\", credentialType: {}, deepLinked: {}, uri: {}", bindingToken, credentialType, deepLinked, uri)
        viewModelScope.launch {
            initialize()
        }
    }

    private suspend fun initialize() {
        when {
            bindingToken != null -> {
                issueEEPid(bindingToken)
            }

            uri != null && deepLinked -> {
                issueMdl()
            }

            credentialType == CredentialType.MDL -> {
                if (AppConfig.useMocks) {
                    if (issueMockMdl == true) {
                        issueMockMdl()
                    } else {
                        sendEffect { Effect.AuthenticateWithMockAuthFlow }
                    }
                } else {
                    requestMdlIssuance()
                }
            }

            else -> {
                // TODO: show error? exit?
            }
        }
    }

    private suspend fun issueMockMdl() {
        val mockMdl = mdlProviderServiceMock.issueMdl()
        val attestation = Attestation(
            id = UUID.randomUUID().toString(),
            credential = mockMdl.credential,
            type = CredentialType.MDL,
            keyAttestation = mockMdl.keyAttestation
        )
        val document = credentialToDocumentMapper.convert(attestation)!!
        setState { copy(documents = listOf(document)) }
    }

    private suspend fun issueMdl() {
        setLoading(true)
        runCatching {
            val credentialDocument = openId4VCIManager.getCredential(Url(uri!!))
            setState { copy(documents = listOf(credentialDocument)) }
        }.onFailure {
            logger.error("failed: $it", it)
            sendEffect { Effect.ShowError(AppError.ISSUANCE_ERROR) }
        }
        setLoading(false)
    }

    override fun initialState(): UiState = UiState(isLoading = true)

    override suspend fun handleEvents(event: Event): Unit = run {
        when (event) {
            Event.AcceptDocument -> {
                state.value.documents.forEach {
                    documentRepository.addDocument(it)
                }
                sendEffect { Effect.Complete }
            }

            Event.UserAuthenticated -> {
                authenticated.value = true
                initialize()
            }
        }
    }

    private suspend fun issueEEPid(bindingToken: String) {
        if (!authenticated.value) {
            sendEffect { Effect.AuthenticateWithPin(CredentialType.PID_SD_JWT) }
            return
        }

        setLoading(true)
        runCatching {
            val documents = issuePidUseCase.issuePid(bindingToken)
            setState {
                copy(documents = documents)
            }
            documents
        }.onFailure {
            logger.error("Failed to issue PID with bindingToken: $it", it)
            sendEffect { Effect.ShowError(AppError.ISSUANCE_ERROR) }
        }
        setLoading(false)
    }

    private suspend fun requestMdlIssuance() {
        if (!authenticated.value) {
            sendEffect { Effect.AuthenticateWithPin(CredentialType.MDL) }
            return
        }

        setLoading(true)
        runCatching {
            openId4VCIManager.prepareAuthorizationRequest().also {
                logger.info("Redirecting user to $it")
                sendEffect { Effect.NavigateToUri(it) }
            }
        }.onFailure {
            logger.error("Failed to authorize", it)
            sendEffect { Effect.ShowError(AppError.ISSUANCE_AUTHORIZATION_ERROR) }
        }
        setLoading(false)
    }

    private fun setLoading(loading: Boolean) = setState { copy(isLoading = loading) }
}
