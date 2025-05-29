package ee.cyber.wallet.domain.credentials

import ee.cyber.wallet.domain.provider.Attestation
import ee.cyber.wallet.domain.provider.wallet.KeyAttestation

interface CredentialIssuanceService {
    suspend fun issueCredential(credential: Credential, keyAttestation: KeyAttestation): Attestation

    fun supports(credentialType: CredentialType): Boolean
}
