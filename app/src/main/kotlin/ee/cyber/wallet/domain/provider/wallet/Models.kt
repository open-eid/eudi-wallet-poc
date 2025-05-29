package ee.cyber.wallet.domain.provider.wallet

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jwt.SignedJWT

data class WalletInstanceCredentials(
    val instanceId: String,
    val instancePassword: String
) {
    fun isRegistered() = instanceId.isNotBlank() && instancePassword.isNotBlank()
}

data class KeyAttestation(
    val keyId: String,
    val attestation: String,
    val keyType: KeyType
) {
    val jwt: SignedJWT by lazy { SignedJWT.parse(attestation) }
    val jwk: JWK by lazy { JWK.parse(requireNotNull(requireNotNull(jwt.jwtClaimsSet).getJSONObjectClaim("jwk"))) }
    val jwsAlgorithm: JWSAlgorithm by lazy { keyType.jwsAlgorithm() }
}

fun KeyType.jwsAlgorithm(): JWSAlgorithm = when (this) {
    KeyType.RSA -> JWSAlgorithm.RS256
    KeyType.EC -> JWSAlgorithm.ES256
}

fun JWSAlgorithm.asJavaAlgorithm() = when (this) {
    JWSAlgorithm.ES256 -> "SHA256WithECDSA"
    JWSAlgorithm.ES384 -> "SHA384WithECDSA"
    JWSAlgorithm.ES512 -> "SHA512WithECDSA"
    JWSAlgorithm.PS256, JWSAlgorithm.RS256 -> "SHA256withRSA"
    JWSAlgorithm.PS384, JWSAlgorithm.RS384 -> "SHA384withRSA"
    JWSAlgorithm.PS512, JWSAlgorithm.RS512 -> "SHA512withRSA"
    else -> throw IllegalArgumentException("algorithm not supported: $this")
}

data class DeviceData(
    val modelName: String
)

enum class KeyType {
    RSA,
    EC
}
