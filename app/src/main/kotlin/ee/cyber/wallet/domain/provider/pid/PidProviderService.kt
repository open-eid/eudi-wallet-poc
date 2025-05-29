package ee.cyber.wallet.domain.provider.pid

import ee.cyber.wallet.domain.provider.Attestation

interface PidProviderService {

    suspend fun bindInstance(bindingToken: String): InstanceBinding

    suspend fun issuePid(sessionToken: ByteArray, requests: Map<AttestationType, AttestationRequest>): List<Attestation>
}
