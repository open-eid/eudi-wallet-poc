package ee.cyber.wallet.ui.screens.documents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ee.cyber.wallet.R
import ee.cyber.wallet.domain.credentials.CredentialType
import ee.cyber.wallet.domain.documents.CredentialDocument
import ee.cyber.wallet.domain.documents.DocumentField
import ee.cyber.wallet.ui.components.AppContent
import ee.cyber.wallet.ui.components.DocumentTypeIcon
import ee.cyber.wallet.ui.components.HSpace
import ee.cyber.wallet.ui.components.SimpleNavigationHeader
import ee.cyber.wallet.ui.theme.PreviewThemes
import ee.cyber.wallet.ui.theme.WalletThemePreviewSurface
import ee.cyber.wallet.ui.util.ContentAlpha
import ee.cyber.wallet.ui.util.document

private val fakeDocuments = listOf(
    document(
        CredentialType.PID_SD_JWT,
        listOf(
            DocumentField.pidField("sub", "3145435"),
            DocumentField.pidField("name", "ewkjhbfkwefbj"),
            DocumentField.pidField("name 2", "akefgi4utf")
        )
    ),
    document(
        CredentialType.MDL,
        listOf(
            DocumentField.pidField("sub", "3145435"),
            DocumentField.pidField("name", "ewkjhbfkwefbj"),
            DocumentField.pidField("name 2", "akefgi4utf")
        )
    )
)

@Composable
@PreviewThemes
private fun MyDocumentsPreview() {
    WalletThemePreviewSurface {
        MyDocuments(UiState(fakeDocuments))
    }
}

@Composable
@PreviewThemes
private fun MyDocumentsEmptyPreview() {
    WalletThemePreviewSurface {
        MyDocuments(UiState(listOf()))
    }
}

@Composable
fun MyDocumentsScreen(
    viewModel: MyDocumentsViewModel,
    onDocumentClicked: (documentId: String) -> Unit,
    onAddDocument: (CredentialType) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val state by viewModel.state
    MyDocuments(
        state = state,
        onDocumentClicked = onDocumentClicked,
        onAddDocument = onAddDocument,
        onBack = onBack
    )
}

@Composable
private fun MyDocuments(
    state: UiState,
    onDocumentClicked: (documentId: String) -> Unit = {},
    onAddDocument: (docType: CredentialType) -> Unit = {},
    onBack: () -> Unit = {}
) {
    var isSheetVisible by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            if (state.supportedTypes.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    onClick = { isSheetVisible = true },
                    icon = { Icon(imageVector = Icons.Filled.Add, contentDescription = "") },
                    text = { Text(text = stringResource(R.string.my_documents_add_new_document)) }
                )
            }
        }
    ) { padding ->
        AppContent(
            modifier = Modifier.padding(padding),
            header = {
                SimpleNavigationHeader(
                    title = stringResource(R.string.my_documents_title),
                    onNavigateBack = onBack
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (state.documents.isEmpty()) {
                    EmptyList()
                } else {
                    MyDocumentsContent(
                        state = state,
                        onDocumentClicked = onDocumentClicked
                    )
                }
            }

            if (isSheetVisible) {
                DocumentTypesModalBottomSheet(
                    supportedTypes = state.supportedTypes,
                    onDismiss = { isSheetVisible = false },
                    onDocumentTypeSelected = {
                        isSheetVisible = false
                        onAddDocument(it)
                    }
                )
            }
        }
    }
}

@Composable
private fun DocumentTypesModalBottomSheet(
    supportedTypes: List<CredentialType>,
    onDismiss: () -> Unit = {},
    onDocumentTypeSelected: (docType: CredentialType) -> Unit = {}
) {
    ModalBottomSheet(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        dragHandle = {},
        onDismissRequest = onDismiss
    ) {
        DocumentTypes(supportedTypes, onDocumentTypeSelected)
    }
}

@Composable
private fun DocumentTypes(supportedTypes: List<CredentialType>, onAddDocument: (docType: CredentialType) -> Unit = {}) {
    Column(Modifier.padding(16.dp)) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            text = "Add document",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium
        )
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                if (supportedTypes.contains(CredentialType.PID_SD_JWT)) DocumentTypeItem(credentialType = CredentialType.PID_SD_JWT) { onAddDocument(CredentialType.PID_SD_JWT) }
                if (supportedTypes.contains(CredentialType.PID_MDOC)) DocumentTypeItem(credentialType = CredentialType.PID_MDOC) { onAddDocument(CredentialType.PID_MDOC) }
                if (supportedTypes.contains(CredentialType.MDL)) DocumentTypeItem(credentialType = CredentialType.MDL) { onAddDocument(CredentialType.MDL) }
            }
        }
    }
}

@Composable
private fun EmptyList() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        ContentAlpha(0.6f) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = stringResource(R.string.my_documents_no_documents_found), textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun MyDocumentsContent(
    state: UiState,
    onDocumentClicked: (documentId: String) -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        state.documents.forEach {
            DocumentCard(it, onDocumentClicked = onDocumentClicked)
        }
    }
}

@Composable
private fun DocumentTypeItem(credentialType: CredentialType, onClick: () -> Unit = {}) {
    Card(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DocumentTypeIcon(docType = credentialType.docType())
                HSpace(8.dp)
                Column(
                    Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = credentialType.docTypeNameWithFormat(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(text = credentialType.docType().docTypeDescription())
                }
            }
        }
    }
}

@Composable
private fun DocumentCard(
    document: CredentialDocument,
    onDocumentClicked: (documentId: String) -> Unit
) {
    Card(onClick = { onDocumentClicked(document.id) }) {
        DocumentContent(document)
    }
}

@Composable
private fun DocumentContent(document: CredentialDocument) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            DocumentTypeIcon(docType = document.type)
            HSpace(8.dp)
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            ) {
                Text(
                    text = document.credentialType().docTypeNameWithFormat(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = if (document.expired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                ContentAlpha(0.6f) {
                    Text(
                        text = document.type.docTypeDescription(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            ContentAlpha(0.6f) {
                Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "")
            }
        }
    }
}
