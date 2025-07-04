package ee.cyber.wallet.ui.screens.proximity

import android.Manifest
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.isGranted
import ee.cyber.wallet.R
import ee.cyber.wallet.domain.credentials.CredentialAttribute
import ee.cyber.wallet.domain.credentials.DocType
import ee.cyber.wallet.ui.components.AppContent
import ee.cyber.wallet.ui.components.CameraSettingsCard
import ee.cyber.wallet.ui.components.DocumentCardHeader
import ee.cyber.wallet.ui.components.HDivider
import ee.cyber.wallet.ui.components.PrimaryButton
import ee.cyber.wallet.ui.components.SecondaryButton
import ee.cyber.wallet.ui.components.VSpace
import ee.cyber.wallet.ui.components.VerifiedParty
import ee.cyber.wallet.ui.components.WSpace
import ee.cyber.wallet.ui.components.rememberBase64DecodedBitmap
import ee.cyber.wallet.ui.screens.documents.docType
import ee.cyber.wallet.ui.screens.documents.label
import ee.cyber.wallet.ui.screens.presentation.MatchedField
import ee.cyber.wallet.ui.util.requestPermission
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

@Composable
fun ProximityScreen(onCancel: () -> Unit = {}, onBack: () -> Unit = {}) {
    Proximity(
        viewModel = hiltViewModel(),
        onBack = { onBack() },
        onCancel = { onCancel() }
    )
}

@Composable
private fun Proximity(
    viewModel: ProximityViewModel,
    onCancel: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val bluetoothConnectPermission = requestPermission(permission = Manifest.permission.BLUETOOTH_CONNECT)
    val bluetoothAdvertisePermission = requestPermission(permission = Manifest.permission.BLUETOOTH_ADVERTISE)
    var showProximityRequest by remember { mutableStateOf(false) }
    var showProximityRequestNoMatch by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effect.onEach { effect ->
            when (effect) {
                Effect.ProximityRequest -> showProximityRequest = true
                Effect.ProximityRequestNoMatch -> showProximityRequestNoMatch = true
                Effect.ProximityResponseSent -> onBack()
                Effect.ProximityCancel -> onCancel()
            }
        }.collect()
    }

    AppContent {
        if (showProximityRequest) {
            ProximityRequestContent(state = viewModel.state.value, onEvent = { viewModel.sendEvent(it) })
        } else if (showProximityRequestNoMatch) {
            ProximityRequestNoMatchContent(state = viewModel.state.value, onEvent = { viewModel.sendEvent(it) })
        } else {
            if (bluetoothConnectPermission.status.isGranted && bluetoothAdvertisePermission.status.isGranted) {
                ProximityQrCodeContent(viewModel = viewModel)
            } else {
                NoPermissionContent(onCancel)
            }
        }
    }
}

@Composable
private fun ProximityRequestNoMatchContent(modifier: Modifier = Modifier, state: UiState, onEvent: (Event) -> Unit) {
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
            text = "Verifier ${state.verifier} has requested your credentials, but no matching credentials were found.",
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ProximityRequestContent(modifier: Modifier = Modifier, state: UiState, onEvent: (Event) -> Unit) {
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
            PrimaryButton(text = stringResource(R.string.share_btn), onClick = { onEvent(Event.OnShareClicked) })
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
                    credential.fields.forEach { field ->
                        PresentationOptionalField(field, true, credential.credentialType.docType()) {
                            onEvent(Event.OnOptionalFieldChange(field, it))
                        }
                        HDivider()
                    }
                    credential.optionalFields.forEach { field ->
                        PresentationOptionalField(field, false, credential.credentialType.docType()) {
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
private fun PresentationOptionalField(matchedField: MatchedField, isMandatory: Boolean, docType: DocType, onCheck: (Boolean) -> Unit) {
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
        Switch(
            checked = matchedField.checked,
            onCheckedChange = { onCheck(it) },
            colors = if (isMandatory) {
                SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                    disabledCheckedThumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    disabledCheckedTrackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                )
            } else {
                SwitchDefaults.colors(
                    checkedThumbColor = Color.DarkGray,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            }
        )
    }
}

@Composable
private fun ProximityQrCodeContent(viewModel: ProximityViewModel) {
    val qrCodeBitmap = viewModel.state.value.qrCodeBitmap
    val qrCode by remember(qrCodeBitmap) {
        derivedStateOf {
            qrCodeBitmap?.asImageBitmap()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WSpace()
        if (qrCode != null) {
            VSpace(24)
            Card {
                Column(
                    Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.document_proximity_qr_code_title),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Image(bitmap = qrCode!!, contentDescription = "")
                }
            }
        }
        WSpace()
        SecondaryButton(text = stringResource(R.string.cancel_btn), onClick = { viewModel.sendEvent(Event.OnCancelClicked) })
    }
}

@Composable
private fun NoPermissionContent(onCancel: () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WSpace()
        CameraSettingsCard()
        WSpace()
        SecondaryButton(text = stringResource(R.string.cancel_btn), onClick = onCancel)
    }
}
