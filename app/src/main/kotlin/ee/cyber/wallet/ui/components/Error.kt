package ee.cyber.wallet.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ee.cyber.wallet.R
import ee.cyber.wallet.ui.theme.PreviewThemes
import ee.cyber.wallet.ui.theme.WalletThemePreviewSurface
import ee.cyber.wallet.ui.theme.yellow_600

@Composable
@PreviewThemes
private fun ErrorPreview() {
    WalletThemePreviewSurface {
        ErrorCard(message = "Unable to establish communication with the Relying Party!")
    }
}

@Composable
fun ErrorCard(message: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(imageVector = Icons.Outlined.WarningAmber, contentDescription = "", tint = yellow_600)
            HSpace(16.dp)
            Column {
                Text(text = message, style = MaterialTheme.typography.titleMedium)
                VSpace(8.dp)
                Text(text = stringResource(R.string.presentation_close_and_try_again), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
