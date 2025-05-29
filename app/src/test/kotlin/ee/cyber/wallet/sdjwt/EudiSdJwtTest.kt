package ee.cyber.wallet.sdjwt

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.jca.JCAContext
import com.nimbusds.jose.jwk.AsymmetricJWK
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.util.Base64URL
import com.nimbusds.jwt.SignedJWT
import ee.cyber.wallet.test.TestCertificateUtils.issueCertificate
import ee.cyber.wallet.test.TestCryptoUtils
import ee.cyber.wallet.util.JsonSupport.toJson
import eu.europa.ec.eudi.sdjwt.JwtSignatureVerifier
import eu.europa.ec.eudi.sdjwt.KeyBindingSigner
import eu.europa.ec.eudi.sdjwt.NimbusSdJwtOps.asJwtVerifier
import eu.europa.ec.eudi.sdjwt.NimbusSdJwtOps.kbJwtIssuer
import eu.europa.ec.eudi.sdjwt.NimbusSdJwtOps.serialize
import eu.europa.ec.eudi.sdjwt.NimbusSdJwtOps.serializeWithKeyBinding
import eu.europa.ec.eudi.sdjwt.SdJwt
import eu.europa.ec.eudi.sdjwt.SdJwtIssuer
import eu.europa.ec.eudi.sdjwt.cnf
import eu.europa.ec.eudi.sdjwt.nimbus
import eu.europa.ec.eudi.sdjwt.sdJwt
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.random.Random
import kotlin.test.Test

class EudiSdJwtTest {

    @Serializable
    data class Pid2(
        @SerialName("sub")
        val uniqueId: String? = null,
        @SerialName("given_name")
        val givenName: String? = null,
        @SerialName("family_name")
        val familyName: String? = null,
        @SerialName("birthdate")
        val birthDate: String? = null,
        @SerialName("age_over_18")
        val ageOver18: Boolean? = null
    )

    // TODO: fix this test
    @Test
    fun `should generate valid credentials, and verifiable presentation - basic`() {
        runTest {
            val issuedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS)
            val expiresAt = issuedAt.plus(EXPIRES_IN, ChronoUnit.DAYS)

            val pid = Pid2(
                uniqueId = "PNOEE-38001085718",
                birthDate = "1980-01-08",
                givenName = "JAAK-KRISTJAN",
                familyName = "JÕEORG",
                ageOver18 = true
            )
            val sdJwtIssuer: SdJwtIssuer<SignedJWT> = SdJwtIssuer.nimbus(
                signer = ECDSASigner(issuerKey),
                signAlgorithm = JWSAlgorithm.ES512
            ) {
                type(JOSEObjectType("vc+sd-jwt"))
                //            keyID(issuerKey.keyID)
                x509CertChain(issuerKey.x509CertChain)
                //            jwk(issuerKey.toPublicJWK())
            }

            val payload = sdJwt {
                claim("iss", issuer)
                claim("sub", pid.uniqueId!!)
                claim("exp", expiresAt.epochSecond)
                claim("iat", issuedAt.epochSecond)
                cnf(holderKey.toPublicJWK())
                claim("sd", pid.toJson())
            }

            val issuanceSdJwt = sdJwtIssuer.issue(payload).getOrThrow().apply {
                println(jwt.serialize())
                println(jwt.payload)
                println(jwt.header)
                println(disclosures.map { d -> java.util.Base64.getDecoder().decode(d.value).let { String(it) } })
            }
            val credential = issuanceSdJwt.serialize()

            val asJwtVerifier: JwtSignatureVerifier<SignedJWT> = ECDSAVerifier(issuerKey.toPublicJWK()).asJwtVerifier()
            asJwtVerifier.verify(credential)

            val simpleCertificateChainValidator = eu.europa.ec.eudi.sdjwt.vc.X509CertificateTrust { true }

            // DefaultSdJwtOps.SdJwtVcVerifier.usingX5c(simpleCertificateChainValidator).verify(credential).getOrThrow()

            // Present
            val issueTime = Date.from(Instant.now().truncatedTo(ChronoUnit.SECONDS))
            // val hashAlg = (issuanceSdJwt.jwt.jwtClaimsSet.getClaim("_sd_alg") as String).let { HashAlgorithm.fromString(it) }!!
            val keyBindingSigner: KeyBindingSigner by lazy {
                val actualSigner = ECDSASigner(holderKey)
                object : KeyBindingSigner {
                    override val signAlgorithm: JWSAlgorithm = JWSAlgorithm.ES512
                    override val publicKey = holderKey.toPublicJWK() as AsymmetricJWK
                    override fun getJCAContext(): JCAContext = actualSigner.jcaContext
                    override fun sign(p0: JWSHeader?, p1: ByteArray?): Base64URL = actualSigner.sign(p0, p1)
                }
            }
            val presentationSdJwt = SdJwt(issuanceSdJwt.jwt, issuanceSdJwt.disclosures)

            val buildKbJwt = kbJwtIssuer(keyBindingSigner, JWSAlgorithm.ES512, keyBindingSigner.publicKey) { // TODO: use hashAlg
                audience(verifier)
                claim("nonce", nonce)
                issueTime(issueTime)
            }
            val presentation: String = presentationSdJwt.serializeWithKeyBinding(buildKbJwt).getOrThrow()

            println("Presentation")
            presentation.split("~").forEach {
                println("\n\t$it")
            }

            // verify presentation
//            val challenge = buildJsonObject {
//                put("nonce", nonce)
//                put("aud", verifier)
//                put("iat", issueTime.toInstant().epochSecond)
//            }
//            val keyBindingVerifier : MustBePresentAndValid<JWT>  = KeyBindingVerifier.mustBePresentAndValid(NimbusSdJwtOps.HolderPubKeyInConfirmationClaim, challenge)

//            val presented = DefaultSdJwtOps.verify(
//                jwtSignatureVerifier = noSignatureValidation,
//                keyBindingVerifier = keyBindingVerifier,
//                unverifiedSdJwt = presentation
//            ).getOrThrow()
//
//            println("presented: $presented")
        }
    }

    companion object {
        private const val verifier = "https://test-rp.example.com"
        private const val issuer = "https://test-issuer.example.com"
        private const val EXPIRES_IN: Long = 1 * 24 * 60 * 60 * 1000 // 1 day

        private val nonce = HexFormat.of().formatHex(Random.nextBytes(16))

        private val keyStore = TestCryptoUtils.createInMemoryJavKeyStore("pwd").apply {
            issueCertificate(false, "issuer", "issuer", "pwd", null, null, issuer)
            issueCertificate(false, "holder", "holder", "pwd", null, null)
        }
        private val issuerKey = ECKey.load(keyStore, "issuer", "pwd".toCharArray())
        private val holderKey = ECKey.load(keyStore, "holder", "pwd".toCharArray())
    }
}
