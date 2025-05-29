package ee.cyber.wallet.domain.documents

import ee.cyber.wallet.domain.credentials.CredentialAttribute
import ee.cyber.wallet.domain.credentials.CredentialType
import ee.cyber.wallet.domain.credentials.DocType
import ee.cyber.wallet.domain.credentials.Namespace
import ee.cyber.wallet.domain.documents.mdoc.value
import ee.cyber.wallet.domain.provider.Attestation
import ee.cyber.wallet.security.CertificateChainValidator
import ee.cyber.wallet.ui.screens.documents.asCredentialAttribute
import ee.cyber.wallet.ui.screens.documents.docType
import eu.europa.ec.eudi.sdjwt.DefaultSdJwtOps
import eu.europa.ec.eudi.sdjwt.Disclosure
import eu.europa.ec.eudi.sdjwt.JwtAndClaims
import eu.europa.ec.eudi.sdjwt.SdJwt
import eu.europa.ec.eudi.sdjwt.name
import eu.europa.ec.eudi.sdjwt.value
import eu.europa.ec.eudi.sdjwt.vc.X509CertificateTrust
import id.walt.mdoc.dataelement.ListElement
import id.walt.mdoc.dataelement.MapElement
import id.walt.mdoc.doc.MDoc
import id.walt.mdoc.issuersigned.IssuerSignedItem
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.slf4j.LoggerFactory
import java.security.cert.X509Certificate
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

class CredentialToDocumentMapper(
    private val trustAnchors: List<X509Certificate>
) {

    private val logger = LoggerFactory.getLogger("CredentialToDocumentMapper")

    suspend fun convert(attestation: Attestation): CredentialDocument? {
        return when (attestation.type) {
            CredentialType.PID_SD_JWT -> loadSdJwtCredential(attestation)?.asDocument(attestation)
            CredentialType.PID_MDOC -> MDoc.fromCBORHex(attestation.credential).asDocument(attestation)
            CredentialType.MDL -> MDoc.fromCBORHex(attestation.credential).asDocument(attestation)
        }
    }

    private fun simpleCertificateChainValidator(trustAnchors: List<X509Certificate>) = X509CertificateTrust {
        CertificateChainValidator.validateCertificateChain(it, trustAnchors, false)
    }

    private suspend fun loadSdJwtCredential(attestation: Attestation): SdJwt<JwtAndClaims>? =
        DefaultSdJwtOps.SdJwtVcVerifier.usingX5c(simpleCertificateChainValidator(trustAnchors))
            .verify(attestation.credential)
            .onFailure { logger.error("failure: ", it) }
            .getOrNull()

    private fun MDoc.asDocument(attestation: Attestation): CredentialDocument.MDocDocument {
        val elements = nameSpaces.associateWith { ns ->
            getIssuerSignedItems(ns).associate {
                if (it.elementIdentifier.value.equals("driving_privileges")) {
                    it.elementIdentifier.value to getDrivingPrivileges(it)
                } else {
                    it.elementIdentifier.value to it.elementValue.value()
                }
            }
        }
        val fields = elements.flatMap { nsGroup ->
            val namespace = nsGroup.key.let { ns -> Namespace.byUri(ns) }
            if (namespace == null) {
                emptyList()
            } else {
                nsGroup.value.map {
                    DocumentField(
                        namespace = namespace,
                        name = it.key,
                        value = it.value!!.toString()
                    )
                }
            }
        }
        val expiresAt = fields.find { it.name == CredentialAttribute.MDOC_PID_1_EXPIRY_DATE.fieldName }?.value?.let { LocalDate.parse(it) }
        return CredentialDocument.MDocDocument(
            id = attestation.id,
            type = attestation.type.docType(),
            fields = fields.sortedBy { it.asCredentialAttribute(attestation.type.docType()) },
            expired = expiresAt?.let { LocalDate.now(ZoneOffset.UTC).isAfter(it) } ?: false,
            attestation = attestation,
            mDoc = this
        )
    }

    private fun getDrivingPrivileges(it: IssuerSignedItem): String {
        return (it.elementValue as ListElement).value.joinToString("\n") { element ->
            val privilegesKeyOrder = listOf("vehicle_category_code", "issue_date", "expiry_date", "codes")
            val codesKeyOrder = listOf("code", "sign", "value")
            (element as MapElement).value.entries
                .sortedBy { entry -> privilegesKeyOrder.indexOf(entry.key.toString()) }
                .joinToString(", ") { entry ->
                    if (entry.key.toString() == "codes") {
                        "\n" + (entry.value as ListElement).value.joinToString(", ") { codeElement ->
                            (codeElement as MapElement).value.entries
                                .sortedBy { entry -> codesKeyOrder.indexOf(entry.key.toString()) }
                                .joinToString(" ") { codeEntry ->
                                    "${codeEntry.value.internalValue}"
                                }
                        }
                    } else {
                        entry.value.internalValue.toString()
                    }
                }
        }
    }

    private fun List<Disclosure>.asDocumentFields(): List<DocumentField> {
        val result = mutableListOf<DocumentField>()
        map { it.claim() }.forEach { disclosure ->
            val value = disclosure.value()
            result.add(
                DocumentField(
                    namespace = Namespace.NONE,
                    name = disclosure.name(),
                    value = when (value) {
                        is JsonPrimitive -> value.content
                        is JsonObject -> value.toString()
                        is JsonArray -> value.toString()
                    },
                    element = disclosure.value()
                )
            )
        }
        return result
    }

    private fun SdJwt<JwtAndClaims>.asDocument(attestation: Attestation): CredentialDocument.JwtDocument {
        val jwtClaims = jwt.second
            .filter { it.value is JsonPrimitive }
            .map { Pair(it.key, (it.value as JsonPrimitive).content) }
            .toMap()
        val fields = disclosures.asDocumentFields()
        val expiresAt = LocalDateTime.ofInstant(Instant.ofEpochSecond(jwtClaims["exp"]!!.toLong()), ZoneOffset.UTC)

        return when (val vct = jwtClaims["vct"]) {
            DocType.PID_SD_JWT.uri -> {
                CredentialDocument.JwtDocument(
                    id = attestation.id,
                    fields = fields.sortedBy { it.asCredentialAttribute(DocType.PID_SD_JWT) },
                    type = DocType.PID_SD_JWT,
                    expired = LocalDateTime.now(ZoneOffset.UTC).isAfter(expiresAt),
                    attestation = attestation,
                    sdJwt = this
                )
            }

            else -> throw IllegalArgumentException("Unsupported credential type: $vct")
        }
    }
}
