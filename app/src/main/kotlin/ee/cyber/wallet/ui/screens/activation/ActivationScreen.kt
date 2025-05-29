package ee.cyber.wallet.ui.screens.activation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ee.cyber.wallet.R
import ee.cyber.wallet.ui.components.AppContent
import ee.cyber.wallet.ui.components.VSpace
import ee.cyber.wallet.ui.theme.PreviewThemes
import ee.cyber.wallet.ui.theme.WalletThemePreviewSurface

typealias ActivationWith = (ActivationFlow) -> Unit

enum class ActivationFlow {
    SAME_DEVICE,
    CROSS_DEVICE
}

@Composable
@PreviewThemes
private fun ActivationScreenPreview() {
    WalletThemePreviewSurface {
        ActivationScreenContent {
        }
    }
}

@Composable
fun ActivationScreen(onActivationWith: ActivationWith) {
    ActivationScreenContent(onActivationWith)
}

@Composable
private fun ActivationScreenContent(onActivationWith: ActivationWith) {
    AppContent {
        Text(
            text = stringResource(R.string.activation_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold
        )
        VSpace(24.dp)
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = stringResource(R.string.activation_description),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        VSpace(24.dp)
        ActivationItem(
            title = stringResource(R.string.activation_same_device_title),
            description = stringResource(R.string.activation_same_device_description),
            onClick = { onActivationWith(ActivationFlow.SAME_DEVICE) }
        )
        VSpace(16.dp)
        ActivationItem(
            title = stringResource(R.string.activation_cross_device_title),
            description = stringResource(R.string.activation_cross_device_description),
            onClick = { onActivationWith(ActivationFlow.CROSS_DEVICE) }
        )
    }
}

@Composable
private fun ActivationItem(title: String, description: String, onClick: () -> Unit = {}) {
    Card(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                VSpace(4.dp)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "", tint = MaterialTheme.colorScheme.primary)
        }
    }
}
