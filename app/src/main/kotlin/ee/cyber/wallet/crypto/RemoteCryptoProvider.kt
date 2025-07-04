package ee.cyber.wallet.crypto

import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.jca.JCAContext
import com.nimbusds.jose.jwk.AsymmetricJWK
import com.nimbusds.jose.util.Base64URL
import ee.cyber.wallet.data.database.KeyAttestationEntity
import ee.cyber.wallet.data.database.dao.KeyAttestationDao
import ee.cyber.wallet.data.database.toModel
import ee.cyber.wallet.data.datastore.WalletInstanceCredentialsDataSource
import ee.cyber.wallet.domain.provider.wallet.KeyAttestation
import ee.cyber.wallet.domain.provider.wallet.KeyType
import ee.cyber.wallet.domain.provider.wallet.WalletProviderService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.security.KeyPair

class RemoteCryptoProvider(
    private val dispatcher: CoroutineDispatcher,
    private val keyAttestationDao: KeyAttestationDao,
    private val walletProviderService: WalletProviderService,
    private val walletInstanceCredentialsDataSource: WalletInstanceCredentialsDataSource
) : CryptoProvider {

    private val logger = LoggerFactory.getLogger("RemoteCryptoProvider")

    private val credentials = walletInstanceCredentialsDataSource.credentials

    override suspend fun generateKey(keyType: KeyType): KeyAttestation = withContext(dispatcher) {
        if (!supports(keyType)) throw IllegalArgumentException("Key type no supported: $keyType")

        walletProviderService.generateKey(keyType, credentials.first())
            .also {
                keyAttestationDao.insert(
                    KeyAttestationEntity(
                        id = it.keyId,
                        attestation = it.attestation,
                        keyType = keyType.name
                    )
                )
            }
    }

    override suspend fun getKeyPair(keyId: String): KeyPair {
        val jwk = getKeyAttestation(keyId).jwk as AsymmetricJWK
        return KeyPair(jwk.toPublicKey(), null)
    }

    override suspend fun getKeyAttestation(keyId: String): KeyAttestation =
        keyAttestationDao.getById(keyId).toModel()

    override suspend fun sign(keyId: String, dataToSign: ByteArray): ByteArray {
        logger.debug("sign: $keyId")
        return walletProviderService.sign(getKeyAttestation(keyId), dataToSign, credentials.first())
    }

    override suspend fun jwsSigner(keyId: String): JWSSigner = jwsSigner(getKeyAttestation(keyId))

    private fun jwsSigner(key: KeyAttestation): JWSSigner = object : JWSSigner {
        override fun getJCAContext(): JCAContext = JCAContext()

        override fun supportedJWSAlgorithms() = mutableSetOf(key.jwsAlgorithm)

        override fun sign(header: JWSHeader, signingInput: ByteArray): Base64URL {
            val signature = runBlocking { sign(key.keyId, signingInput) }
            return Base64URL.encode(signature)
        }
    }

    override fun supports(keyType: KeyType): Boolean = walletProviderService.supportsKey(keyType)

    override suspend fun clearAll() {
        runCatching {
            walletInstanceCredentialsDataSource.clearAll()
            keyAttestationDao.deleteAll()
        }
    }
}
