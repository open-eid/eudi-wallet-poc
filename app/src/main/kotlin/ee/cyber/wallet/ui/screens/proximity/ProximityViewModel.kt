package ee.cyber.wallet.ui.screens.proximity

import android.content.Context
import android.graphics.Bitmap
import android.os.Parcelable
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ee.cyber.wallet.crypto.CryptoProvider
import ee.cyber.wallet.crypto.deviceCryptoProvider
import ee.cyber.wallet.data.datastore.UserPreferencesDataSource
import ee.cyber.wallet.data.repository.DocumentRepository
import ee.cyber.wallet.domain.credentials.CredentialType
import ee.cyber.wallet.domain.documents.CredentialDocument
import ee.cyber.wallet.domain.presentation.CredentialClaim
import ee.cyber.wallet.domain.presentation.OpenId4VPManager
import ee.cyber.wallet.domain.provider.Attestation
import ee.cyber.wallet.ui.mvi.MviViewModel
import ee.cyber.wallet.ui.mvi.ViewEvent
import ee.cyber.wallet.ui.mvi.ViewSideEffect
import ee.cyber.wallet.ui.mvi.ViewState
import ee.cyber.wallet.ui.screens.documents.credentialType
import ee.cyber.wallet.ui.screens.documents.docType
import ee.cyber.wallet.ui.screens.presentation.MatchedField
import ee.cyber.wallet.ui.screens.presentation.MatchedFields
import ee.cyber.wallet.ui.screens.presentation.fields
import eu.europa.ec.eudi.iso18013.transfer.TransferEvent
import eu.europa.ec.eudi.iso18013.transfer.TransferManager
import eu.europa.ec.eudi.iso18013.transfer.engagement.BleRetrievalMethod
import eu.europa.ec.eudi.iso18013.transfer.response.device.DeviceRequest
import eu.europa.ec.eudi.prex.Constraints
import eu.europa.ec.eudi.prex.Constraints.LimitDisclosure
import eu.europa.ec.eudi.prex.FieldConstraint
import eu.europa.ec.eudi.prex.FieldQueryResult
import eu.europa.ec.eudi.prex.Id
import eu.europa.ec.eudi.prex.InputDescriptor
import eu.europa.ec.eudi.prex.InputDescriptorId
import eu.europa.ec.eudi.prex.JsonPath
import eu.europa.ec.eudi.prex.Match
import eu.europa.ec.eudi.prex.PresentationDefinition
import id.walt.mdoc.dataelement.DataElement
import id.walt.mdoc.dataelement.EncodedCBORElement
import id.walt.mdoc.dataelement.ListElement
import id.walt.mdoc.dataelement.MapElement
import id.walt.mdoc.doc.MDoc
import id.walt.mdoc.docrequest.MDocRequestBuilder
import id.walt.mdoc.mdocauth.DeviceAuthentication
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ProximityViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val documentRepository: DocumentRepository,
    val openId4VPManager: OpenId4VPManager,
    var transferManager: TransferManager,
    val cryptoProviderFactory: CryptoProvider.Factory,
    private val userPreferencesDataSource: UserPreferencesDataSource
) : MviViewModel<Event, UiState, Effect>() {

    private val logger = LoggerFactory.getLogger(ProximityViewModel::class.java)

    init {
        viewModelScope.launch {
            userPreferencesDataSource.userPreferences.collect { prefs ->
                setState { copy(isPeripheralMode = prefs.blePeripheralMode) }
                setupTransferManager()
            }
        }
    }
    
    private fun setupTransferManager() {
        val isPeripheralMode = state.value.isPeripheralMode
        transferManager.setRetrievalMethods(listOf(
            BleRetrievalMethod(
                peripheralServerMode = isPeripheralMode,
                centralClientMode = !isPeripheralMode,
                clearBleCache = true
            )))
        transferManager.addTransferEventListener { event ->
            when (event) {
                is TransferEvent.QrEngagementReady -> {
                    val qrCodeBitmap = event.qrCode.asBitmap(size = 800)
                    setState { copy(qrCodeBitmap = qrCodeBitmap) }
                }

                TransferEvent.Connecting -> {
                    logger.info("Connecting to device...")
                }

                TransferEvent.Connected -> {
                    logger.info("Connected to device")
                }

                is TransferEvent.RequestReceived -> {
                    logger.info("Request received")
                    val deviceRequest = event.request as DeviceRequest

                    viewModelScope.launch {
                        handleRequestObject(deviceRequest)
                    }
                }

                TransferEvent.ResponseSent -> {
                    logger.info("Response sent to the device")
                    transferManager.stopPresentation(false)

                    sendEffect { Effect.ProximityResponseSent }
                }

                TransferEvent.Disconnected -> {
                    logger.info("Disconnected from the device")
                    transferManager.stopPresentation(false)
                }

                is TransferEvent.Error -> {
                    logger.error("Error occurred: ${event.error}")
                    transferManager.stopPresentation(false)
                }

                is TransferEvent.Redirect -> TODO()
            }
        }
        transferManager.startQrEngagement()
    }

    private suspend fun handleRequestObject(deviceRequest: DeviceRequest) {
        val presentationDefinition = createPresentationDefinition(deviceRequest)
        val documentMatches = presentationDefinition.getDocumentMatches(documentRepository.documents.first())
        when (val match = documentMatches.second) {
            is Match.NotMatched -> {
                transferManager.stopPresentation(true)
                sendEffect { Effect.ProximityRequestNoMatch }
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
                    val document = claims.first { it.uniqueId == candidateClaimId }.credentialDocument as CredentialDocument.MDocDocument
                    val fields = match.fields(
                        candidateClaimId,
                        false,
                        document.type
                    )
                    val optionalFields = match.fields(candidateClaimId, true, document.type)
                    if (!fields.isEmpty() || !optionalFields.isEmpty()) {
                        val credential = Credential(
                            credentialType = document.credentialType(),
                            fields = fields,
                            optionalFields = optionalFields,
                            attestation = document.attestation,
                            mDoc = document.mDoc
                        )
                        credentials.add(credential)
                    }
                }

                setState { copy(credentials = credentials, sessionTranscript = deviceRequest.sessionTranscriptBytes) }
                sendEffect { Effect.ProximityRequest }
            }
        }
    }

    private fun PresentationDefinition.getDocumentMatches(documents: List<CredentialDocument>): Pair<List<CredentialClaim>, Match> =
        openId4VPManager.matches(this, documents)

    private fun createPresentationDefinition(deviceRequest: DeviceRequest): PresentationDefinition {
        val requestedDocuments = DeviceRequestParser(deviceRequest.deviceRequestBytes, deviceRequest.sessionTranscriptBytes)
            .parse()
        val inputDescriptors = requestedDocuments.docRequests
            .map { requestedDocument -> requestedDocument.docType to requestedDocument.requestMap }
            .flatMap { (docType, requestMap) ->
                requestMap.map { d ->
                    val fieldConstraints = d.value
                        .map { (fieldName, intentToRetain) ->
                            FieldConstraint(paths = listOf(JsonPath.jsonPath("\$['${d.key}']['$fieldName']")!!), intentToRetain = intentToRetain)
                        }
                    InputDescriptor(
                        id = InputDescriptorId(UUID.randomUUID().toString()),
                        constraints = Constraints.of(fs = fieldConstraints, limitDisclosure = LimitDisclosure.PREFERRED)!!
                    )
                }
            }

        return PresentationDefinition(
            id = Id(UUID.randomUUID().toString()),
            inputDescriptors = inputDescriptors
        )
    }

    private fun onOptionalFieldChange(field: MatchedField, checked: Boolean) {
        updateState {
            copy(
                credentials = credentials.map { credential ->
                    credential.copy(
                        fields = credential.fields.map {
                            if (field == it && it.checked != checked) {
                                it.copy(checked = checked)
                            } else {
                                it
                            }
                        },
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

    override fun initialState(): UiState {
        return UiState()
    }

    override suspend fun handleEvents(event: Event) {
        when (event) {
            is Event.OnShareClicked -> {
                val responseDocuments = mutableListOf<MDoc>()
                state.value.credentials.forEach { credential ->
                    val mDoc = credential.mDoc
                    val fields = credential.allCheckedFields.map { it.field }
                    val optionalFields = credential.optionalFields.map { it.field }
                    val docType = credential.credentialType.docType().uri
                    val mDocRequest = MDocRequestBuilder(docType).apply {
                        fields.forEach {
                            addDataElementRequest(it.namespace.uri, it.name, true)
                        }
                        optionalFields.forEach {
                            addDataElementRequest(it.namespace.uri, it.name, true)
                        }
                    }.build(null)
                    val cryptoProvider = cryptoProviderFactory.forKeyType(credential.attestation.keyAttestation.keyType)
                    val keyId = credential.attestation.keyAttestation.keyId
                    val deviceNameSpaces = EncodedCBORElement(MapElement(mapOf()))
                    val sessionTranscript = DataElement.fromCBOR<ListElement>(state.value.sessionTranscript!!)
                    val deviceAuthentication = DeviceAuthentication(sessionTranscript, docType, deviceNameSpaces)
                    val documentResponse = mDoc.presentWithDeviceSignature(
                        mDocRequest = mDocRequest,
                        deviceAuthentication = deviceAuthentication,
                        cryptoProvider = cryptoProvider.deviceCryptoProvider(keyId),
                        keyID = keyId
                    )
                    responseDocuments.add(documentResponse)
                }

                val response = eu.europa.ec.eudi.iso18013.transfer.response.device.DeviceResponse(
                    deviceResponseBytes = ee.cyber.wallet.domain.documents.mdoc.DeviceResponse(responseDocuments).toCBOR(),
                    sessionTranscriptBytes = byteArrayOf(),
                    documentIds = listOf()
                )
                transferManager.sendResponse(response)
            }

            Event.OnCancelClicked -> {
                transferManager.stopPresentation(false)
                sendEffect { Effect.ProximityCancel }
            }
            is Event.OnOptionalFieldChange -> onOptionalFieldChange(event.field, event.checked)
            Event.OnBleModeToggle -> {
                viewModelScope.launch {
                    userPreferencesDataSource.setBlePeripheralMode(!state.value.isPeripheralMode)
                }
                transferManager.stopPresentation(false)
            }
        }
    }
}

@Parcelize
data class Credential(
    val credentialType: CredentialType = CredentialType.PID_SD_JWT,
    val fields: MatchedFields = listOf(),
    val optionalFields: MatchedFields = listOf(),
    val attestation: @RawValue Attestation,
    val mDoc: @RawValue MDoc
) : Parcelable {
    private val allFields: MatchedFields
        get() = fields + optionalFields

    val allCheckedFields: MatchedFields
        get() = allFields.filter { it.checked }
}

@Parcelize
data class UiState(
    val verifier: String = "",
    val sessionTranscript: ByteArray? = null,
    val qrCodeBitmap: Bitmap? = null,
    val credentials: List<Credential> = listOf(),
    val shareDisabled: Boolean = false,
    val isPeripheralMode: Boolean = true
) : ViewState, Parcelable

sealed class Effect : ViewSideEffect {
    data object ProximityRequest : Effect()
    data object ProximityRequestNoMatch : Effect()

    data object ProximityResponseSent : Effect()
    data object ProximityCancel : Effect()
}

sealed class Event : ViewEvent {
    data class OnOptionalFieldChange(val field: MatchedField, val checked: Boolean) : Event()
    data object OnShareClicked : Event()
    data object OnCancelClicked : Event()
    data object OnBleModeToggle : Event()
}
