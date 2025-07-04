package ee.cyber.wallet.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ee.cyber.wallet.domain.credentials.CredentialType
import ee.cyber.wallet.domain.credentials.DocType
import ee.cyber.wallet.ui.screens.documents.docType
import ee.cyber.wallet.ui.screens.documents.docTypeName
import ee.cyber.wallet.ui.screens.documents.docTypeNameWithFormat

@Composable
fun DocumentCardHeader(credentialType: CredentialType) {
    Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        DocumentTypeIcon(docType = credentialType.docType())
        HSpace(8.dp)
        Text(text = credentialType.docTypeNameWithFormat(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DocumentCardHeader(docType: DocType) {
    Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        DocumentTypeIcon(docType = docType)
        HSpace(8.dp)
        Text(text = docType.docTypeName(), style = MaterialTheme.typography.titleMedium)
    }
}
