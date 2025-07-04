package ee.cyber.wallet.domain.provider.wallet

import android.security.keystore.KeyProperties
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.factories.DefaultJWSSignerFactory
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jwt.JWTClaimsSet
import ee.cyber.wallet.crypto.jwsSigner
import ee.cyber.wallet.security.EncryptedKeyStoreManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.bouncycastle.jce.ECNamedCurveTable
import org.slf4j.LoggerFactory
import java.security.spec.RSAKeyGenParameterSpec
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.UUID

class WalletProviderServiceMock(
    private val keyStore: EncryptedKeyStoreManager,
    private val dispatcher: CoroutineDispatcher
) : WalletProviderService {

    private val logger = LoggerFactory.getLogger("WalletProviderServiceMock")

    override suspend fun registerWalletInstance(deviceData: DeviceData): WalletInstanceCredentials {
        delay(3000)
        return WalletInstanceCredentials(
            instanceId = UUID.randomUUID().toString(),
            instancePassword = UUID.randomUUID().toString()
        )
    }

    override suspend fun activateInstance(personalDataAccessToken: ByteArray, credentials: WalletInstanceCredentials) = delay(1000)

    override fun supportsKey(keyType: KeyType): Boolean = keyType == KeyType.RSA

    override suspend fun generateKey(keyType: KeyType, credentials: WalletInstanceCredentials): KeyAttestation = withContext(dispatcher) {
        if (!supportsKey(keyType)) throw IllegalArgumentException("Key type not supported: $keyType")

        with(UUID.randomUUID().toString()) {
            createKeyPair(keyType, this)
            createKeyAttestation(keyType, this)
        }
    }

    private fun createKeyAttestation(keyType: KeyType, keyId: String): KeyAttestation {
        val jwk = loadJwkFromKeyStore(keyId)
        val now = Instant.now()
        val keyAttestation = JWSObject(
            JWSHeader.Builder(keyType.jwsAlgorithm())
                .x509CertChain(jwk.x509CertChain)
                .build(),
            Payload(
                JWTClaimsSet.Builder()
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plus(60 * 30, ChronoUnit.DAYS)))
                    .claim("jwk", jwk.toPublicJWK().toJSONObject())
                    .build().toJSONObject()
            )
        ).also {
            it.sign(jwsSigner(jwk))
        }.serialize()

        return KeyAttestation(
            keyId = keyId,
            attestation = keyAttestation,
            keyType = keyType
        )
    }

    private fun createKeyPair(keyType: KeyType, keyId: String) = when (keyType) {
        KeyType.RSA -> keyStore.createKeyPair(
            alias = keyId,
            keyAlgorithm = KeyProperties.KEY_ALGORITHM_RSA,
            certSignAlgorithm = keyType.jwsAlgorithm().asJavaAlgorithm(),
            paramSpec = RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4)
        )

        KeyType.EC -> keyStore.createKeyPair(
            alias = keyId,
            keyAlgorithm = KeyProperties.KEY_ALGORITHM_EC,
            certSignAlgorithm = keyType.jwsAlgorithm().asJavaAlgorithm(),
            paramSpec = ECNamedCurveTable.getParameterSpec("secp521r1")
        )
    }

    override suspend fun sign(keyAttestation: KeyAttestation, dataToBeSigned: ByteArray, credentials: WalletInstanceCredentials): ByteArray = withContext(dispatcher) {
        val jwk = loadJwkFromKeyStore(keyAttestation.keyId)
        jwsSigner(jwk).sign(JWSHeader(keyAttestation.jwsAlgorithm), dataToBeSigned).decode()
    }

    override suspend fun attestKey(keyId: String, keyType: KeyType, jwk: JWK, credentials: WalletInstanceCredentials): KeyAttestation {
        val issuedAt = Instant.now()
        val expiresAt = issuedAt.plus(60 * 30, ChronoUnit.DAYS)
        val keyAttestation = JWSObject(
            JWSHeader.Builder(keyType.jwsAlgorithm())
                .x509CertChain(jwk.x509CertChain)
                .build(),
            Payload(
                JWTClaimsSet.Builder()
                    .issueTime(Date.from(issuedAt))
                    .expirationTime(Date.from(expiresAt))
                    .claim("jwk", jwk.toPublicJWK().toJSONObject())
                    .build().toJSONObject()
            )
        ).also {
            it.sign(jwk.jwsSigner())
        }.serialize()
        return KeyAttestation(
            keyId = keyId,
            attestation = keyAttestation,
            keyType = keyType
        )
    }

    private fun jwsSigner(jwk: JWK): JWSSigner = DefaultJWSSignerFactory().createJWSSigner(jwk)

    private fun loadJwkFromKeyStore(keyId: String): JWK = JWK.load(keyStore.keyStore(), keyId, password)

    companion object {
        private val password = "".toCharArray()
    }
}
