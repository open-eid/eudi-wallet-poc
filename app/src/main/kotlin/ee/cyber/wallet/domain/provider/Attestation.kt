package ee.cyber.wallet.domain.provider

import ee.cyber.wallet.domain.credentials.CredentialType
import ee.cyber.wallet.domain.provider.wallet.KeyAttestation
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class Attestation(
    val id: String,
    val credential: String,
    val type: CredentialType,
    val keyAttestation: KeyAttestation,
    val issuedAt: Instant = Clock.System.now()
)
