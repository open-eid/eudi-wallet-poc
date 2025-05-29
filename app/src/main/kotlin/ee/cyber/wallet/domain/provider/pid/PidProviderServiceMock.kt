package ee.cyber.wallet.domain.provider.pid

import android.content.Context
import ee.cyber.wallet.domain.credentials.Credential
import ee.cyber.wallet.domain.credentials.CredentialIssuanceService
import ee.cyber.wallet.domain.provider.Attestation
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.LocalDate
import kotlin.random.Random

const val MOCK_APP_PREFS = "mock_app_prefs"
const val MOCK_USER_PID = "mock_user_pid"

class PidProviderServiceMock(
    private val context: Context,
    private val credentialIssuanceService: CredentialIssuanceService
) : PidProviderService {

    private val PORTRAIT_38001085718 = context.resources.openRawResource(ee.cyber.wallet.R.raw.portrait_38001085718).readBytes()
    private val PORTRAIT_47101010033 = context.resources.openRawResource(ee.cyber.wallet.R.raw.portrait_47101010033).readBytes()
    private val PORTRAIT_50801139731 = context.resources.openRawResource(ee.cyber.wallet.R.raw.portrait_50801139731).readBytes()

    private val JWT_38001085718_ARF = Credential.JwtArfPidCredential(
        familyName = "Jõeorg",
        givenName = "Jaak-Kristjan",
        birthdate = LocalDate.parse("1980-01-08"),
        placeOfBirth = Credential.PlaceOfBirth(locality = "EE"),
        nationalities = listOf("EE"),
        address = Credential.Address(
            formatted = "Tallinna mnt 605, 10145 Tallinn",
            streetAddress = "Tallinna mnt 605",
            country = "EE",
            region = "Harju",
            locality = "Tallinn",
            postalCode = "10145",
            houseNumber = "605"
        ),
        personalAdministrativeNumber = "38001085718",
        picture = PORTRAIT_38001085718,
        birthGivenName = "Jaak",
        birthFamilyName = "Org",
        sex = 1,
        email = "jaak.kristjan.joeorg@example.com",
        phoneNumber = "+37200000766",
        dateOfIssuance = LocalDate.parse("2020-12-13"),
        dateOfExpiry = LocalDate.parse("2028-12-13"),
        issuingAuthority = "RIA",
        issuingCountry = "EE",
        issuingJurisdiction = "EE-I",
        ageEqualOrOver = Credential.AgeEqualOrOver(is16 = true, is18 = true, is21 = true),
        ageInYears = 45,
        ageBirthYear = 1980,
        documentNumber = "EE1234567",
        trustAnchor = "https://trustanchor.example.com/statuslist/1",
    )

    private val JWT_38001085718 = Credential.JwtPidCredential(
        familyName = "Joeorg",
        givenName = "Jaak-Kristjan",
        birthDate = LocalDate.parse("1980-01-08"),
        birthPlace = "EE",
        nationality = "EE",
        residentAddress = "Tallinna mnt 605, 10145 Tallinn",
        residentCountry = "EE",
        residentState = "Harju",
        residentCity = "Tallinn",
        residentPostalCode = "10145",
        residentStreet = "Tallinna mnt",
        residentHouseNumber = "605",
        personalAdministrativeNumber = "38001085718",
        portrait = PORTRAIT_38001085718,
        givenNameBirth = "Jaak",
        familyNameBirth = "Org",
        sex = 1,
        emailAddress = "jaak.kristjan.joeorg@example.com",
        mobilePhoneNumber = "+37200000766",
        issuanceDate = LocalDate.parse("2020-12-13"),
        expiryDate = LocalDate.parse("2028-12-13"),
        issuingAuthority = "RIA",
        issuingCountry = "EE",
        issuingJurisdiction = "EE-I",
        ageOver16 = true,
        ageOver18 = true,
        ageOver21 = true,
        ageInYears = 45,
        ageBirthYear = 1980,
        documentNumber = "EE1234567",
        trustAnchor = "https://trustanchor.example.com/statuslist/1",
    )

    private val MDOC_38001085718 = Credential.MdocPidCredential(
        familyName = "Joeorg",
        givenName = "Jaak-Kristjan",
        birthDate = LocalDate.parse("1980-01-08"),
        birthPlace = "EE",
        nationality = "EE",
        residentAddress = "Tallinna mnt 605, 10145 Tallinn",
        residentCountry = "EE",
        residentState = "Harju",
        residentCity = "Tallinn",
        residentPostalCode = "10145",
        residentStreet = "Tallinna mnt",
        residentHouseNumber = "605",
        personalAdministrativeNumber = "38001085718",
        portrait = PORTRAIT_38001085718,
        givenNameBirth = "Jaak",
        familyNameBirth = "Org",
        sex = 1,
        emailAddress = "jaak.kristjan.joeorg@example.com",
        mobilePhoneNumber = "+37200000766",
        issuanceDate = LocalDate.parse("2020-12-13"),
        expiryDate = LocalDate.parse("2028-12-13"),
        issuingAuthority = "RIA",
        issuingCountry = "EE",
        issuingJurisdiction = "EE-I",
        ageOver16 = true,
        ageOver18 = true,
        ageOver21 = true,
        ageInYears = 45,
        ageBirthYear = 1980,
        documentNumber = "EE1234567"
    )

    private val JWT_47101010033 = Credential.JwtPidCredential(
        familyName = "O’Connež-Šuslik",
        givenName = "Mari-Liis Õnne",
        birthDate = LocalDate.parse("1971-01-01"),
        birthPlace = "EE",
        nationality = "EE",
        residentAddress = "Pärnu mnt 705, 12145 Tallinn",
        residentCountry = "EE",
        residentState = "Harju",
        residentCity = "Tallinn",
        residentPostalCode = "12145",
        residentStreet = "Pärnu mnt",
        residentHouseNumber = "705",
        personalAdministrativeNumber = "47101010033",
        portrait = PORTRAIT_47101010033,
        givenNameBirth = "Mari-Liis Õnne",
        familyNameBirth = "O’Connež-Šuslik",
        sex = 2,
        emailAddress = "mari.liis.onne@example.com",
        mobilePhoneNumber = "+37200000877",
        issuanceDate = LocalDate.parse("2020-11-13"),
        expiryDate = LocalDate.parse("2028-11-13"),
        issuingAuthority = "RIA",
        issuingCountry = "EE",
        issuingJurisdiction = "EE-I",
        ageOver16 = true,
        ageOver18 = true,
        ageOver21 = true,
        ageInYears = 54,
        ageBirthYear = 1971,
        documentNumber = "EE2345678",
        trustAnchor = "https://trustanchor.example.com/statuslist/1",
    )

    private val MDOC_47101010033 = Credential.MdocPidCredential(
        familyName = "O’Connež-Šuslik",
        givenName = "Mari-Liis Õnne",
        birthDate = LocalDate.parse("1971-01-01"),
        birthPlace = "EE",
        nationality = "EE",
        residentAddress = "Pärnu mnt 705, 12145 Tallinn",
        residentCountry = "EE",
        residentState = "Harju",
        residentCity = "Tallinn",
        residentPostalCode = "12145",
        residentStreet = "Pärnu mnt",
        residentHouseNumber = "705",
        personalAdministrativeNumber = "47101010033",
        portrait = PORTRAIT_47101010033,
        givenNameBirth = "Mari-Liis Õnne",
        familyNameBirth = "O’Connež-Šuslik",
        sex = 2,
        emailAddress = "mari.liis.onne@example.com",
        mobilePhoneNumber = "+37200000877",
        issuanceDate = LocalDate.parse("2020-11-13"),
        expiryDate = LocalDate.parse("2028-11-13"),
        issuingAuthority = "RIA",
        issuingCountry = "EE",
        issuingJurisdiction = "EE-I",
        ageOver16 = true,
        ageOver18 = true,
        ageOver21 = true,
        ageInYears = 54,
        ageBirthYear = 1971,
        documentNumber = "EE2345678"
    )

    private val JWT_50801139731 = Credential.JwtPidCredential(
        familyName = "Alaealine",
        givenName = "Alar",
        birthDate = LocalDate.parse("2008-01-13"),
        birthPlace = "EE",
        nationality = "EE",
        residentAddress = "Tartu mnt 605, 13145 Tallinn",
        residentCountry = "EE",
        residentState = "Harju",
        residentCity = "Tallinn",
        residentPostalCode = "13145",
        residentStreet = "Tartu mnt",
        residentHouseNumber = "605",
        personalAdministrativeNumber = "50801139731",
        portrait = PORTRAIT_50801139731,
        givenNameBirth = "Alarike",
        familyNameBirth = "Vastsündinu",
        sex = 2,
        emailAddress = "alar.alaealine@example.com",
        mobilePhoneNumber = "+37200000988",
        issuanceDate = LocalDate.parse("2020-10-14"),
        expiryDate = LocalDate.parse("2028-10-14"),
        issuingAuthority = "RIA",
        issuingCountry = "EE",
        issuingJurisdiction = "EE-I",
        ageOver16 = true,
        ageOver18 = false,
        ageOver21 = false,
        ageInYears = 17,
        ageBirthYear = 2008,
        documentNumber = "EE3456789",
        trustAnchor = "https://trustanchor.example.com/statuslist/1",
    )

    private val MDOC_50801139731 = Credential.MdocPidCredential(
        familyName = "Alaealine",
        givenName = "Alar",
        birthDate = LocalDate.parse("2008-01-13"),
        birthPlace = "EE",
        nationality = "EE",
        residentAddress = "Tartu mnt 605, 13145 Tallinn",
        residentCountry = "EE",
        residentState = "Harju",
        residentCity = "Tallinn",
        residentPostalCode = "13145",
        residentStreet = "Tartu mnt",
        residentHouseNumber = "605",
        personalAdministrativeNumber = "50801139731",
        portrait = PORTRAIT_50801139731,
        givenNameBirth = "Alarike",
        familyNameBirth = "Vastsündinu",
        sex = 2,
        emailAddress = "alar.alaealine@example.com",
        mobilePhoneNumber = "+37200000988",
        issuanceDate = LocalDate.parse("2020-10-14"),
        expiryDate = LocalDate.parse("2028-10-14"),
        issuingAuthority = "RIA",
        issuingCountry = "EE",
        issuingJurisdiction = "EE-I",
        ageOver16 = true,
        ageOver18 = false,
        ageOver21 = false,
        ageInYears = 17,
        ageBirthYear = 2008,
        documentNumber = "EE3456789"
    )

    val pidToCredential = mapOf(
        "38001085718" to (JWT_38001085718_ARF to MDOC_38001085718),
        "1" to (JWT_38001085718 to MDOC_38001085718),
        "2" to (JWT_47101010033 to MDOC_47101010033),
        "3" to (JWT_50801139731 to MDOC_50801139731)
    )

    override suspend fun bindInstance(bindingToken: String): InstanceBinding {
        return InstanceBinding(
            sessionToken = Random.nextBytes(256),
            cNonce = Random.nextBytes(256),
            personalDataAccessToken = Random.nextBytes(256)
        )
    }

    override suspend fun issuePid(sessionToken: ByteArray, requests: Map<AttestationType, AttestationRequest>): List<Attestation> = coroutineScope {
        val pid = context.getSharedPreferences(MOCK_APP_PREFS, Context.MODE_PRIVATE).getString(MOCK_USER_PID, "")
        requests.map { entry ->
            async {
                val pid = pidToCredential.getOrDefault(pid, JWT_38001085718 to MDOC_38001085718)
                credentialIssuanceService.issueCredential(
                    credential = when (entry.key) {
                        AttestationType.SD_JWT_VC -> pid.first
                        AttestationType.MDOC -> pid.second
                    },
                    keyAttestation = entry.value.keyAttestation
                )
            }
        }.awaitAll()
    }
}
