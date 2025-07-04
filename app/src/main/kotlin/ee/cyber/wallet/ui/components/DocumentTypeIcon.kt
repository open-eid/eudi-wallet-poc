package ee.cyber.wallet.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCarFilled
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ee.cyber.wallet.domain.credentials.DocType
import ee.cyber.wallet.ui.theme.PreviewThemes
import ee.cyber.wallet.ui.theme.WalletThemePreviewSurface
import ee.cyber.wallet.ui.theme.colorsBlueBlue300
import ee.cyber.wallet.ui.theme.pink
import ee.cyber.wallet.ui.theme.green_600

@Composable
@PreviewThemes
private fun DocumentTypeIconPreview() {
    WalletThemePreviewSurface {
        Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DocumentTypeIcon(docType = DocType.PID_SD_JWT)
            DocumentTypeIcon(docType = DocType.PID)
            DocumentTypeIcon(docType = DocType.MDL)
            DocumentTypeIcon(docType = DocType.AGE_VERIFICATION)
        }
    }
}

@Composable
fun DocumentTypeIcon(docType: DocType) {
    val color = when (docType) {
        DocType.MDL -> pink
        DocType.AGE_VERIFICATION -> green_600
        else -> colorsBlueBlue300
    }
    Card(
        modifier = Modifier
            .width(40.dp)
            .height(32.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        shape = MaterialTheme.shapes.small
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.padding(2.dp),
                imageVector = when (docType) {
                    DocType.MDL -> Icons.Filled.DirectionsCarFilled
                    DocType.AGE_VERIFICATION -> Icons.Filled.VerifiedUser
                    else -> Icons.Filled.Person
                },
                contentDescription = "",
                tint = color
            )
        }
    }
}
