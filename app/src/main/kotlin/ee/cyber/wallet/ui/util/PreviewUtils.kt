package ee.cyber.wallet.ui.util

import ee.cyber.wallet.domain.credentials.CredentialType
import ee.cyber.wallet.domain.credentials.DocType
import ee.cyber.wallet.domain.documents.CredentialDocument
import ee.cyber.wallet.domain.documents.DocumentField
import ee.cyber.wallet.domain.provider.Attestation
import ee.cyber.wallet.domain.provider.wallet.KeyAttestation
import ee.cyber.wallet.domain.provider.wallet.KeyType
import ee.cyber.wallet.ui.screens.documents.docType
import eu.europa.ec.eudi.sdjwt.JwtAndClaims
import eu.europa.ec.eudi.sdjwt.SdJwt
import id.walt.mdoc.doc.MDocBuilder
import kotlinx.serialization.json.JsonObject
import java.util.UUID

val emptyJson = JsonObject(emptyMap())
val emptySdJwt = SdJwt(JwtAndClaims("", emptyJson), emptyList())

fun document(credentialType: CredentialType, fields: List<DocumentField> = listOf()): CredentialDocument {
    return when (credentialType) {
        CredentialType.PID_SD_JWT -> CredentialDocument.JwtDocument(
            id = "123",
            type = credentialType.docType(),
            fields = fields,
            expired = false,
            attestation = Attestation(
                id = UUID.randomUUID().toString(),
                credential = "",
                type = credentialType,
                keyAttestation = KeyAttestation(
                    keyId = UUID.randomUUID().toString(),
                    attestation = "",
                    keyType = KeyType.RSA
                )
            ),
            sdJwt = SdJwt(JwtAndClaims("", JsonObject(emptyMap())), emptyList())
        )

        CredentialType.PID_MDOC, CredentialType.MDL -> CredentialDocument.MDocDocument(
            id = "123",
            type = credentialType.docType(),
            fields = fields,
            expired = false,
            attestation = Attestation(
                id = UUID.randomUUID().toString(),
                credential = "",
                type = credentialType,
                keyAttestation = KeyAttestation(
                    keyId = UUID.randomUUID().toString(),
                    attestation = "",
                    keyType = KeyType.EC
                )
            ),
            mDoc = MDocBuilder(DocType.PID.uri).build(null)
        )
    }
}
