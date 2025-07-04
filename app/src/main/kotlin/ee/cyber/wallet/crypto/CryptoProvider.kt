package ee.cyber.wallet.crypto

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.crypto.factories.DefaultJWSSignerFactory
import com.nimbusds.jose.jca.JCAContext
import com.nimbusds.jose.jwk.AsymmetricJWK
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.util.Base64URL
import ee.cyber.wallet.domain.provider.wallet.KeyAttestation
import ee.cyber.wallet.domain.provider.wallet.KeyType
import eu.europa.ec.eudi.openid4vci.JwtBindingKey
import eu.europa.ec.eudi.openid4vci.PopSigner
import eu.europa.ec.eudi.sdjwt.KeyBindingSigner
import id.walt.mdoc.COSECryptoProviderKeyInfo
import id.walt.mdoc.SimpleCOSECryptoProvider
import org.cose.java.AlgorithmID
import java.security.KeyPair

interface CryptoProvider {
    suspend fun generateKey(keyType: KeyType): KeyAttestation
    suspend fun getKeyAttestation(keyId: String): KeyAttestation
    suspend fun getKeyPair(keyId: String): KeyPair
    suspend fun sign(keyId: String, dataToSign: ByteArray): ByteArray
    fun supports(keyType: KeyType): Boolean
    suspend fun clearAll()
    suspend fun jwsSigner(keyId: String): JWSSigner

    class Factory(private val cryptoProviders: List<CryptoProvider>) {
        fun forKeyType(keyType: KeyType): CryptoProvider = cryptoProviders.find { it.supports(keyType) }
            ?: throw IllegalStateException("No supported Key Managers for key type $keyType")
    }
}

suspend fun CryptoProvider.keyBindingSigner(keyId: String): KeyBindingSigner {
    val keyAttestation = getKeyAttestation(keyId)
    val jwsSigner = jwsSigner(keyId)
    return object : KeyBindingSigner {
        override val signAlgorithm: JWSAlgorithm = keyAttestation.jwsAlgorithm
        override val publicKey = keyAttestation.jwk.toPublicJWK() as AsymmetricJWK
        override fun getJCAContext(): JCAContext = jwsSigner.jcaContext
        override fun sign(p0: JWSHeader?, p1: ByteArray?): Base64URL = jwsSigner.sign(p0, p1)
    }
}

suspend fun CryptoProvider.popSigner(keyId: String): PopSigner.Jwt {
    val keyAttestation = getKeyAttestation(keyId)
    val jwsSigner = jwsSigner(keyId)
    val bindingKey = JwtBindingKey.Jwk(keyAttestation.jwk.toPublicJWK())
    return PopSigner.Jwt(keyAttestation.jwsAlgorithm, bindingKey, jwsSigner)
}
// suspend fun CryptoProvider.proofSigner(keyId: String): ProofSigner {
//    val keyAttestation = getKeyAttestation(keyId)
//    val jwsSigner = jwsSigner(keyId)
//    return object : ProofSigner, JWSSigner by jwsSigner {
//        override fun getBindingKey(): BindingKey = BindingKey.Jwk(jwk = keyAttestation.jwk.toPublicJWK())
//        override fun getAlgorithm(): JWSAlgorithm = keyAttestation.jwsAlgorithm
//    }
// }

suspend fun CryptoProvider.deviceCryptoProvider(keyId: String): SimpleCOSECryptoProvider {
    val keyAttestation = getKeyAttestation(keyId)
    val keyPair = getKeyPair(keyId)
    return SimpleCOSECryptoProvider(
        listOf(
            COSECryptoProviderKeyInfo(
                keyID = keyId,
                algorithmID = keyAttestation.jwsAlgorithm.toAlgorithmID(),
                publicKey = keyPair.public,
                privateKey = keyPair.private,
                x5Chain = keyAttestation.jwk.parsedX509CertChain,
                trustedRootCAs = emptyList()
            )
        )
    )
}

fun JWK.jwsSigner(): JWSSigner = DefaultJWSSignerFactory().createJWSSigner(this)

fun JWSAlgorithm.toAlgorithmID(): AlgorithmID = when (this) {
    JWSAlgorithm.ES256 -> AlgorithmID.ECDSA_256
    JWSAlgorithm.ES384 -> AlgorithmID.ECDSA_384
    JWSAlgorithm.ES512 -> AlgorithmID.ECDSA_512
    JWSAlgorithm.PS256 -> AlgorithmID.RSA_PSS_256
    JWSAlgorithm.PS384 -> AlgorithmID.RSA_PSS_384
    JWSAlgorithm.PS512 -> AlgorithmID.RSA_PSS_512
    else -> throw IllegalArgumentException("Unsupported algorithm: $this")
}
