package ee.cyber.wallet.ui.screens.offer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ee.cyber.wallet.R
import ee.cyber.wallet.domain.credentials.CredentialAttribute
import ee.cyber.wallet.domain.credentials.CredentialType
import ee.cyber.wallet.ui.components.AppContent
import ee.cyber.wallet.ui.components.DocumentTypeIcon
import ee.cyber.wallet.ui.components.HSpace
import ee.cyber.wallet.ui.components.PrimaryButton
import ee.cyber.wallet.ui.components.SecondaryButton
import ee.cyber.wallet.ui.components.VSpace
import ee.cyber.wallet.ui.components.VerifiedParty
import ee.cyber.wallet.ui.components.WSpace
import ee.cyber.wallet.ui.screens.documents.docType
import ee.cyber.wallet.ui.screens.documents.docTypeNameWithFormat
import ee.cyber.wallet.ui.screens.documents.issuerName
import ee.cyber.wallet.ui.screens.documents.label
import ee.cyber.wallet.ui.theme.PreviewThemes
import ee.cyber.wallet.ui.theme.WalletThemePreviewSurface

data class CredentialOfferScreenNavigationHandler(
    val onCancelClicked: () -> Unit = {},
    val onContinueClicked: (type: CredentialType, bindingToken: String?) -> Unit = { _, _ -> }
)

@Composable
@PreviewThemes
private fun CredentialOfferShortScreenPreview() {
    WalletThemePreviewSurface {
        CredentialOfferScreenContent(
            UiState(
                credentialType = CredentialType.PID_SD_JWT,
                pidAttributes = listOf(
                    CredentialAttribute.JWT_PID_1_PERSONAL_ADMINISTRATIVE_NUMBER,
                    CredentialAttribute.JWT_PID_1_GIVEN_NAME,
                    CredentialAttribute.JWT_PID_1_FAMILY_NAME,
                    CredentialAttribute.JWT_PID_1_BIRTHDATE,
                    CredentialAttribute.JWT_PID_1_AGE_EQUAL_OR_OVER_18
                )
            )
        )
    }
}

@Composable
@PreviewThemes
private fun CredentialOfferScreenPreview() {
    WalletThemePreviewSurface {
        CredentialOfferScreenContent(
            UiState(
                credentialType = CredentialType.PID_SD_JWT,
                pidAttributes = listOf(
                    CredentialAttribute.JWT_PID_1_PERSONAL_ADMINISTRATIVE_NUMBER,
                    CredentialAttribute.JWT_PID_1_GIVEN_NAME,
                    CredentialAttribute.JWT_PID_1_FAMILY_NAME,
                    CredentialAttribute.JWT_PID_1_BIRTHDATE,
                    CredentialAttribute.JWT_PID_1_AGE_EQUAL_OR_OVER_18,
                    CredentialAttribute.JWT_PID_1_PERSONAL_ADMINISTRATIVE_NUMBER,
                    CredentialAttribute.JWT_PID_1_GIVEN_NAME,
                    CredentialAttribute.JWT_PID_1_FAMILY_NAME,
                    CredentialAttribute.JWT_PID_1_BIRTHDATE,
                    CredentialAttribute.JWT_PID_1_AGE_EQUAL_OR_OVER_18,
                    CredentialAttribute.JWT_PID_1_PERSONAL_ADMINISTRATIVE_NUMBER,
                    CredentialAttribute.JWT_PID_1_GIVEN_NAME,
                    CredentialAttribute.JWT_PID_1_FAMILY_NAME,
                    CredentialAttribute.JWT_PID_1_BIRTHDATE,
                    CredentialAttribute.JWT_PID_1_AGE_EQUAL_OR_OVER_18,
                    CredentialAttribute.JWT_PID_1_PERSONAL_ADMINISTRATIVE_NUMBER,
                    CredentialAttribute.JWT_PID_1_GIVEN_NAME,
                    CredentialAttribute.JWT_PID_1_FAMILY_NAME,
                    CredentialAttribute.JWT_PID_1_BIRTHDATE,
                    CredentialAttribute.JWT_PID_1_AGE_EQUAL_OR_OVER_18
                )
            )
        )
    }
}

@Composable
fun CredentialOfferScreen(viewModel: CredentialOfferViewModel, navigationHandler: CredentialOfferScreenNavigationHandler) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CredentialOfferScreenContent(
        state = state,
        navigationHandler = navigationHandler
    )
}

@Composable
private fun CredentialOfferScreenContent(
    state: UiState,
    navigationHandler: CredentialOfferScreenNavigationHandler = CredentialOfferScreenNavigationHandler()
) {
    AppContent {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = state.credentialType.issuerName(),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )
            VSpace(8.dp)
            VerifiedParty()
            VSpace(24.dp)
            Text(
                text = stringResource(R.string.offer_description),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            VSpace(24.dp)
            OfferCard(
                credentialType = state.credentialType,
                attributes = state.pidAttributes
            )
            VSpace(24.dp)
            WSpace()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally)
            ) {
                SecondaryButton(text = stringResource(R.string.cancel_btn), onClick = navigationHandler.onCancelClicked)
                PrimaryButton(
                    text = stringResource(R.string.continue_btn),
                    onClick = {
                        navigationHandler.onContinueClicked(state.credentialType, state.bindingToken)
                    }
                )
            }
        }
    }
}

@Composable
private fun OfferCard(credentialType: CredentialType, attributes: List<CredentialAttribute>) {
    Card {
        OfferCardTitle(credentialType)
        attributes.forEach {
            OfferClaim(it)
        }
    }
}

@Composable
private fun OfferCardTitle(credentialType: CredentialType) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DocumentTypeIcon(docType = credentialType.docType())
            HSpace(12.dp)
            Text(
                text = credentialType.docTypeNameWithFormat(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun OfferClaim(credentialAttribute: CredentialAttribute) {
    Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text = credentialAttribute.label(), style = MaterialTheme.typography.bodyMedium)
    }
}
