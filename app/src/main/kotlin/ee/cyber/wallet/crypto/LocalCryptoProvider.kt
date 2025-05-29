package ee.cyber.wallet.crypto

import android.security.keystore.KeyProperties
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.jwk.AsymmetricJWK
import com.nimbusds.jose.jwk.ECKey
import ee.cyber.wallet.data.database.KeyAttestationEntity
import ee.cyber.wallet.data.database.dao.KeyAttestationDao
import ee.cyber.wallet.data.database.toModel
import ee.cyber.wallet.data.datastore.WalletInstanceCredentialsDataSource
import ee.cyber.wallet.domain.provider.wallet.KeyAttestation
import ee.cyber.wallet.domain.provider.wallet.KeyType
import ee.cyber.wallet.domain.provider.wallet.WalletProviderService
import ee.cyber.wallet.domain.provider.wallet.asJavaAlgorithm
import ee.cyber.wallet.domain.provider.wallet.jwsAlgorithm
import ee.cyber.wallet.security.EncryptedKeyStoreManager
import kotlinx.coroutines.flow.first
import org.bouncycastle.jce.ECNamedCurveTable
import org.slf4j.LoggerFactory
import java.security.KeyPair
import java.security.spec.RSAKeyGenParameterSpec
import java.util.UUID

class LocalCryptoProvider(
    private val keyAttestationDao: KeyAttestationDao,
    private val keyStoreManager: EncryptedKeyStoreManager,
    private val walletProviderService: WalletProviderService,
    walletInstanceCredentialsDataSource: WalletInstanceCredentialsDataSource
) : CryptoProvider {

    private val logger = LoggerFactory.getLogger("LocalKeyManager")

    private val credentials = walletInstanceCredentialsDataSource.credentials

    override suspend fun generateKey(keyType: KeyType): KeyAttestation {
        logger.debug("generateKey: {}", keyType)
        if (!supports(keyType)) throw IllegalArgumentException("Key type not supported: $keyType")

        return with(UUID.randomUUID().toString()) {
            createKeyPair(keyType, this)
            createKeyAttestation(keyType, this)
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
    }

    override suspend fun getKeyPair(keyId: String): KeyPair {
        val jwk = loadJwkFromKeyStore(keyId) as AsymmetricJWK
        return KeyPair(jwk.toPublicKey(), jwk.toPrivateKey())
    }

    override suspend fun getKeyAttestation(keyId: String): KeyAttestation =
        keyAttestationDao.getById(keyId).toModel()

    override suspend fun sign(keyId: String, dataToSign: ByteArray): ByteArray {
        logger.debug("sign: $keyId")
        val keyAttestation = getKeyAttestation(keyId)
        return with(jwsSigner(keyAttestation)) {
            sign(JWSHeader(keyAttestation.jwsAlgorithm), dataToSign).decode()
        }
    }

    override suspend fun jwsSigner(keyId: String): JWSSigner = jwsSigner(getKeyAttestation(keyId))

    private fun jwsSigner(key: KeyAttestation): JWSSigner = loadJwkFromKeyStore(key.keyId).jwsSigner()

    override fun supports(keyType: KeyType): Boolean = keyType == KeyType.EC

    override suspend fun clearAll() {
        runCatching {
            keyStoreManager.clearAll()
            keyAttestationDao.deleteAll()
        }
    }

    private suspend fun createKeyAttestation(keyType: KeyType, keyId: String): KeyAttestation =
        walletProviderService.attestKey(keyId, keyType, loadJwkFromKeyStore(keyId), credentials.first())

    private fun createKeyPair(keyType: KeyType, keyId: String) = when (keyType) {
        KeyType.RSA -> keyStoreManager.createKeyPair(
            alias = keyId,
            keyAlgorithm = KeyProperties.KEY_ALGORITHM_RSA,
            certSignAlgorithm = keyType.jwsAlgorithm().asJavaAlgorithm(),
            paramSpec = RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4)
        )

        KeyType.EC -> keyStoreManager.createKeyPair(
            alias = keyId,
            keyAlgorithm = KeyProperties.KEY_ALGORITHM_EC,
            certSignAlgorithm = keyType.jwsAlgorithm().asJavaAlgorithm(),
            paramSpec = ECNamedCurveTable.getParameterSpec("P-256")
        )
    }

    private fun loadJwkFromKeyStore(keyId: String) =
        ECKey.load(keyStoreManager.keyStore(), keyId, password)

    companion object {
        private val password = "".toCharArray()
    }
}
