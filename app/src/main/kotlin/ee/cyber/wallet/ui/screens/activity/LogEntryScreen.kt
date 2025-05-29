package ee.cyber.wallet.ui.screens.activity

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ee.cyber.wallet.R
import ee.cyber.wallet.domain.AppError
import ee.cyber.wallet.domain.credentials.CredentialAttribute
import ee.cyber.wallet.domain.credentials.DocType
import ee.cyber.wallet.ui.components.AppContent
import ee.cyber.wallet.ui.components.DocumentCardHeader
import ee.cyber.wallet.ui.components.HDivider
import ee.cyber.wallet.ui.components.HSpace
import ee.cyber.wallet.ui.components.SimpleNavigationHeader
import ee.cyber.wallet.ui.components.VSpace
import ee.cyber.wallet.ui.screens.documents.label
import ee.cyber.wallet.ui.theme.PreviewThemes
import ee.cyber.wallet.ui.theme.PreviewThemesSmallScreen
import ee.cyber.wallet.ui.theme.WalletThemePreviewSurface
import ee.cyber.wallet.ui.theme.green_600
import ee.cyber.wallet.ui.theme.red_600
import ee.cyber.wallet.ui.util.ContentAlpha
import ee.cyber.wallet.ui.util.ContentColor
import ee.cyber.wallet.ui.util.format
import kotlinx.datetime.Clock

@Composable
@PreviewThemesSmallScreen
private fun TransactionLogScreenPreview() {
    WalletThemePreviewSurface {
        LogEntryScreenContent(
            UiState.Success(
                LogEntryModel(
                    id = 0,
                    date = Clock.System.now(),
                    party = "Swedbank",
                    type = DocType.PID,
                    attributes = mapOf(
                        CredentialAttribute.JWT_PID_1_PERSONAL_ADMINISTRATIVE_NUMBER to "PNOEE-38001085718",
                        CredentialAttribute.JWT_PID_1_GIVEN_NAME to "JAAK-KRISTJAN",
                        CredentialAttribute.JWT_PID_1_FAMILY_NAME to "JÕEORG",
                        CredentialAttribute.JWT_PID_1_BIRTHDATE to "1980-01-08"

                    )
                )
            )
        )
    }
}

@Composable
@PreviewThemes
private fun FailedTransactionLogScreenPreview() {
    WalletThemePreviewSurface {
        LogEntryScreenContent(
            UiState.Success(
                LogEntryModel(
                    id = 0,
                    date = Clock.System.now(),
                    party = "Swedbank",
                    type = DocType.PID,
                    attributes = mapOf(
                        CredentialAttribute.JWT_PID_1_PERSONAL_ADMINISTRATIVE_NUMBER to "PNOEE-38001085718",
                        CredentialAttribute.JWT_PID_1_GIVEN_NAME to "JAAK-KRISTJAN",
                        CredentialAttribute.JWT_PID_1_FAMILY_NAME to "JÕEORG",
                        CredentialAttribute.JWT_PID_1_BIRTHDATE to "1980-01-08"
                    ),
                    error = AppError.PRESENTATION_MATCH_ERROR
                )
            )
        )
    }
}

@Composable
@PreviewThemes
private fun FailedRequestedLogEntryScreenPreview() {
    WalletThemePreviewSurface {
        LogEntryScreenContent(
            UiState.Success(
                LogEntryModel(
                    id = 0,
                    date = Clock.System.now(),
                    party = "Swedbank",
                    type = DocType.PID,
                    attributes = mapOf(
                        CredentialAttribute.JWT_PID_1_PERSONAL_ADMINISTRATIVE_NUMBER to null,
                        CredentialAttribute.JWT_PID_1_GIVEN_NAME to null,
                        CredentialAttribute.JWT_PID_1_FAMILY_NAME to null,
                        CredentialAttribute.JWT_PID_1_BIRTHDATE to null
                    ),
                    error = AppError.PRESENTATION_MATCH_ERROR
                )
            )
        )
    }
}

@Composable
fun LogEntryScreen(viewModel: LogEntryScreenViewModel, onNavigateBack: () -> Unit = {}) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    when (state) {
        is UiState.Success -> {
            LogEntryScreenContent(state as UiState.Success, onNavigateBack = onNavigateBack)
        }

        UiState.Loading -> {}
    }
}

@Composable
private fun LogEntryScreenContent(state: UiState.Success, onNavigateBack: () -> Unit = {}) {
    AppContent(
        header = { SimpleNavigationHeader(title = stringResource(R.string.log_entry_title), onNavigateBack = onNavigateBack) }
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            ContentAlpha(0.6f) {
                Text(text = stringResource(R.string.log_entry_request_title).uppercase(), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
            }
            VSpace(24)
            Card {
                SimpleRequestItem(stringResource(R.string.log_entry_relying_party), state.logEntry.party)
                HDivider()
                SimpleRequestItem(stringResource(R.string.log_entry_presentation_time), state.logEntry.date.format())
                HDivider()
                ResultRequestItem(state.logEntry.error)
            }
            if (state.logEntry.attributes.isNotEmpty()) {
                VSpace(24)
                ContentAlpha(0.6f) {
                    Text(
                        text = if (state.logEntry.attributes.any { it.value?.isNotBlank() == true }) {
                            stringResource(R.string.log_entry_presented_data).uppercase()
                        } else {
                            stringResource(R.string.log_entry_requested_data).uppercase()
                        },
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                VSpace(24)
                Card {
                    DocumentCardHeader(state.logEntry.type)
                    state.logEntry.attributes.forEach {
                        HDivider()
                        if (it.key == CredentialAttribute.ORG_ISO_18013_5_1_PORTRAIT ||
                            it.key == CredentialAttribute.ORG_ISO_18013_5_1_SIGNATURE_USUAL_MARK
                        ) {
                            SimpleRequestItem(it.key.label(), it.value?.let { stringResource(R.string.presentation_image_stub) })
                        } else {
                            SimpleRequestItem(it.key.label(), it.value)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestItem(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        ContentAlpha(0.6f) {
            Text(text = title)
        }
        content()
    }
}

@Composable
private fun SimpleRequestItem(title: String, value: String?) =
    RequestItem(title = title) {
        Text(text = value ?: "-", fontWeight = FontWeight.Bold)
    }

@Composable
private fun ResultRequestItem(error: AppError? = null) = RequestItem(title = stringResource(R.string.log_entry_data_exchange)) {
    ContentColor(if (error != null) red_600 else green_600) {
        Column {
            Row {
                Text(
                    text = if (error != null) {
                        stringResource(R.string.log_entry_failed)
                    } else {
                        stringResource(R.string.log_entry_successful)
                    },
                    fontWeight = FontWeight.Bold
                )
                HSpace(6)
                Icon(imageVector = if (error != null) Icons.Filled.Cancel else Icons.Rounded.CheckCircle, contentDescription = "")
            }
            if (error != null) Text(text = stringResource(error.resId), style = MaterialTheme.typography.bodySmall)
        }
    }
}
