package ee.cyber.wallet.ui.screens.boarding

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
import androidx.compose.material.icons.rounded.CheckCircleOutline
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
import ee.cyber.wallet.domain.credentials.DocType
import ee.cyber.wallet.ui.components.AppContent
import ee.cyber.wallet.ui.components.DocumentCardHeader
import ee.cyber.wallet.ui.components.HDivider
import ee.cyber.wallet.ui.components.PrimaryButton
import ee.cyber.wallet.ui.components.SecondaryButton
import ee.cyber.wallet.ui.components.VSpace
import ee.cyber.wallet.ui.components.VerifiedParty
import ee.cyber.wallet.ui.components.WSpace

@Composable
fun PidAuthenticationScreen(onShare: () -> Unit = {}, onCancel: () -> Unit = {}, viewModel: PidAuthenticationViewModel) {
    Box(Modifier.fillMaxSize()) {
        AppContent {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "tara.ria.ee",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold
                )
                VerifiedParty()
                VSpace(24.dp)
                Text(
                    style = MaterialTheme.typography.bodyMedium,
                    text = stringResource(R.string.presentation_requests_following_data_to),
                    textAlign = TextAlign.Center
                )
                VSpace(24.dp)
                Card(modifier = Modifier.padding(bottom = 16.dp)) {
                    DocumentCardHeader(docType = DocType.PID_SD_JWT)
                    HDivider()
                    Column {
                        Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(text = stringResource(R.string.attr_identification_nr), style = MaterialTheme.typography.bodySmall)
                                Text(text = viewModel.state.value.pid, style = MaterialTheme.typography.titleMedium)
                            }
                            WSpace()
                            Icon(imageVector = Icons.Rounded.CheckCircleOutline, contentDescription = "", tint = MaterialTheme.colorScheme.primary)
                        }
                        HDivider()
                    }
                }
                VSpace(24.dp)
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    text = stringResource(R.string.presentation_footer_note, "tara.ria.ee"),
                    textAlign = TextAlign.Center
                )
                VSpace(24.dp)
                WSpace()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally)
                ) {
                    SecondaryButton(text = stringResource(R.string.cancel_btn), onClick = onCancel)
                    PrimaryButton(text = stringResource(R.string.share_btn), enabled = true, onClick = onShare)
                }
            }
        }
    }
}
