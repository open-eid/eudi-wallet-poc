package ee.cyber.wallet.ui.screens.issuance

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ee.cyber.wallet.R
import ee.cyber.wallet.domain.AppError
import ee.cyber.wallet.domain.credentials.CredentialAttribute
import ee.cyber.wallet.domain.credentials.CredentialType
import ee.cyber.wallet.domain.documents.CredentialDocument
import ee.cyber.wallet.domain.documents.DocumentField
import ee.cyber.wallet.ui.components.AppContent
import ee.cyber.wallet.ui.components.FadedProgressIndicator
import ee.cyber.wallet.ui.components.PrimaryButton
import ee.cyber.wallet.ui.components.VSpace
import ee.cyber.wallet.ui.components.WSpace
import ee.cyber.wallet.ui.screens.document.DocumentCardView
import ee.cyber.wallet.ui.screens.documents.issuerName
import ee.cyber.wallet.ui.screens.pin.Input
import ee.cyber.wallet.ui.screens.pin.PinActivityResultContract
import ee.cyber.wallet.ui.screens.pin.PinFlow
import ee.cyber.wallet.ui.screens.pin.Result
import ee.cyber.wallet.ui.theme.PreviewThemes
import ee.cyber.wallet.ui.theme.WalletThemePreviewSurface
import ee.cyber.wallet.ui.util.document
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Composable
@PreviewThemes
private fun IssuanceScreenPreview() {
    WalletThemePreviewSurface {
        val document = document(
            CredentialType.PID_SD_JWT,
            listOf(
                DocumentField.fromAttribute(CredentialAttribute.JWT_PID_1_PERSONAL_ADMINISTRATIVE_NUMBER, "PNOEE-38001085718"),
                DocumentField.fromAttribute(CredentialAttribute.JWT_PID_1_GIVEN_NAME, "JAAK-KRISTJAN"),
                DocumentField.fromAttribute(CredentialAttribute.JWT_PID_1_FAMILY_NAME, "JÕEORG"),
                DocumentField.fromAttribute(CredentialAttribute.JWT_PID_1_BIRTHDATE, "1980-01-08")
            )
        )
        IssuanceContent(UiState(listOf(document), false))
    }
}

@Composable
@PreviewThemes
private fun IssuanceScreenLongPreview() {
    WalletThemePreviewSurface {
        val fields = mutableListOf<DocumentField>()
        repeat(4) { // for scrolling
            fields += listOf(
                DocumentField.fromAttribute(CredentialAttribute.JWT_PID_1_PERSONAL_ADMINISTRATIVE_NUMBER, "PNOEE-38001085718"),
                DocumentField.fromAttribute(CredentialAttribute.JWT_PID_1_GIVEN_NAME, "JAAK-KRISTJAN"),
                DocumentField.fromAttribute(CredentialAttribute.JWT_PID_1_FAMILY_NAME, "JÕEORG"),
                DocumentField.fromAttribute(CredentialAttribute.JWT_PID_1_BIRTHDATE, "1980-01-08")
            )
        }
        val document = document(CredentialType.PID_SD_JWT, fields)
        IssuanceContent(UiState(listOf(document), false))
    }
}

@Composable
@PreviewThemes
private fun IssuanceScreenLoadingPreview() {
    WalletThemePreviewSurface {
        IssuanceContent(UiState(listOf(), true))
    }
}

data class IssuanceScreenNavigationHandler(
    val onContinueClicked: () -> Unit,
    val onMockAuth: () -> Unit,
    val onOpenUri: (uri: String) -> Unit = {},
    val onBackClicked: () -> Unit = {},
    val showError: (error: AppError) -> Unit = {}
)

@Composable
fun IssuanceScreen(viewModel: IssuanceViewModel, navigationHandler: IssuanceScreenNavigationHandler) {
    val pinLauncher = rememberLauncherForActivityResult(PinActivityResultContract()) {
        when (it) {
            is Result.Success -> viewModel.sendEvent(Event.UserAuthenticated)
            Result.Cancelled, Result.Failure -> {
                navigationHandler.onBackClicked()
            }
        }
    }
    val state = viewModel.state.value
    val scope = rememberCoroutineScope()
    val pidIssuer = CredentialType.PID_SD_JWT.issuerName()
    val mdlIssuer = CredentialType.MDL.issuerName()

    LaunchedEffect(Unit) {
        viewModel.effect.onEach {
            when (it) {
                is Effect.ShowError -> scope.launch {
                    navigationHandler.showError(it.error)
                }

                is Effect.NavigateToUri -> {
                    navigationHandler.onOpenUri(it.uri)
                }

                Effect.Complete -> {
                    navigationHandler.onContinueClicked()
                }

                is Effect.AuthenticateWithPin -> {
                    pinLauncher.launch(Input(PinFlow.CONFIRM_ISSUANCE, if (it.credentialType == CredentialType.MDL) mdlIssuer else pidIssuer))
                }
                is Effect.AuthenticateWithMockAuthFlow -> {
                    navigationHandler.onMockAuth()
                }
            }
        }.collect()
    }

    IssuanceContent(
        state = state,
        onContinueClicked = { viewModel.sendEvent(Event.AcceptDocument) }
    )
}

@Composable
private fun IssuanceContent(state: UiState, onContinueClicked: () -> Unit = {}) {
    AppContent {
        if (state.documents.isNotEmpty()) {
            DocumentContent(state.documents.first(), onContinueClicked)
        } else {
            RetrievingContent()
        }
    }
}

@Composable
fun DocumentContent(document: CredentialDocument, onContinueClicked: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(R.string.issuance_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        VSpace(24.dp)
        DocumentCardView(document = document)
        VSpace(24.dp)
        WSpace()
        PrimaryButton(text = stringResource(R.string.issuance_add_to_wallet), onClick = onContinueClicked)
    }
}

@Composable
fun RetrievingContent() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.issuance_retrieving_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold
        )
        VSpace(16.dp)
        Text(
            text = stringResource(R.string.issuance_retrieving_description),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
        VSpace(24.dp)
        FadedProgressIndicator()
    }
}
