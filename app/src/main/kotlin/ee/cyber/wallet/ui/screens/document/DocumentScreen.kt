package ee.cyber.wallet.ui.screens.document

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ee.cyber.wallet.R
import ee.cyber.wallet.domain.credentials.CredentialAttribute
import ee.cyber.wallet.domain.credentials.CredentialType
import ee.cyber.wallet.domain.documents.DocumentField
import ee.cyber.wallet.ui.components.AppContent
import ee.cyber.wallet.ui.components.ConfirmationDialog
import ee.cyber.wallet.ui.components.MenuItem
import ee.cyber.wallet.ui.components.SimpleNavigationHeader
import ee.cyber.wallet.ui.components.VSpace
import ee.cyber.wallet.ui.screens.documents.credentialType
import ee.cyber.wallet.ui.screens.documents.docTypeNameWithFormat
import ee.cyber.wallet.ui.theme.PreviewThemes
import ee.cyber.wallet.ui.theme.WalletThemePreviewSurface
import ee.cyber.wallet.ui.util.document
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

@Composable
@PreviewThemes
private fun DocumentPreview() {
    WalletThemePreviewSurface {
        DocumentScreenContent(
            document = DocumentView(
                document = document(
                    credentialType = CredentialType.PID_SD_JWT,
                    fields = listOf(
                        DocumentField.fromAttribute(CredentialAttribute.JWT_PID_1_PERSONAL_ADMINISTRATIVE_NUMBER, "38001085718"),
                        DocumentField.fromAttribute(CredentialAttribute.JWT_PID_1_GIVEN_NAME, "JAAK-KRISTJAN"),
                        DocumentField.fromAttribute(CredentialAttribute.JWT_PID_1_FAMILY_NAME, "JÕEORG"),
                        DocumentField.fromAttribute(CredentialAttribute.JWT_PID_1_BIRTHDATE, "1980-01-08"),
                        DocumentField.fromAttribute(CredentialAttribute.JWT_PID_1_PERSONAL_ADMINISTRATIVE_NUMBER, "38001085718"),
                        DocumentField.fromAttribute(CredentialAttribute.JWT_PID_1_GIVEN_NAME, "JAAK-KRISTJAN")
                    )
                ),
                deletable = false
            )
        )
    }
}

@Composable
fun DocumentScreen(viewModel: DocumentViewModel, onBack: () -> Unit) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effect.onEach { effect ->
            when (effect) {
                is DocumentSideEffect.GoBack -> onBack()
                DocumentSideEffect.ShowDeleteConfirmation -> showDeleteConfirmation = true
            }
        }.collect()
    }

    val state = viewModel.state.value

    LaunchedEffect(state.document) {
        if (state.document == null) {
            onBack()
        }
    }
    if (state.document != null) {
        DocumentScreenContent(
            document = state.document,
            qrCodeBitmap = state.qrCodeBitmap,
            onBack = onBack,
            onDeleteClicked = { viewModel.sendEvent(DocumentEvent.DeleteWithConfirmation) }
        )
    }

    if (showDeleteConfirmation) {
        ConfirmationDialog(
            title = stringResource(R.string.document_delete_confirmation_title),
            text = stringResource(R.string.document_delete_confirmation_text),
            confirmButtonText = stringResource(R.string.delete_btn),
            onConfirm = {
                showDeleteConfirmation = false
                viewModel.sendEvent(DocumentEvent.Delete)
            },
            cancelButtonText = stringResource(R.string.cancel_btn),
            onCancel = {
                showDeleteConfirmation = false
            }
        )
    }
}

@Composable
private fun DocumentScreenContent(document: DocumentView, qrCodeBitmap: Bitmap? = null, onBack: () -> Unit = {}, onDeleteClicked: () -> Unit = {}) {
    val menuItems = remember(document) {
        if (document.deletable) {
            listOf(
                MenuItem(
                    title = "Delete",
                    onClick = onDeleteClicked,
                    icon = {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                    }
                )
            )
        } else {
            listOf()
        }
    }
    AppContent(
        header = {
            SimpleNavigationHeader(
                title = document.document.credentialType().docTypeNameWithFormat(),
                menuItems = menuItems,
                onNavigateBack = onBack
            )
        }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DocumentCardView(document.document)
            val qrCode by remember(qrCodeBitmap) {
                derivedStateOf {
                    qrCodeBitmap?.asImageBitmap()
                }
            }
            if (qrCode != null) {
                VSpace(24)
                Card {
                    Column(
                        Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.document_qr_code_title),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Image(bitmap = qrCode!!, contentDescription = "")
                    }
                }
            }
        }
    }
}
