package ee.cyber.wallet.ui.screens.crossdevice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ee.cyber.wallet.R
import ee.cyber.wallet.ui.components.AppContent
import ee.cyber.wallet.ui.components.HDivider
import ee.cyber.wallet.ui.components.HSpace
import ee.cyber.wallet.ui.components.PrimaryButton
import ee.cyber.wallet.ui.components.VSpace
import ee.cyber.wallet.ui.theme.PreviewThemes
import ee.cyber.wallet.ui.theme.WalletThemePreviewSurface

@Composable
@PreviewThemes
private fun CrossDeviceTutorialScreenPreview() = WalletThemePreviewSurface { CrossDeviceTutorialScreen() }

private val items = listOf(
    R.string.cross_tutorial_item1,
    R.string.cross_tutorial_item2,
    R.string.cross_tutorial_item3,
    R.string.cross_tutorial_item4
)

@Composable
fun CrossDeviceTutorialScreen(onBack: () -> Unit = {}, onQRCodeOpen: () -> Unit = {}) {
    AppContent(
        header = {
            Row(Modifier.fillMaxWidth()) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "")
                }
            }
        }
    ) {
        Text(text = stringResource(R.string.cross_tutorial_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        VSpace(24.dp)

        Card {
            items.forEachIndexed { index, item ->
                TutorialItem(
                    num = (index + 1).toString(),
                    text = stringResource(item)
                )
                if (index + 1 < items.size) HDivider()
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                PrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.cross_tutorial_open_reader),
                    onClick = onQRCodeOpen
                )
            }
            VSpace(16.dp)
        }
    }
}

@Composable
private fun TutorialItem(num: String, text: String) =
    Row(Modifier.padding(16.dp)) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(color = MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = num,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        HSpace(16.dp)
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
