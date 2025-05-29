package ee.cyber.wallet.ui.screens.document

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.cyber.wallet.data.repository.DocumentRepository
import ee.cyber.wallet.domain.credentials.CredentialAttribute
import ee.cyber.wallet.domain.credentials.CredentialType
import ee.cyber.wallet.domain.documents.CredentialDocument
import ee.cyber.wallet.ui.mvi.MviViewModel
import ee.cyber.wallet.ui.mvi.ViewEvent
import ee.cyber.wallet.ui.mvi.ViewSideEffect
import ee.cyber.wallet.ui.mvi.ViewState
import ee.cyber.wallet.ui.screens.documents.credentialType
import ee.cyber.wallet.ui.util.QRCode
import ee.cyber.wallet.ui.util.document
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.slf4j.LoggerFactory
import javax.inject.Inject

data class DocumentView(
    val document: CredentialDocument,
    val deletable: Boolean
)

data class DocumentUiState(
    val document: DocumentView? = null,
    val qrCodeBitmap: Bitmap? = null,
    val isLoading: Boolean = false
) : ViewState

sealed class DocumentEvent : ViewEvent {
    data object DeleteWithConfirmation : DocumentEvent()
    data object Delete : DocumentEvent()
}

sealed class DocumentSideEffect : ViewSideEffect {
    data object GoBack : DocumentSideEffect()
    data object ShowDeleteConfirmation : DocumentSideEffect()
}

private val emptyDocument = DocumentView(document(CredentialType.PID_SD_JWT), false)

@HiltViewModel
class DocumentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val documentRepository: DocumentRepository
) : MviViewModel<DocumentEvent, DocumentUiState, DocumentSideEffect>() {

    private val logger = LoggerFactory.getLogger("DocumentViewModel")

    private val documentId = requireNotNull(savedStateHandle.get<String>("documentId"))

    init {
        viewModelScope.launch {
            documentRepository.getDocumentById(documentId).collect {
                setState { copy(document = it?.let { doc -> DocumentView(doc, doc.credentialType() == CredentialType.MDL) }) }
                generateQrCodeIfNeeded(it)
            }
        }
    }

    private suspend fun generateQrCodeIfNeeded(document: CredentialDocument?) {
        if (document != null && document is CredentialDocument.JwtDocument) {
            viewModelScope.launch {
                val qrCodeBitmap = buildJsonObject {
                    document.fields.forEach {
                        if (it.name != CredentialAttribute.ORG_ISO_18013_5_1_PORTRAIT.fieldName &&
                            it.name != CredentialAttribute.MDOC_PID_1_PORTRAIT.fieldName &&
                            it.name != CredentialAttribute.JWT_PID_1_PICTURE.fieldName &&
                            it.name != CredentialAttribute.ORG_ISO_18013_5_1_BIOMETRIC_TEMPLATE_FACE.fieldName &&
                            it.name != CredentialAttribute.ORG_ISO_18013_5_1_BIOMETRIC_TEMPLATE_FINGER.fieldName &&
                            it.name != CredentialAttribute.ORG_ISO_18013_5_1_BIOMETRIC_TEMPLATE_SIGNATURE_SIGN.fieldName &&
                            it.name != CredentialAttribute.ORG_ISO_18013_5_1_BIOMETRIC_TEMPLATE_IRIS.fieldName
                        ) {
                            put(it.name, it.value)
                        }
                    }
                }.let { QRCode.createQrCode(it.toString()) }
                setState { copy(qrCodeBitmap = qrCodeBitmap) }
            }
        }
    }

    override fun initialState(): DocumentUiState = DocumentUiState(emptyDocument)

    override suspend fun handleEvents(event: DocumentEvent) {
        when (event) {
            DocumentEvent.DeleteWithConfirmation -> sendEffect { DocumentSideEffect.ShowDeleteConfirmation }
            DocumentEvent.Delete -> deleteDocument()
        }
    }

    private suspend fun deleteDocument() {
        setLoading(true)
        runCatching {
            documentRepository.deleteDocument(documentId)
            setState { copy(document = null, qrCodeBitmap = null) }
        }.onFailure { logger.error("Failed to delete document", it) }
        setLoading(false)
    }

    private fun setLoading(loading: Boolean) {
        setState {
            copy(isLoading = loading)
        }
    }
}
