package ee.cyber.wallet.domain.provider

import com.nimbusds.jose.JWSHeader
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import ee.cyber.wallet.crypto.CryptoProvider
import ee.cyber.wallet.data.repository.WalletCredentialsRepository
import ee.cyber.wallet.domain.documents.CredentialDocument
import ee.cyber.wallet.domain.documents.CredentialToDocumentMapper
import ee.cyber.wallet.domain.provider.pid.AttestationRequest
import ee.cyber.wallet.domain.provider.pid.AttestationType
import ee.cyber.wallet.domain.provider.pid.InstanceBinding
import ee.cyber.wallet.domain.provider.pid.PidProviderService
import ee.cyber.wallet.domain.provider.wallet.KeyAttestation
import ee.cyber.wallet.domain.provider.wallet.WalletProviderService
import ee.cyber.wallet.util.toBase64String
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

private val supportedTypes = listOf(AttestationType.SD_JWT_VC, AttestationType.MDOC)

class IssuePidUseCase(
    private val cryptoProviderFactory: CryptoProvider.Factory,
    private val walletProviderService: WalletProviderService,
    private val pidProviderService: PidProviderService,
    private val documentMapper: CredentialToDocumentMapper,
    private val dispatcher: CoroutineDispatcher,
    walletCredentialsRepository: WalletCredentialsRepository
) {

    private val credentials = walletCredentialsRepository.credentials

    suspend fun issuePid(bindingToken: String): List<CredentialDocument> = withContext(dispatcher) {
        val bindInstance = pidProviderService.bindInstance(bindingToken)
        walletProviderService.activateInstance(bindInstance.personalDataAccessToken, credentials.first())
        val requests = supportedTypes.associateWith {
            createAttestationRequest(it, bindInstance)
        }
        pidProviderService.issuePid(bindInstance.sessionToken, requests)
            .mapNotNull { documentMapper.convert(it) }
    }

    private suspend fun createAttestationRequest(
        attestationType: AttestationType,
        bindInstance: InstanceBinding
    ): AttestationRequest {
        val cryptoProvider = cryptoProviderFactory.forKeyType(attestationType.keyType)
        val keyAttestation = cryptoProvider.generateKey(attestationType.keyType)
        val pop = createPoPJwt(keyAttestation, bindInstance.cNonce).apply {
            sign(cryptoProvider.jwsSigner(keyAttestation.keyId))
        }.serialize()
        return AttestationRequest(keyAttestation, pop)
    }

    private fun createPoPJwt(keyAttestation: KeyAttestation, cNonce: ByteArray) =
        SignedJWT(
            JWSHeader.Builder(keyAttestation.jwsAlgorithm).jwk(keyAttestation.jwk.toPublicJWK()).build(),
            JWTClaimsSet.Builder().claim("nonce", cNonce.toBase64String()).build()
        )
}
