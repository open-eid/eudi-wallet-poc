package ee.cyber.wallet.domain.provider.pid

import ee.cyber.wallet.domain.provider.wallet.KeyAttestation
import ee.cyber.wallet.domain.provider.wallet.KeyType

class InstanceBinding(
    val sessionToken: ByteArray,
    val cNonce: ByteArray,
    val personalDataAccessToken: ByteArray
) {
    val isBound by lazy { sessionToken.isNotEmpty() && cNonce.isNotEmpty() && personalDataAccessToken.isNotEmpty() }
}

enum class AttestationType(val keyType: KeyType) {
    SD_JWT_VC(KeyType.EC),
    MDOC(KeyType.EC)
}

class AttestationRequest(
    val keyAttestation: KeyAttestation,
    val proofOfPossession: String
)
