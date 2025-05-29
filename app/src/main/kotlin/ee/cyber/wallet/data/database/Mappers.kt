package ee.cyber.wallet.data.database

import ee.cyber.wallet.domain.credentials.CredentialAttribute
import ee.cyber.wallet.domain.provider.Attestation
import ee.cyber.wallet.domain.provider.wallet.KeyAttestation
import ee.cyber.wallet.domain.provider.wallet.KeyType
import ee.cyber.wallet.ui.screens.activity.LogEntryModel
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull

fun AttestationAttestationKey.toModel() = attestation.toModel(keyAttestation.toModel())
fun AttestationEntity.toModel(keyAttestation: KeyAttestation) = Attestation(
    id = id,
    credential = credential,
    type = type,
    keyAttestation = keyAttestation,
    issuedAt = issuedAt
)

fun Attestation.toEntity() = AttestationEntity(
    id = id,
    issuedAt = issuedAt,
    credential = credential,
    type = type,
    keyAttestationId = keyAttestation.keyId
)

fun KeyAttestationEntity.toModel() = KeyAttestation(
    keyId = id,
    attestation = attestation,
    keyType = KeyType.valueOf(keyType)
)

fun LogEntryEntity.toModel(): LogEntryModel {
    fun JsonElement.toStringOrNull(): String? = if (this is JsonNull) null else toString()
    return LogEntryModel(
        id = id,
        date = date,
        party = party,
        type = docType,
        attributes = attributes?.mapNotNull { entry ->
            CredentialAttribute.findByPath(entry.key)?.let { it to entry.value.toStringOrNull() }
        }?.toMap()?.toSortedMap(compareBy { it.ordinal }) ?: mapOf(),
        error = error
    )
}
