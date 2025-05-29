package ee.cyber.wallet

import ee.cyber.wallet.security.CertificateChainValidator
import eu.europa.ec.eudi.openid4vp.X509CertificateTrust
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import java.security.cert.X509Certificate
import kotlin.test.Ignore
import kotlin.test.Test

val rootCertificate = """
-----BEGIN CERTIFICATE-----
MIIDUzCCAjugAwIBAgIUEQCMZr0r8nBLYKyRATO0cUyui9IwDQYJKoZIhvcNAQEL
BQAwODEQMA4GA1UEAwwHVGVzdC1DQTEXMBUGA1UECgwOQ3liZXJuZXRpY2EgQVMx
CzAJBgNVBAYTAkVFMCAXDTI0MDMyODEwMzUzN1oYDzMwMjMwNjMwMTAzNTM3WjA4
MRAwDgYDVQQDDAdUZXN0LUNBMRcwFQYDVQQKDA5DeWJlcm5ldGljYSBBUzELMAkG
A1UEBhMCRUUwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCLEh185mNb
iXqZsJ14dPqGpdG3ERlwPPIpproOzYwOMbB7PHm7loB5bRnAHIsZlmp3bVD1f78t
s/ILPmx7FB4G27PEFC2C7PAYDEdDKpUrOItvZg44AnqkSMugfeimv0LgaHEaeXwK
bR6/+f/zlkdU2RDbM1GBgyDfwzZT4yZYdNR/EgKvrQiZ/rFcE0C7jk0orVXWw5wG
ZZzbUUpK0Li/dzednlzvq6SY1j2UXcXWYA9/dVT3+RL3H5g8JinYGmrHhBRdnkF5
dGjF9x0D6zlQWCS9U+Uzbhrou40mdQDIpy+x91WNDk5tD/POMqq3Ny/ClApKSwQ8
9PaDx54p3AHBAgMBAAGjUzBRMB0GA1UdDgQWBBQ7g4xFUJXtJoZxpWUm64847/yH
FjAfBgNVHSMEGDAWgBQ7g4xFUJXtJoZxpWUm64847/yHFjAPBgNVHRMBAf8EBTAD
AQH/MA0GCSqGSIb3DQEBCwUAA4IBAQAR2WVJMkpcpmBIDOieG2qmz3qT7efQUy6J
LDaAQFFBBqKK+c0NE21axcAxukBdgnx2f6pXKEulIjjGC9i2t9zZPQsM5SXcjPIj
b+9buOJ9TuJpe+sydlMc9BxmXnBzS5QZxv59Ht1jaQkGxd6z+Gz64zHz86UT8zPz
nzwkzEp/qiEOfZbKg1sP2wBiiaHADEYv4cge/CaHYAY6pq8oMgZhTzVgRAWSpjjW
6Cu6kpryhLch01khJ5gYVx7YZSBOKJvdmIr1/JW5yjMoqYKwmXGvcUgp8JGGPSQO
F/uZDgTcPsu23j4Er0cEcUZo34YFEeRIxyr2BYhE3zGhrC5hHtst
-----END CERTIFICATE-----
""".trimIndent()

@Serializable
data class SessionResponse(
    @SerialName("wallet_redirect_uri")
    val walletRedirectUri: String,
    @SerialName("session_id")
    val sessionId: String,
    @SerialName("wallet_redirect_uri_timeout_ms")
    val timeout: Long
)

suspend fun HttpClient.createRPSession(baseUrl: String): SessionResponse {
    val request = """
        {"credential_id":"0","mandatory_attributes":["sub","given_name","family_name","birthdate","age_over_18"],"optional_attributes":[],"not_requested_attributes":[]}
    """.trimIndent()
    return withContext(Dispatchers.IO) {
        use {
            it.post("$baseUrl/api/v1/authorization/sessions.create") {
                setBody(request)
                contentType(ContentType.Application.Json)
            }.body()
        }
    }
}

val httpLogger: org.slf4j.Logger = LoggerFactory.getLogger("HTTP")

class SampleTest {
    @Test
    @Ignore("requires locally running test-rp")
    fun testMain(): Unit = runTest {
//        fun createHttpClient(): HttpClient = HttpClient(OkHttp) {
//            install(ContentNegotiation) { json() }
//
//            install(Logging) {
//                this.logger = object : Logger {
//                    override fun log(message: String) {
//                        httpLogger.info(message)
//                    }
//                }
//                level = LogLevel.BODY
//            }
//
//            expectSuccess = true
//        }
//
//        val keyStore = TestCryptoUtils.createInMemoryJavKeyStore("pwd").apply {
//            issueCertificate(false, "issuer", "issuer", "pwd", null, null)
//            issueCertificate(false, "holder", "holder", "pwd", null, null)
//        }
//
//        val session = createHttpClient().createRPSession("http://localhost:8080")
//
//        val issuerKey = ECKey.load(keyStore, "issuer", "pwd".toCharArray())
//        val issuerKeyProvider = object : KeyProvider {
//            override val key: JWK = issuerKey
//            override val signer: JWSSigner = ECDSASigner(issuerKey)
//            override val verifier: JWSVerifier = ECDSAVerifier(issuerKey.toPublicJWK())
//            override val algorithm: JWSAlgorithm = JWSAlgorithm.ES512
//        }
//
//        val holderKey = ECKey.load(keyStore, "holder", "pwd".toCharArray())
//        val holderKeyProvider = object : KeyProvider {
//            override val key: JWK = holderKey
//            override val signer: JWSSigner = ECDSASigner(holderKey)
//            override val verifier: JWSVerifier = ECDSAVerifier(holderKey.toPublicJWK())
//            override val algorithm: JWSAlgorithm = JWSAlgorithm.ES512
//        }
//
//        val pidIssuanceService = MockCredentialIssuanceService(issuerKeyProvider, holderKeyProvider)
//
//        val pid = Credential.JwtPidCredential(
//            uniqueId = "PNOEE-38001085718",
//            birthDate = LocalDate.parse("1980-01-08"),
//            givenName = "JAAK-KRISTJAN",
//            familyName = "JÕEORG",
//            ageOver18 = true,
//            issueDate = Clock.System.now(),
//            expiryDate = Clock.System.now().plus(365.days),
//            issuingCountry = "EE",
//            issuingAuthority = "RIA"
//        )
//
//        val document = pidIssuanceService.issueCredential(UUID.randomUUID().toString(), pid).let {
//            CredentialToDocumentMapper(listOf()).convert(it)
//        }
//
//        val openId4VPConfig = SiopOpenId4VPConfig(
//            vpConfiguration = VPConfiguration(false, emptyMap()),
//            supportedClientIdSchemes = listOf(
//                SupportedClientIdScheme.X509SanDns(
//                    simpleCertificateChainValidator(X509CertUtils.parse(rootCertificate))
//                )
//            )
//        )
//
//        val openId4VPManager = OpenId4VPManager(
//            Dispatchers.IO,
//            openId4VPConfig = openId4VPConfig,
//            holderKeyProvider = holderKeyProvider,
//            remoteKeyManager = RemoteKeyManager(),
//            httpClientFactory = { createHttpClient() }
//        )
//
//        when (val resolution = openId4VPManager.handleRequestUri(session.walletRedirectUri)) {
//            is Resolution.Invalid -> throw resolution.error.asException()
//            is Resolution.Success -> {
//                val requestObject = resolution.requestObject
//                val consensus = openId4VPManager.buildConsensus(requestObject, document!!)
//                when (val dispatchOutcome = openId4VPManager.sendResponse(requestObject, consensus)) {
//                    is DispatchOutcome.RedirectURI -> error("Unexpected: $dispatchOutcome")
//                    is DispatchOutcome.VerifierResponse.Accepted -> Unit
//                    DispatchOutcome.VerifierResponse.Rejected -> error("Unexpected failure: $dispatchOutcome")
//                }
//            }
//        }
    }
}

private fun simpleCertificateChainValidator(trustAnchor: X509Certificate) = X509CertificateTrust {
    CertificateChainValidator.validateCertificateChain(it, listOf(trustAnchor), false)
}
