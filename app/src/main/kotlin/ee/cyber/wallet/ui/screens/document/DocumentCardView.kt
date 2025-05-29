package ee.cyber.wallet.ui.screens.document

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ee.cyber.wallet.R
import ee.cyber.wallet.domain.credentials.CredentialAttribute
import ee.cyber.wallet.domain.credentials.CredentialType
import ee.cyber.wallet.domain.documents.CredentialDocument
import ee.cyber.wallet.domain.documents.DocumentField
import ee.cyber.wallet.ui.components.DocumentCardHeader
import ee.cyber.wallet.ui.components.HDivider
import ee.cyber.wallet.ui.components.rememberBase64DecodedBitmap
import ee.cyber.wallet.ui.screens.documents.credentialType
import ee.cyber.wallet.ui.screens.documents.humanReadableValue
import ee.cyber.wallet.ui.screens.documents.isExpiry
import ee.cyber.wallet.ui.screens.documents.label
import ee.cyber.wallet.ui.theme.PreviewThemes
import ee.cyber.wallet.ui.theme.WalletThemePreviewSurface
import ee.cyber.wallet.ui.util.ContentAlpha
import ee.cyber.wallet.ui.util.document
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

@Composable
@PreviewThemes
private fun DocumentCardViewPreview() {
    WalletThemePreviewSurface {
        val document = document(
            CredentialType.PID_SD_JWT,
            listOf(
                DocumentField.fromAttribute(CredentialAttribute.JWT_PID_1_PERSONAL_ADMINISTRATIVE_NUMBER, "38001085718"),
                DocumentField.fromAttribute(CredentialAttribute.JWT_PID_1_GIVEN_NAME, "JAAK-KRISTJAN"),
                DocumentField.fromAttribute(CredentialAttribute.JWT_PID_1_FAMILY_NAME, "JÕEORG"),
                DocumentField.fromAttribute(CredentialAttribute.JWT_PID_1_BIRTHDATE, "08.01.1980"),
                DocumentField.fromAttribute(CredentialAttribute.JWT_PID_1_AGE_EQUAL_OR_OVER_18, "true"),
                DocumentField.fromAttribute(CredentialAttribute.JWT_PID_1_DATE_OF_ISSUANCE, "01.01.2024"),
                DocumentField.fromAttribute(CredentialAttribute.JWT_PID_1_DATE_OF_EXPIRY, "01.01.2025"),
                DocumentField.fromAttribute(CredentialAttribute.JWT_PID_1_ISSUING_AUTHORITY, "Test PID issuer")
            )
        )

        Box(Modifier.padding(16.dp)) {
            DocumentCardView(document = document)
        }
    }
}

@Composable
fun DocumentCardView(document: CredentialDocument) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card {
            DocumentCardHeader(document.credentialType())
            HDivider()
            document.fields.forEach {
                DocumentField(document, it)
            }
        }
    }
}

@Composable
private fun DocumentField(document: CredentialDocument, field: DocumentField) {
    @Composable
    fun addField(field: DocumentField) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column {
                Column {
                    ContentAlpha(0.6f) {
                        val text = field.label(document.type)
                        val next = text
                        Text(text = next, style = MaterialTheme.typography.bodyMedium)
                    }
                    if (field.name == CredentialAttribute.ORG_ISO_18013_5_1_PORTRAIT.fieldName ||
                        field.name == CredentialAttribute.MDOC_PID_1_PORTRAIT.fieldName ||
                        field.name == CredentialAttribute.JWT_PID_1_PICTURE.fieldName ||
                        field.name == CredentialAttribute.ORG_ISO_18013_5_1_BIOMETRIC_TEMPLATE_FACE.fieldName ||
                        field.name == CredentialAttribute.ORG_ISO_18013_5_1_BIOMETRIC_TEMPLATE_FINGER.fieldName ||
                        field.name == CredentialAttribute.ORG_ISO_18013_5_1_BIOMETRIC_TEMPLATE_SIGNATURE_SIGN.fieldName ||
                        field.name == CredentialAttribute.ORG_ISO_18013_5_1_BIOMETRIC_TEMPLATE_IRIS.fieldName ||
                        field.name == CredentialAttribute.ORG_ISO_18013_5_1_SIGNATURE_USUAL_MARK.fieldName
                    ) {
                        rememberBase64DecodedBitmap(field.value)?.let {
                            Image(bitmap = it, contentDescription = "")
                        } ?: Text(text = stringResource(R.string.presentation_image_stub), style = MaterialTheme.typography.titleMedium)
                    } else {
                        val color = if (document.expired && field.isExpiry()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        Text(
                            text = field.value.humanReadableValue(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }
                }
            }
        }
        HDivider()
    }

    when (field.element) {
        is JsonPrimitive -> addField(field)
        is JsonObject -> {
            fun flattenJsonObject(jsonObject: JsonObject, prefix: String = field.name): List<DocumentField> {
                return jsonObject.entries.flatMap { (key, value) ->
                    val newKey = "$prefix.$key"
                    when (value) {
                        is JsonPrimitive -> listOf(field.copy(name = newKey, value = value.toString().trim('"')))
                        is JsonObject -> flattenJsonObject(value, newKey)
                        is JsonArray -> listOf(field.copy(name = newKey, value = value.joinToString(", ") { it.toString().trim('"') }))
                    }
                }
            }
            flattenJsonObject(field.element).forEach { flattenedField ->
                addField(flattenedField)
            }
        }
        is JsonArray -> {
            addField(field.copy(value = field.element.joinToString(", ") { it.toString().trim('"') }))
        }
        else -> addField(field)
    }
}
