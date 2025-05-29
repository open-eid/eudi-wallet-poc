package ee.cyber.wallet.ui.screens.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ee.cyber.wallet.R
import ee.cyber.wallet.data.database.LogEntryEntity
import ee.cyber.wallet.domain.AppError
import ee.cyber.wallet.domain.credentials.DocType
import ee.cyber.wallet.ui.components.AppContent
import ee.cyber.wallet.ui.components.HSpace
import ee.cyber.wallet.ui.components.SimpleNavigationHeader
import ee.cyber.wallet.ui.components.VSpace
import ee.cyber.wallet.ui.screens.documents.docTypeName
import ee.cyber.wallet.ui.theme.PreviewThemes
import ee.cyber.wallet.ui.theme.WalletThemePreviewSurface
import ee.cyber.wallet.ui.theme.green_600
import ee.cyber.wallet.ui.theme.red_600
import ee.cyber.wallet.ui.util.ContentAlpha
import ee.cyber.wallet.ui.util.format
import kotlinx.datetime.Clock

@Composable
@PreviewThemes
private fun ActivityLogScreenPreview() {
    WalletThemePreviewSurface {
        ActivityLogScreenContent(
            listOf(
                LogEntryEntity(0, Clock.System.now(), "Swedbank", DocType.PID, error = AppError.PRESENTATION_INCORRECT_PIN_ERROR),
                LogEntryEntity(0, Clock.System.now(), "Swedbank", DocType.PID),
                LogEntryEntity(0, Clock.System.now(), "Car rental service", DocType.MDL),
                LogEntryEntity(0, Clock.System.now(), "mDL Issuer", DocType.PID),
                LogEntryEntity(0, Clock.System.now(), "mDL Issuer", DocType.PID, error = AppError.PRESENTATION_MATCH_ERROR),
                LogEntryEntity(0, Clock.System.now(), "Riigi Infosüsteemi Amet", DocType.PID, error = AppError.PRESENTATION_VERIFIER_REJECTED_ERROR),
                LogEntryEntity(0, Clock.System.now(), "Swedbank", DocType.PID, error = AppError.PRESENTATION_INCORRECT_PIN_ERROR),
                LogEntryEntity(0, Clock.System.now(), "Swedbank", DocType.PID),
                LogEntryEntity(0, Clock.System.now(), "Car rental service", DocType.MDL),
                LogEntryEntity(0, Clock.System.now(), "mDL Issuer", DocType.PID),
                LogEntryEntity(0, Clock.System.now(), "mDL Issuer", DocType.PID, error = AppError.PRESENTATION_MATCH_ERROR),
                LogEntryEntity(0, Clock.System.now(), "Riigi Infosüsteemi Amet", DocType.PID, error = AppError.PRESENTATION_VERIFIER_REJECTED_ERROR)
            )
        )
    }
}

@Composable
fun ActivityLogScreen(viewModel: ActivityLogViewModel, onItemClicked: (itemId: Long) -> Unit = {}, onNavigateBack: () -> Unit = {}) {
    val logs by viewModel.transactionLogs.collectAsStateWithLifecycle(initialValue = listOf())
    ActivityLogScreenContent(logs = logs, onItemClicked = onItemClicked, onNavigateBack = onNavigateBack)
}

@Composable
private fun ActivityLogScreenContent(logs: List<LogEntryEntity>, onItemClicked: (itemId: Long) -> Unit = {}, onNavigateBack: () -> Unit = {}) {
    AppContent(
        header = { SimpleNavigationHeader(title = stringResource(R.string.activity_log_title), onNavigateBack = onNavigateBack) }
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ContentAlpha(0.8f) {
                Text(text = stringResource(R.string.activity_log_description))
            }
            VSpace(24)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                logs.forEach {
                    ActivityItem(it) { onItemClicked(it.id) }
                }
            }
        }
    }
}

@Composable
private fun ActivityItem(it: LogEntryEntity, onClick: () -> Unit = {}) {
    Card(onClick = onClick) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painter = painterResource(R.drawable.dot), contentDescription = "", tint = if (it.error != null) red_600 else green_600)
            HSpace(8)
            Column(Modifier.weight(1f)) {
                Text(text = it.party, fontWeight = FontWeight.Bold)
                ContentAlpha(0.6f) {
                    Text(text = it.docType.docTypeName())
                    Text(text = it.date.format())
                }
            }
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "")
        }
    }
}
