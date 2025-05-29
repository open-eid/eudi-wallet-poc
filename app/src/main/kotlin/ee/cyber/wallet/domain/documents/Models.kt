package ee.cyber.wallet.domain.documents

import android.os.Parcelable
import ee.cyber.wallet.domain.credentials.CredentialAttribute
import ee.cyber.wallet.domain.credentials.DocType
import ee.cyber.wallet.domain.credentials.Namespace
import ee.cyber.wallet.domain.credentials.isFamilyName
import ee.cyber.wallet.domain.credentials.isGivenName
import ee.cyber.wallet.domain.presentation.SupportedFormat
import ee.cyber.wallet.domain.provider.Attestation
import eu.europa.ec.eudi.sdjwt.JwtAndClaims
import eu.europa.ec.eudi.sdjwt.SdJwt
import id.walt.mdoc.doc.MDoc
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import kotlinx.serialization.json.JsonElement

sealed class CredentialDocument(
    open val id: String,
    open val type: DocType,
    open val fields: List<DocumentField>,
    open val expired: Boolean,
    open val attestation: Attestation
) {
    data class MDocDocument(
        override val id: String,
        override val type: DocType,
        override val fields: List<DocumentField>,
        override val expired: Boolean,
        override val attestation: Attestation,
        val mDoc: MDoc
    ) : CredentialDocument(id, type, fields, expired, attestation)

    data class JwtDocument(
        override val id: String,
        override val type: DocType,
        override val fields: List<DocumentField>,
        override val expired: Boolean,
        override val attestation: Attestation,
        val sdJwt: SdJwt<JwtAndClaims>
    ) : CredentialDocument(id, type, fields, expired, attestation)
}

fun CredentialDocument.supportedFormat() = when (this) {
    is CredentialDocument.JwtDocument -> SupportedFormat.SD_JWT
    is CredentialDocument.MDocDocument -> SupportedFormat.MSO_MDOC
}

fun CredentialDocument.fullName(): String? {
    val givenName = fields.firstOrNull { it.isGivenName() }?.value ?: return null
    val familyName = fields.firstOrNull { it.isFamilyName() }?.value ?: return null
    return "$givenName $familyName"
}

@Parcelize
data class DocumentField(
    val namespace: Namespace,
    val name: String,
    val value: String,
    val element: @RawValue JsonElement? = null,
    val optional: Boolean = false
) : Parcelable {
    companion object {
        fun fromAttribute(attr: CredentialAttribute, value: String, optional: Boolean = false) =
            DocumentField(
                namespace = attr.namespace,
                name = attr.fieldName,
                value = value,
                element = null,
                optional = optional
            )

        fun pidField(name: String, value: String, optional: Boolean = false) =
            DocumentField(
                namespace = Namespace.EU_EUROPA_EC_EUDI_PID_1,
                name = name,
                value = value,
                element = null,
                optional = optional
            )
    }
}

fun DocumentField.isGivenName() = name in CredentialAttribute.entries.filter { it.isGivenName() }.map { it.fieldName }
fun DocumentField.isFamilyName() = name in CredentialAttribute.entries.filter { it.isFamilyName() }.map { it.fieldName }
