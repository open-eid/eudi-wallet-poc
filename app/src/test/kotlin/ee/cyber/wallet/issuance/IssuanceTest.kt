package ee.cyber.wallet.issuance

import kotlinx.coroutines.test.runTest
import kotlin.test.Ignore
import kotlin.test.Test

class IssuanceTest {

    val issuerCert = """
    -----BEGIN CERTIFICATE-----
MIIDgDCCAmigAwIBAgIJALmyJefXQOYiMA0GCSqGSIb3DQEBCwUAMG4xCzAJBgNV
BAYTAkVFMRAwDgYDVQQIEwdVbmtub3duMRAwDgYDVQQHEwdVbmtub3duMRcwFQYD
VQQKEw5DeWJlcm5ldGljYSBBUzEQMA4GA1UECxMHVW5rbm93bjEQMA4GA1UEAxMH
VW5rbm93bjAeFw0yNDAzMjIxNzAxMjJaFw0yNjEyMTcxNzAxMjJaMG4xCzAJBgNV
BAYTAkVFMRAwDgYDVQQIEwdVbmtub3duMRAwDgYDVQQHEwdVbmtub3duMRcwFQYD
VQQKEw5DeWJlcm5ldGljYSBBUzEQMA4GA1UECxMHVW5rbm93bjEQMA4GA1UEAxMH
VW5rbm93bjCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBANqJ7fIMXS5K
9gWKpTMJCoYb9aifMN8i5dcPS+a7IJ88CU1gUhnfKWd2W3/HMJ5LVGM6uvjPai0V
ihaSzsO3RYrv2i7xTk5erdXj23WwuKhB0Ef1hpYIfpiUjW54pGGtbxCsp8woi2O3
zbSW8FRMZB/LnMA8bAtyTMF7pqYrWfYfmQl8LP3UssLIzf+ocQBVr8LoVzpDcbP4
LLIJ3VWPeIUBzRiR9UCyxbpjSrOwi8WgLYYP5yNvRGUOJCvlbe8FlS4PH6dwOHIU
OCCtd+mscZNzD4NZlTyjbN5YZWwr3Le1/dCJAH6wR5lPpgqNKmN6Zp7iwMCefYcv
xIc6Yw7tsiMCAwEAAaMhMB8wHQYDVR0OBBYEFLtdBnpICzoJpi7vudm1Q+dZopGu
MA0GCSqGSIb3DQEBCwUAA4IBAQB0A4WA/D64YimTddPgTMvXnlaNigZyOLMYQ2nz
RojJHK/ByBZNzrFmQXKkPAkVYMufPlTEz2LZA3OQCMO+eRLkqQogoHGDIn0njHYx
tiStN+axhNnEZbrx9tvlEWYlM8CZtoJl5ccQA8kOg6kzycMkFZhEUQT5qH2Z0FT5
fUA4x23UpcYSzKeB842Vh3NiUwcT1s6QAGzb0nXB4eqjwoTguN18KAtOP8ZFVpHH
NnQVG0mCU94cowacw7vcOLXdoni31+QkQIjeM3StULpNx2RGLcKFhOCu3B/ow657
EFE6kPgvspdIyy++CdMTWZzNQyYLbfyDhMNZs8G9mJez5zE9
-----END CERTIFICATE-----
    """.trimIndent()

    @Test
    @Ignore("requires locally running test-rp")
    fun `should issue valid credentials`() = runTest {
//        val keyStore = TestCryptoUtils.createInMemoryJavKeyStore("pwd").apply {
//            issueCertificate(false, "issuer", "issuer", "pwd", null, null)
//            issueCertificate(false, "holder", "holder", "pwd", null, null)
//        }
//
//        val issuerVerifier = RSASSAVerifier(RSAKey.parse(X509CertUtils.parse(issuerCert)).toPublicJWK())
//
//        val holderKey = ECKey.load(keyStore, "holder", "pwd".toCharArray())
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
//        val pidIssuanceService = RpcCredentialIssuanceService(holderKey, "http://localhost:6565")
//        val rawCredential = pidIssuanceService.issueCredential(pid, UUID.randomUUID().toString())
//            .also { println("Issued JWT: $it") }
//
//        CredentialToDocumentMapper(listOf()).convert(rawCredential).also {
//            println("Verified document: $it")
//        }
    }
}
