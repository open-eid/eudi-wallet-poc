package ee.cyber.wallet.domain.provider.ageverification

import android.content.Context
import ee.cyber.wallet.crypto.CryptoProvider
import ee.cyber.wallet.domain.credentials.Credential
import ee.cyber.wallet.domain.credentials.CredentialIssuanceService
import ee.cyber.wallet.domain.provider.Attestation
import ee.cyber.wallet.domain.provider.pid.MOCK_APP_PREFS
import ee.cyber.wallet.domain.provider.pid.MOCK_USER_PID
import ee.cyber.wallet.domain.provider.wallet.KeyType
import kotlinx.datetime.LocalDate

class AgeVerificationProviderServiceMock(
    private val context: Context,
    private val cryptoProviderFactory: CryptoProvider.Factory,
    private val credentialIssuanceService: CredentialIssuanceService
) {

    suspend fun issueAgeVerification(): Attestation {
        val credential = getAgeVerificationCredentialByPid()
        val cryptoProvider = cryptoProviderFactory.forKeyType(KeyType.EC)

        return credentialIssuanceService.issueCredential(credential, cryptoProvider.generateKey(KeyType.EC))
    }

    private fun getAgeVerificationCredentialByPid(): Credential.AgeVerificationCredential {
        val prefs = context.getSharedPreferences(MOCK_APP_PREFS, Context.MODE_PRIVATE)
        val pid = prefs.getString(MOCK_USER_PID, null)
        
        return when (pid) {
            "38001085718" -> getAgeVerificationCredential38001085718() // 20 years old
            "47101010033" -> getAgeVerificationCredential47101010033() // 54 years old
            "50801139731" -> getAgeVerificationCredential50801139731() // 17 years old
            else -> getAgeVerificationCredential38001085718() // Default to adult
        }
    }

    private fun getAgeVerificationCredential38001085718() = Credential.AgeVerificationCredential(
        userPseudonym = "pseudonym1",
        ageOver18 = true,
        issuanceDate = LocalDate(2024, 1, 1),
        expiryDate = LocalDate(2034, 1, 1),
        issuingAuthority = "Estonian Age Verification Authority",
        issuingCountry = "EST",
        issuingJurisdiction = "EE"
    )

    private fun getAgeVerificationCredential47101010033() = Credential.AgeVerificationCredential(
        userPseudonym = "pseudonym2",
        ageOver18 = true,
        issuanceDate = LocalDate(2024, 1, 1),
        expiryDate = LocalDate(2025, 1, 1),
        issuingAuthority = "Estonian Age Verification Authority",
        issuingCountry = "EST",
        issuingJurisdiction = "EE"
    )

    private fun getAgeVerificationCredential50801139731() = Credential.AgeVerificationCredential(
        userPseudonym = "pseudonym1",
        ageOver18 = false,
        issuanceDate = LocalDate(2024, 1, 1),
        expiryDate = LocalDate(2034, 1, 1),
        issuingAuthority = "Estonian Age Verification Authority",
        issuingCountry = "EST",
        issuingJurisdiction = "EE"
    )
}