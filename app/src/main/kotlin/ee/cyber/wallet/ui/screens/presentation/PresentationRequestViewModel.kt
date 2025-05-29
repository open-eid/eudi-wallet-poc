package ee.cyber.wallet.ui.screens.presentation

import android.content.Intent
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.cyber.wallet.AppConfig
import ee.cyber.wallet.data.repository.DocumentRepository
import ee.cyber.wallet.data.repository.TransactionLogRepository
import ee.cyber.wallet.domain.AppError
import ee.cyber.wallet.domain.credentials.CredentialAttribute
import ee.cyber.wallet.domain.credentials.CredentialType
import ee.cyber.wallet.domain.credentials.DocType
import ee.cyber.wallet.domain.credentials.Namespace
import ee.cyber.wallet.domain.documents.CredentialDocument
import ee.cyber.wallet.domain.documents.DocumentField
import ee.cyber.wallet.domain.documents.mdoc.MDocUtils
import ee.cyber.wallet.domain.presentation.CredentialClaim
import ee.cyber.wallet.domain.presentation.OpenId4VPManager
import ee.cyber.wallet.ui.mvi.MviViewModel
import ee.cyber.wallet.ui.mvi.ViewEvent
import ee.cyber.wallet.ui.mvi.ViewSideEffect
import ee.cyber.wallet.ui.mvi.ViewState
import ee.cyber.wallet.ui.navigation.main.MainRoute
import ee.cyber.wallet.ui.screens.documents.credentialType
import ee.cyber.wallet.ui.screens.documents.docType
import ee.cyber.wallet.ui.util.mapState
import ee.cyber.wallet.util.deserializeFromBase64
import ee.cyber.wallet.util.serializeToBase64
import eu.europa.ec.eudi.openid4vp.Consensus
import eu.europa.ec.eudi.openid4vp.DispatchOutcome
import eu.europa.ec.eudi.openid4vp.PresentationQuery
import eu.europa.ec.eudi.openid4vp.Resolution
import eu.europa.ec.eudi.openid4vp.ResolvedRequestObject
import eu.europa.ec.eudi.openid4vp.VpFormats
import eu.europa.ec.eudi.openid4vp.legalName
import eu.europa.ec.eudi.prex.FieldQueryResult
import eu.europa.ec.eudi.prex.Id
import eu.europa.ec.eudi.prex.Match
import eu.europa.ec.eudi.prex.PresentationDefinition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.URLDecoder
import java.util.*
import javax.inject.Inject

@Parcelize
data class MatchedField(
    val field: DocumentField,
    val checked: Boolean = false
) : Parcelable

typealias MatchedFields = List<MatchedField>

@Parcelize
data class Success(val redirectUri: URI? = null) : Parcelable

@Parcelize
data class UiState(
    val isLoading: Boolean = false,
    val verifier: String = "",
    val success: Success? = null,
    val shareDisabled: Boolean = false,
    val credentials: List<Credential> = listOf()
) : ViewState, Parcelable

@Parcelize
data class Credential(
    val credentialType: CredentialType = CredentialType.PID_SD_JWT,
    val fields: MatchedFields = listOf(),
    val optionalFields: MatchedFields = listOf()
) : Parcelable {
    private val allFields: MatchedFields
        get() = fields + optionalFields

    val allCheckedFields: MatchedFields
        get() = allFields.filter { it.checked }
}

@Parcelize
sealed class Error(
    open val appError: AppError,
    open val retryable: Boolean = false
) : Parcelable {
    data object UnknownError : Error(AppError.UNKNOWN_ERROR, false)
    data class ConnectionError(override val retryable: Boolean = false) : Error(AppError.CONNECTION_ERROR, retryable)
    data object VerificationError : Error(AppError.PRESENTATION_FETCH_REQUEST_ERROR)
    data object VerifierError : Error(AppError.PRESENTATION_VERIFIER_REJECTED_ERROR)
    data object UnsupportedRequestType : Error(AppError.PRESENTATION_UNSUPPORTED_REQUEST_ERROR)
    data object MatchError : Error(AppError.PRESENTATION_MATCH_ERROR)
}

sealed class Effect : ViewSideEffect {
    sealed class Back(open val exit: Boolean) : Effect() {
        data class Cancel(override val exit: Boolean) : Back(exit)
        data class Close(override val exit: Boolean) : Back(exit)
    }

    data class AuthenticateWithPin(val party: String) : Effect()
    data class ShowError(val error: Error) : Effect()
}

sealed class Event : ViewEvent {
    data class OnOptionalFieldChange(val field: MatchedField, val checked: Boolean) : Event()
    data object OnShareClicked : Event()
    data object OnCancelClicked : Event()
    data object OnCloseClicked : Event()

    data object UserAuthenticated : Event()
    data object IncorrectPin : Event()
}

private const val STATE_KEY = "state"
private const val REQUEST_KEY = "request"

@HiltViewModel
class PresentationRequestViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val openId4VPManager: OpenId4VPManager,
    private val documentRepository: DocumentRepository,
    private val transactionLogRepository: TransactionLogRepository
) : MviViewModel<Event, UiState, Effect>() {

    private val log = LoggerFactory.getLogger("PresentationRequestViewModel")

    override fun initialState(): UiState = savedStateHandle.get<UiState>(STATE_KEY) ?: UiState(isLoading = true)

    override suspend fun handleEvents(event: Event) {
        when (event) {
            is Event.OnOptionalFieldChange -> onOptionalFieldChange(event.field, event.checked)
            is Event.OnShareClicked -> onShareClicked()
            Event.OnCancelClicked -> {
                logTransaction(error = AppError.USER_CANCELLED, includeValues = false)
                onCancel()
            }

            Event.OnCloseClicked -> onCloseClicked()
            is Event.UserAuthenticated -> {
                authenticated.value = true
                if (resolvedRequest.value != null) {
                    sendResponse()
                }
            }

            Event.IncorrectPin -> {
                logTransaction(error = AppError.PRESENTATION_INCORRECT_PIN_ERROR, includeValues = false)
                onCancel()
            }
        }
    }

    private val resolvedRequest: StateFlow<ResolvedRequestObject.OpenId4VPAuthorization?> =
        savedStateHandle.getStateFlow<String?>(REQUEST_KEY, null)
            .mapState(viewModelScope) {
                val request = it?.deserializeFromBase64<OpenId4VPAuthorizationRequest>()

                request?.let {
                    ResolvedRequestObject.OpenId4VPAuthorization(
                        client = request.client,
                        responseMode = request.responseMode,
                        nonce = request.nonce,
                        state = request.state,
                        jarmRequirement = request.jarmRequirement,
                        vpFormats = VpFormats(request.vpFormats),
                        presentationQuery = PresentationQuery.ByPresentationDefinition(request.presentationDefinition)
                    )
                }
            }

    private fun saveResolvedRequest(request: ResolvedRequestObject.OpenId4VPAuthorization) {
        when (val presentationQuery = request.presentationQuery) {
            is PresentationQuery.ByPresentationDefinition -> {
                // OpenId4VPAuthorizationRequest/PresentationQuery are not serializable, so we need use OpenId4VPAuthorizationRequest as workaround.
                savedStateHandle[REQUEST_KEY] = OpenId4VPAuthorizationRequest(
                    client = request.client,
                    responseMode = request.responseMode,
                    nonce = request.nonce,
                    state = request.state,
                    jarmRequirement = request.jarmRequirement,
                    vpFormats = request.vpFormats.values,
                    presentationDefinition = presentationQuery.value
                ).serializeToBase64()
            }
            is PresentationQuery.ByDigitalCredentialsQuery -> {
                throw NotImplementedError()
            }
        }
    }

    private val intent: Intent? = savedStateHandle[NavController.KEY_DEEP_LINK_INTENT]
    private val deepLinked: Boolean = intent?.data?.toString()?.startsWith(AppConfig.deepLinkSchema) == true

    private val uri = (savedStateHandle[MainRoute.ARG_URI] ?: checkNotNull(intent).data.toString())
        .let { URLDecoder.decode(it, "UTF-8") }

    private val authenticated = MutableStateFlow(false)

    init {
        log.info("uri: $uri")
        if (resolvedRequest.value == null) {
            fetchRequest()
        }
    }

    private fun fetchRequest() {
        setLoading(true)
        viewModelScope.launch {
            runCatching { openId4VPManager.handleRequestUri(uri) }
                .onSuccess {
                    when (it) {
                        is Resolution.Success -> {
                            log.info("Presentation: $it")
                            handleRequestObject(it.requestObject)
                        }

                        is Resolution.Invalid -> {
                            log.error("Invalid: ${it.error}")
                            showError(Error.VerificationError)
                        }
                    }
                }.onFailure {
                    log.error("Error while fetching the request: ${it.message}")
                    showError(Error.ConnectionError())
                }
            setLoading(false)
        }
    }

    private fun onCancel() {
        setState { initialState() }
        sendEffect { Effect.Back.Cancel(deepLinked) }
    }

    private fun onCloseClicked() {
        sendEffect { Effect.Back.Close(deepLinked) }
    }

    private fun onShareClicked() {
        if (!authenticated.value) {
            sendEffect { Effect.AuthenticateWithPin(state.value.verifier) }
            return
        }
        sendResponse()
    }

    private fun onOptionalFieldChange(field: MatchedField, checked: Boolean) {
        updateState {
            copy(
                credentials = credentials.map { credential ->
                    credential.copy(
                        optionalFields = credential.optionalFields.map {
                            if (field == it && it.checked != checked) {
                                it.copy(checked = checked)
                            } else {
                                it
                            }
                        }
                    )
                }
            )
        }
    }

    private fun sendResponse() {
        val requestObject = resolvedRequest.value
        if (requestObject == null) {
            log.warn("resolvedRequestObject == null")
            return
        }
        setLoading(true)
        viewModelScope.launch {
            runCatching {
                val mdocNonce = MDocUtils.generateMdocGeneratedNonce()
                when (
                    val consensus = openId4VPManager.buildConsensus(
                        request = requestObject,
                        documents = documentRepository.documents.first(),
                        disclosedFields = state.value.credentials.flatMap { it.fields.map { it.field } + it.optionalFields.filter { it.checked }.map { it.field } },
                        mdocNonce = mdocNonce
                    )
                ) {
                    is Consensus.PositiveConsensus.VPTokenConsensus -> {
                        runCatching { openId4VPManager.sendResponse(requestObject, consensus, mdocNonce) }
                            .onSuccess {
                                when (it) {
                                    is DispatchOutcome.VerifierResponse.Accepted -> {
                                        log.info("Accepted: $it")
                                        logTransaction()
                                        setState { copy(success = Success(it.redirectURI)) }
                                    }

                                    is DispatchOutcome.RedirectURI -> {
                                        log.info("RedirectURI: $it")
                                        logTransaction()
                                        setState { copy(success = Success()) }
                                    }

                                    is DispatchOutcome.VerifierResponse.Rejected -> {
                                        log.error("Rejected: $it")
                                        // we treat it as successful transaction!!!
                                        logTransaction()
                                        showError(Error.VerifierError)
                                    }
                                }
                            }.onFailure {
                                log.error("Error sending Auth Response", it)
                                logTransaction(error = AppError.CONNECTION_ERROR)
                                sendEffect { Effect.ShowError(Error.ConnectionError(true)) }
                            }
                    }

                    else -> {
                        log.error("Negative consensus")
                        logTransaction(error = AppError.PRESENTATION_UNSUPPORTED_REQUEST_ERROR)
                        showError(Error.UnknownError)
                    }
                }
            }.onFailure {
                logTransaction(error = AppError.PRESENTATION_RESPONSE_ERROR)
                log.error("Error sending Auth Response", it)
                showError(Error.UnknownError)
            }
            setLoading(false)
        }
    }

    private suspend fun handleRequestObject(requestObject: ResolvedRequestObject) {
        when (requestObject) {
            is ResolvedRequestObject.OpenId4VPAuthorization -> {
                saveResolvedRequest(requestObject)

                val presentationDefinition = createPresentationDefinition(requestObject.presentationQuery)
                val documentMatches = presentationDefinition.getDocumentMatches(documentRepository.documents.first())
                when (val match = documentMatches.second) {
                    is Match.NotMatched -> {
                        logTransaction(error = AppError.PRESENTATION_MATCH_ERROR)
                        showError(Error.MatchError)
                        return
                    }
                    is Match.Matched -> {
                        val credentials = mutableListOf<Credential>()
                        val claims = documentMatches.first
                        match.matches.forEach { inputDescriptor ->
                            val candidateClaimId = inputDescriptor.value
                                .filter { (_, candidateClaim) ->
                                    candidateClaim.matches.any { it.value is FieldQueryResult.CandidateField.Found }
                                }
                                .keys
                                .first()
                            val document = claims.first { it.uniqueId == candidateClaimId }.credentialDocument
                            val fields = match.fields(
                                candidateClaimId,
                                false,
                                document.type
                            )
                            val optionalFields = match.fields(candidateClaimId, true, document.type)
                            if (fields.isEmpty() && optionalFields.isEmpty()) {
                                logTransaction(error = AppError.PRESENTATION_MATCH_ERROR)
                                showError(Error.MatchError)
                                return
                            }
                            val credential = Credential(
                                credentialType = document.credentialType(),
                                fields = fields,
                                optionalFields = optionalFields
                            )
                            credentials.add(credential)
                        }

                        setState {
                            copy(
                                verifier = requestObject.verifierName().toString(),
                                credentials = credentials
                            )
                        }
                    }
                }
            }

            else -> {
                log.error("Unsupported request object: $requestObject")
                logTransaction(error = AppError.UNKNOWN_ERROR)
                showError(Error.UnsupportedRequestType)
            }
        }
    }

    private fun createPresentationDefinition(presentationQuery: PresentationQuery): PresentationDefinition {
        return when (presentationQuery) {
            is PresentationQuery.ByPresentationDefinition -> {
                PresentationDefinition(
                    id = Id(UUID.randomUUID().toString()),
                    inputDescriptors = presentationQuery.value.inputDescriptors
                )
            }

            is PresentationQuery.ByDigitalCredentialsQuery -> {
                throw NotImplementedError()
            }
        }
    }

    private fun ResolvedRequestObject.verifierName() = client.legalName() ?: client.id

    private fun PresentationDefinition.getDocumentMatches(documents: List<CredentialDocument>): Pair<List<CredentialClaim>, Match> =
        openId4VPManager.matches(this, documents)

    private fun Match.Matched.fields(inputDescriptorId: String, optional: Boolean, docType: DocType): MatchedFields {
        return matches.entries
            .flatMap { matchEntry ->
                matchEntry.value.entries
                    .filter { valueEntry -> valueEntry.key == inputDescriptorId }
                    .flatMap { valueEntry ->
                        valueEntry.value.matches
                            .asSequence()
                            .filter { it.value is FieldQueryResult.CandidateField.Found }
                            .filter { it.key.optional == optional }
                            .filter { it.key.paths.none { path -> path.value == "\$.type" || path.value == "\$.vct" } } // Still exclude type and vct from results
                            .map {
                                val attr = CredentialAttribute.findByPath(it.key.paths.first().value)
                                MatchedField(
                                    field = DocumentField(
                                        namespace = attr?.namespace ?: Namespace.NONE,
                                        name = attr?.fieldName ?: it.key.name?.value ?: it.key.paths.first().value,
                                        value = (it.value as FieldQueryResult.CandidateField.Found).value(),
                                        optional = it.key.optional
                                    ),
                                    checked = !it.key.optional
                                )
                            }
                    }
            }
            .sortedBy { it.order(docType) }
            .toList()
    }

    private fun MatchedField.order(docType: DocType) = CredentialAttribute.find(field.namespace, field.name, docType)?.ordinal ?: Int.MAX_VALUE

    private fun FieldQueryResult.CandidateField.Found.value() = content.removeSurrounding("\"")

    private fun updateState(reducer: UiState.() -> UiState) {
        setState {
            reducer()
                .let { state ->
                    val disableShare = state.credentials.all { credential ->
                        credential.fields.isEmpty() && credential.optionalFields.none { it.checked }
                    }
                    if (state.shareDisabled != disableShare) {
                        state.copy(shareDisabled = disableShare)
                    } else {
                        state
                    }
                }
        }
    }

    private fun setLoading(isLoading: Boolean) = setState { copy(isLoading = isLoading) }

    private fun showError(error: Error) = sendEffect { Effect.ShowError(error) }

    override fun setState(reducer: UiState.() -> UiState) {
        super.setState(reducer)
        savedStateHandle[STATE_KEY] = state.value
    }

    private fun PresentationDefinition.paths() = inputDescriptors.first().constraints.fields()
        .mapNotNull { it.paths.firstOrNull()?.value }

    private fun PresentationDefinition?.toJsonObjectAttributes(fields: List<MatchedField>): JsonObject? {
        val attributes = this?.paths()?.associate { path ->
            path to JsonPrimitive(CredentialAttribute.findByPath(path)?.let { fields.findAttributeValue(it) })
        }?.let { JsonObject(it) }
        return attributes
    }

    private fun List<MatchedField>.findAttributeValue(attr: CredentialAttribute) =
        find { it.field.name == attr.fieldName && it.field.namespace == attr.namespace }?.field?.value

    private suspend fun logTransaction(error: AppError? = null, includeValues: Boolean = true) {
        val currentState = state.value

        for (credential in currentState.credentials) {
            resolvedRequest.value?.let {
                val presentationDefinition = createPresentationDefinition(it.presentationQuery)
                val attributes = presentationDefinition.toJsonObjectAttributes(if (includeValues) credential.allCheckedFields else listOf())
                transactionLogRepository.addTransactionLog(
                    party = currentState.verifier,
                    docType = credential.credentialType.docType(),
                    attributes = attributes,
                    error = error
                )
            }
        }
    }
}
