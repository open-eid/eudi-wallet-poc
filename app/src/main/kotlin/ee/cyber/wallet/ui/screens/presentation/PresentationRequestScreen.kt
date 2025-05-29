package ee.cyber.wallet.ui.screens.presentation

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ee.cyber.wallet.R
import ee.cyber.wallet.domain.AppError
import ee.cyber.wallet.domain.credentials.CredentialAttribute
import ee.cyber.wallet.domain.credentials.CredentialType
import ee.cyber.wallet.domain.credentials.DocType
import ee.cyber.wallet.domain.documents.DocumentField
import ee.cyber.wallet.ui.components.AppContent
import ee.cyber.wallet.ui.components.DocumentCardHeader
import ee.cyber.wallet.ui.components.FadedProgressIndicator
import ee.cyber.wallet.ui.components.FullScreenFadedScrimProgressIndicator
import ee.cyber.wallet.ui.components.HDivider
import ee.cyber.wallet.ui.components.PrimaryButton
import ee.cyber.wallet.ui.components.SecondaryButton
import ee.cyber.wallet.ui.components.VSpace
import ee.cyber.wallet.ui.components.VerifiedParty
import ee.cyber.wallet.ui.components.WSpace
import ee.cyber.wallet.ui.components.rememberBase64DecodedBitmap
import ee.cyber.wallet.ui.screens.documents.docType
import ee.cyber.wallet.ui.screens.documents.label
import ee.cyber.wallet.ui.screens.pin.Input
import ee.cyber.wallet.ui.screens.pin.PinActivityResultContract
import ee.cyber.wallet.ui.screens.pin.Result
import ee.cyber.wallet.ui.theme.PreviewThemes
import ee.cyber.wallet.ui.theme.PreviewThemesSmallScreen
import ee.cyber.wallet.ui.theme.WalletThemePreviewSurface
import ee.cyber.wallet.ui.theme.green_600
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

data class PresentationRequestScreenNavigationHandler(
    val onGoBack: (exit: Boolean) -> Unit = {},
    val onShowError: (error: AppError, retryable: Boolean) -> Unit = { _, _ -> }
)

private val testFields: MatchedFields = listOf(
    MatchedField(DocumentField.fromAttribute(CredentialAttribute.JWT_PID_1_PERSONAL_ADMINISTRATIVE_NUMBER, "PNOEE-38001085718")),
    MatchedField(DocumentField.fromAttribute(CredentialAttribute.JWT_PID_1_GIVEN_NAME, "JAAK-KRISTJAN")),
    MatchedField(DocumentField.fromAttribute(CredentialAttribute.JWT_PID_1_FAMILY_NAME, "JÕEORG"))
)

private val testFields2: MatchedFields = listOf(
    MatchedField(DocumentField.fromAttribute(CredentialAttribute.JWT_PID_1_PERSONAL_ADMINISTRATIVE_NUMBER, "PNOEE-38001085718")),
    MatchedField(DocumentField.fromAttribute(CredentialAttribute.JWT_PID_1_PERSONAL_ADMINISTRATIVE_NUMBER, "PNOEE-38001085718")),
    MatchedField(DocumentField.fromAttribute(CredentialAttribute.JWT_PID_1_PERSONAL_ADMINISTRATIVE_NUMBER, "PNOEE-38001085718")),
    MatchedField(DocumentField.fromAttribute(CredentialAttribute.JWT_PID_1_GIVEN_NAME, "JAAK-KRISTJAN")),
    MatchedField(DocumentField.fromAttribute(CredentialAttribute.JWT_PID_1_GIVEN_NAME, "JAAK-KRISTJAN")),
    MatchedField(DocumentField.fromAttribute(CredentialAttribute.JWT_PID_1_GIVEN_NAME, "JAAK-KRISTJAN")),
    MatchedField(DocumentField.fromAttribute(CredentialAttribute.JWT_PID_1_FAMILY_NAME, "JÕEORG")),
    MatchedField(DocumentField.fromAttribute(CredentialAttribute.JWT_PID_1_FAMILY_NAME, "JÕEORG")),
    MatchedField(DocumentField.fromAttribute(CredentialAttribute.JWT_PID_1_FAMILY_NAME, "JÕEORG"))
)

private val testOptFields: MatchedFields = listOf(
    MatchedField(DocumentField.fromAttribute(CredentialAttribute.JWT_PID_1_BIRTHDATE, "1980-01-08"), false),
    MatchedField(DocumentField.fromAttribute(CredentialAttribute.JWT_PID_1_AGE_EQUAL_OR_OVER_18, "true"))
)

@Composable
@PreviewThemesSmallScreen
private fun PresentationSmallScreenLongPreview() {
    WalletThemePreviewSurface {
        val credential = Credential(
            credentialType = CredentialType.PID_SD_JWT,
            fields = testFields,
            optionalFields = testOptFields
        )
        Presentation(UiState(credentials = listOf(credential), verifier = "Test Relying Party"))
    }
}

@Composable
@PreviewThemes
private fun PresentationPreview() {
    WalletThemePreviewSurface {
        val credential = Credential(
            credentialType = CredentialType.PID_SD_JWT,
            fields = testFields,
            optionalFields = testOptFields
        )
        Presentation(UiState(credentials = listOf(credential), verifier = "Test Relying Party", isLoading = true))
    }
}

@Composable
@PreviewThemes
private fun PresentationLongPreview() {
    WalletThemePreviewSurface {
        val credential = Credential(
            credentialType = CredentialType.PID_SD_JWT,
            fields = testFields2,
            optionalFields = testOptFields
        )
        Presentation(UiState(credentials = listOf(credential), verifier = "Test Relying Party"))
    }
}

@Composable
@PreviewThemes
private fun PresentationSuccessPreview() {
    WalletThemePreviewSurface {
        val credential = Credential(
            fields = testFields
        )
        Presentation(UiState(credentials = listOf(credential), success = Success()))
    }
}

@PreviewThemes
@Composable
private fun LoadingContentPreview() {
    WalletThemePreviewSurface {
        Presentation(state = UiState(isLoading = true))
    }
}

@Composable
fun PresentationScreen(viewModel: PresentationRequestViewModel, navigationHandler: PresentationRequestScreenNavigationHandler) {
    val pinLauncher = rememberLauncherForActivityResult(PinActivityResultContract()) {
        when (it) {
            is Result.Success -> viewModel.sendEvent(Event.UserAuthenticated)
            Result.Cancelled -> {
                Event.OnCancelClicked
            }

            Result.Failure -> viewModel.sendEvent(Event.IncorrectPin)
        }
    }

    BackHandler(enabled = true) {
        viewModel.sendEvent(Event.OnCloseClicked)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.onEach { effect ->
            when (effect) {
                is Effect.AuthenticateWithPin -> {
                    pinLauncher.launch(Input(party = effect.party))
                }

                is Effect.Back -> {
                    navigationHandler.onGoBack(effect.exit)
                }

                is Effect.ShowError -> {
                    navigationHandler.onShowError(effect.error.appError, effect.error.retryable)
                }
            }
        }.collect()
    }

    Presentation(
        state = viewModel.state.value,
        onEvent = { viewModel.sendEvent(it) }
    )
}

@Composable
private fun Presentation(state: UiState, onEvent: (Event) -> Unit = {}) {
    val uriHandler = LocalUriHandler.current
    Box {
        val emptyFields = state.credentials.all { credential ->
            credential.fields.isEmpty()
        }
        AppContent {
            if (state.isLoading && emptyFields) {
                LoadingContent()
            } else if (state.success != null) {
                SuccessContent(
                    autoClose = true,
                    onContinueClicked = {
                        onEvent(Event.OnCloseClicked)
                        if (state.success.redirectUri != null) {
                            uriHandler.openUri(state.success.redirectUri.toString())
                        }
                    }
                )
            } else {
                PresentationContent(state = state, onEvent = onEvent)
            }
        }
        FullScreenFadedScrimProgressIndicator(visible = state.isLoading && !emptyFields)
    }
}

private const val AUTO_CLOSE_IN_SECONDS = 5

@Composable
fun SuccessContent(
    @Suppress("SameParameterValue") autoClose: Boolean = false,
    onContinueClicked: () -> Unit
) {
    var autoCloseIn by remember {
        mutableIntStateOf(if (autoClose) AUTO_CLOSE_IN_SECONDS else Int.MAX_VALUE)
    }

    val buttonText = if (autoCloseIn == Int.MAX_VALUE) stringResource(R.string.continue_btn) else stringResource(R.string.presentation_close_in_btn, autoCloseIn)

    if (autoClose) {
        LaunchedEffect(Unit) {
            while (autoCloseIn > 0) {
                delay(1000)
                autoCloseIn--
            }
            onContinueClicked()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.presentation_data_exchange_successful),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = green_600,
                    textAlign = TextAlign.Center
                )
            }
            WSpace()
            Icon(
                modifier = Modifier.size(128.dp),
                painter = painterResource(R.drawable.check_circle_w200),
                contentDescription = "",
                tint = green_600
            )
            WSpace()
            PrimaryButton(
                onClick = onContinueClicked
            ) {
                Text(text = buttonText)
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Processing document request", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold)
        VSpace(24.dp)
        FadedProgressIndicator()
    }
}

@Composable
private fun PresentationContent(modifier: Modifier = Modifier, state: UiState, onEvent: (Event) -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = state.verifier,
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
        PresentationRequestCard(
            state = state,
            onEvent = onEvent
        )
        VSpace(24.dp)
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            style = MaterialTheme.typography.bodyMedium,
            text = stringResource(R.string.presentation_footer_note, state.verifier),
            textAlign = TextAlign.Center
        )
        VSpace(24.dp)
        WSpace()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally)
        ) {
            SecondaryButton(text = stringResource(R.string.cancel_btn), onClick = { onEvent(Event.OnCancelClicked) })
            PrimaryButton(text = stringResource(R.string.share_btn), enabled = !state.shareDisabled, onClick = { onEvent(Event.OnShareClicked) })
        }
    }
}

@Composable
private fun PresentationRequestCard(state: UiState, onEvent: (Event) -> Unit) {
    Column {
        state.credentials.forEach { credential ->
            Card(modifier = Modifier.padding(bottom = 16.dp)) {
                DocumentCardHeader(docType = credential.credentialType.docType())
                HDivider()
                Column {
                    credential.fields.forEach {
                        PresentationField(it, credential.credentialType.docType())
                        HDivider()
                    }
                    credential.optionalFields.forEach { field ->
                        PresentationOptionalField(field, credential.credentialType.docType()) {
                            onEvent(Event.OnOptionalFieldChange(field, it))
                        }
                        HDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun PresentationField(matchedField: MatchedField, docType: DocType) {
    Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(text = matchedField.field.label(docType), style = MaterialTheme.typography.bodySmall)
            if (matchedField.field.name == CredentialAttribute.ORG_ISO_18013_5_1_PORTRAIT.fieldName ||
                matchedField.field.name == CredentialAttribute.MDOC_PID_1_PORTRAIT.fieldName ||
                matchedField.field.name == CredentialAttribute.JWT_PID_1_PICTURE.fieldName ||
                matchedField.field.name == CredentialAttribute.ORG_ISO_18013_5_1_BIOMETRIC_TEMPLATE_FACE.fieldName ||
                matchedField.field.name == CredentialAttribute.ORG_ISO_18013_5_1_BIOMETRIC_TEMPLATE_FINGER.fieldName ||
                matchedField.field.name == CredentialAttribute.ORG_ISO_18013_5_1_BIOMETRIC_TEMPLATE_SIGNATURE_SIGN.fieldName ||
                matchedField.field.name == CredentialAttribute.ORG_ISO_18013_5_1_BIOMETRIC_TEMPLATE_IRIS.fieldName ||
                matchedField.field.name == CredentialAttribute.ORG_ISO_18013_5_1_SIGNATURE_USUAL_MARK.fieldName
            ) {
                rememberBase64DecodedBitmap(base64Image = matchedField.field.value)?.let {
                    Image(bitmap = it, contentDescription = "")
                } ?: Text(text = stringResource(R.string.presentation_image_stub), style = MaterialTheme.typography.titleMedium)
            } else {
                Text(text = matchedField.field.value, style = MaterialTheme.typography.titleMedium)
            }
        }
        WSpace()
        Icon(imageVector = Icons.Rounded.CheckCircleOutline, contentDescription = "", tint = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun PresentationOptionalField(matchedField: MatchedField, docType: DocType, onCheck: (Boolean) -> Unit) {
    Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(text = matchedField.field.label(docType), style = MaterialTheme.typography.bodySmall)
            if (matchedField.field.name == CredentialAttribute.ORG_ISO_18013_5_1_PORTRAIT.fieldName ||
                matchedField.field.name == CredentialAttribute.MDOC_PID_1_PORTRAIT.fieldName ||
                matchedField.field.name == CredentialAttribute.JWT_PID_1_PICTURE.fieldName ||
                matchedField.field.name == CredentialAttribute.ORG_ISO_18013_5_1_BIOMETRIC_TEMPLATE_FACE.fieldName ||
                matchedField.field.name == CredentialAttribute.ORG_ISO_18013_5_1_BIOMETRIC_TEMPLATE_FINGER.fieldName ||
                matchedField.field.name == CredentialAttribute.ORG_ISO_18013_5_1_BIOMETRIC_TEMPLATE_SIGNATURE_SIGN.fieldName ||
                matchedField.field.name == CredentialAttribute.ORG_ISO_18013_5_1_BIOMETRIC_TEMPLATE_IRIS.fieldName ||
                matchedField.field.name == CredentialAttribute.ORG_ISO_18013_5_1_SIGNATURE_USUAL_MARK.fieldName
            ) {
                rememberBase64DecodedBitmap(base64Image = matchedField.field.value)?.let {
                    Image(bitmap = it, contentDescription = "")
                } ?: Text(text = stringResource(R.string.presentation_image_stub), style = MaterialTheme.typography.titleMedium)
            } else {
                Text(text = matchedField.field.value, style = MaterialTheme.typography.titleMedium)
            }
        }
        WSpace()
        Switch(checked = matchedField.checked, onCheckedChange = { onCheck(it) })
    }
}
