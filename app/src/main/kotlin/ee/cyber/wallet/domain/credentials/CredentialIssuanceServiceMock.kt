package ee.cyber.wallet.domain.credentials

import android.content.Context
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.util.Base64
import ee.cyber.wallet.crypto.jwsSigner
import ee.cyber.wallet.domain.presentation.SupportedFormat
import ee.cyber.wallet.domain.provider.Attestation
import ee.cyber.wallet.domain.provider.wallet.KeyAttestation
import eu.europa.ec.eudi.sdjwt.NimbusSdJwtOps
import eu.europa.ec.eudi.sdjwt.NimbusSdJwtOps.serialize
import eu.europa.ec.eudi.sdjwt.RFC7519
import eu.europa.ec.eudi.sdjwt.SdJwtVcSpec
import eu.europa.ec.eudi.sdjwt.cnf
import eu.europa.ec.eudi.sdjwt.sdJwt
import id.walt.mdoc.COSECryptoProviderKeyInfo
import id.walt.mdoc.SimpleCOSECryptoProvider
import id.walt.mdoc.dataelement.BooleanElement
import id.walt.mdoc.dataelement.ByteStringElement
import id.walt.mdoc.dataelement.DataElement
import id.walt.mdoc.dataelement.FullDateElement
import id.walt.mdoc.dataelement.ListElement
import id.walt.mdoc.dataelement.MapElement
import id.walt.mdoc.dataelement.MapKey
import id.walt.mdoc.dataelement.NumberElement
import id.walt.mdoc.dataelement.StringElement
import id.walt.mdoc.doc.MDocBuilder
import id.walt.mdoc.mso.DeviceKeyInfo
import id.walt.mdoc.mso.Status
import id.walt.mdoc.mso.StatusListInfo
import id.walt.mdoc.mso.ValidityInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.plus
import org.cose.java.AlgorithmID
import org.cose.java.OneKey
import org.slf4j.LoggerFactory
import java.security.KeyStore
import java.util.*

private const val MOCK_PID_ISSUER_KEY_ID = "pid_issuer"
private const val MOCK_MDL_ISSUER_KEY_ID = "mdl_issuer"

class CredentialIssuanceServiceMock(
    val context: Context,
    val dispatcher: CoroutineDispatcher
) : CredentialIssuanceService {
    private val logger = LoggerFactory.getLogger("CredentialIssuanceServiceMock")

    override fun supports(credentialType: CredentialType): Boolean = when (credentialType) {
        CredentialType.PID_SD_JWT, CredentialType.PID_MDOC -> true
        else -> false
    }

    override suspend fun issueCredential(credential: Credential, keyAttestation: KeyAttestation): Attestation {
        logger.debug("issueCredential: {}", credential)
        return when (credential) {
            is Credential.JwtPidCredential -> issueSdJwtPid(credential, keyAttestation)
            is Credential.JwtArfPidCredential -> issueSdJwtArfPid(credential, keyAttestation)
            is Credential.MdocPidCredential -> issueMDocPid(credential, keyAttestation)
            is Credential.MdocMdlCredential -> issueMDocMdl(credential, keyAttestation)
            else -> TODO("${credential.type} not supported yet")
        }.let {
            Attestation(
                id = UUID.randomUUID().toString(),
                credential = it,
                type = credential.type,
                keyAttestation = keyAttestation
            )
        }
    }

    suspend fun issueSdJwtPid(pid: Credential.JwtPidCredential, keyAttestation: KeyAttestation): String {
        val sdJwtSpec = sdJwt {
            claim(RFC7519.ISSUER, "https://eudi-pid-issuer.example.com")
            claim(RFC7519.ISSUED_AT, 1740045600)
            claim(RFC7519.EXPIRATION_TIME, 1771581600)
            claim(SdJwtVcSpec.VCT, DocType.PID_SD_JWT.uri)
            objClaim("status") {
                objClaim("status_list") {
                    claim("idx", "123456")
                    claim("uri", "https://raw.githubusercontent.com/open-eid/eudi-wallet-poc/refs/heads/master/statuslists/1")
                }
            }
            cnf(keyAttestation.jwk.toPublicJWK())
            sdClaim("family_name", pid.familyName)
            sdClaim("given_name", pid.givenName)
            sdClaim("birth_date", pid.birthDate.toString())
            sdClaim("birth_place", pid.birthPlace)
            sdClaim("nationality", pid.nationality)
            pid.residentAddress?.let { sdClaim("resident_address", it) }
            pid.residentCountry?.let { sdClaim("resident_country", it) }
            pid.residentState?.let { sdClaim("resident_state", it) }
            pid.residentCity?.let { sdClaim("resident_city", it) }
            pid.residentPostalCode?.let { sdClaim("resident_postal_code", it) }
            pid.residentStreet?.let { sdClaim("resident_street", it) }
            pid.residentHouseNumber?.let { sdClaim("resident_house_number", it) }
            sdClaim("personal_administrative_number", pid.personalAdministrativeNumber)
            pid.portrait?.let { sdClaim("portrait", Base64.encode(it).toString()) }
            sdClaim("given_name_birth", pid.givenNameBirth)
            sdClaim("family_name_birth", pid.familyNameBirth)
            pid.sex?.let { sdClaim("sex", it) }
            pid.emailAddress?.let { sdClaim("email_address", it) }
            pid.mobilePhoneNumber?.let { sdClaim("mobile_phone_number", it) }
            sdClaim("issuance_date", pid.issuanceDate.toString())
            sdClaim("expiry_date", pid.expiryDate.toString())
            sdClaim("issuing_authority", pid.issuingAuthority)
            sdClaim("issuing_country", pid.issuingCountry)
            pid.issuingJurisdiction?.let { sdClaim("issuing_jurisdiction", it) }
            sdClaim("age_over_16", pid.ageOver16)
            sdClaim("age_over_18", pid.ageOver18)
            sdClaim("age_over_21", pid.ageOver21)
            pid.ageInYears?.let { sdClaim("age_in_years", it) }
            pid.ageBirthYear?.let { sdClaim("age_birth_year", it) }
            pid.documentNumber?.let { sdClaim("document_number", it) }
            pid.trustAnchor?.let { sdClaim("trust_anchor", it) }
        }
        val keyPair = pidIssuerKeyPair()
        val issuer = NimbusSdJwtOps.issuer(signer = keyPair.jwsSigner(), signAlgorithm = JWSAlgorithm.ES256) {
            type(JOSEObjectType(SupportedFormat.SD_JWT.value))
            x509CertChain(keyPair.parsedX509CertChain.map { Base64.encode(it.encoded) })
        }
        return issuer.issue(sdJwtSpec).getOrThrow().serialize()
    }

    suspend fun issueSdJwtArfPid(pid: Credential.JwtArfPidCredential, keyAttestation: KeyAttestation): String {
        val sdJwtSpec = sdJwt {
            claim(RFC7519.ISSUER, "https://eudi-pid-issuer.example.com")
            claim(RFC7519.ISSUED_AT, 1740045600)
            claim(RFC7519.EXPIRATION_TIME, 1771581600)
            claim(SdJwtVcSpec.VCT, DocType.PID_SD_JWT.uri)
            objClaim("status") {
                objClaim("status_list") {
                    claim("idx", "123456")
                    claim("uri", "https://raw.githubusercontent.com/open-eid/eudi-wallet-poc/refs/heads/master/statuslists/1") // TODO: Use official status list
                }
            }
            cnf(keyAttestation.jwk.toPublicJWK())
            sdClaim("family_name", pid.familyName)
            sdClaim("given_name", pid.givenName)
            sdClaim("birthdate", pid.birthdate.toString())
            sdObjClaim("place_of_birth") {
                pid.placeOfBirth.country?.let { country -> claim("country", country) }
                pid.placeOfBirth.region?.let { region -> claim("region", region) }
                pid.placeOfBirth.locality?.let { locality -> claim("locality", locality) }
            }
            sdArrClaim("nationalities") {
                pid.nationalities.forEach {
                    claim(it)
                }
            }
            pid.address?.let {
                sdObjClaim("address") {
                    it.formatted?.let { formatted -> claim("formatted", formatted) }
                    it.country?.let { country -> claim("country", country) }
                    it.region?.let { region -> claim("region", region) }
                    it.locality?.let { locality -> claim("locality", locality) }
                    it.postalCode?.let { postalCode -> claim("postal_code", postalCode) }
                    it.streetAddress?.let { streetAddress -> claim("street_address", streetAddress) }
                    it.houseNumber?.let { houseNumber -> claim("house_number", houseNumber) }
                }
            }
            sdClaim("personal_administrative_number", pid.personalAdministrativeNumber)
            pid.picture?.let { sdClaim("picture", Base64.encode(it).toString()) }
            sdClaim("birth_given_name", pid.birthGivenName)
            sdClaim("birth_family_name", pid.birthFamilyName)
            pid.sex?.let { sdClaim("sex", it) }
            pid.email?.let { sdClaim("email", it) }
            pid.phoneNumber?.let { sdClaim("phone_number", it) }
            sdClaim("date_of_issuance", pid.dateOfIssuance.toString())
            sdClaim("date_of_expiry", pid.dateOfExpiry.toString())
            sdClaim("issuing_authority", pid.issuingAuthority)
            sdClaim("issuing_country", pid.issuingCountry)
            pid.issuingJurisdiction?.let { sdClaim("issuing_jurisdiction", it) }
            pid.ageEqualOrOver?.let {
                sdObjClaim("age_equal_or_over") {
                    claim("16", it.is16)
                    claim("18", it.is18)
                    claim("21", it.is21)
                }
            }
            pid.ageInYears?.let { sdClaim("age_in_years", it) }
            pid.ageBirthYear?.let { sdClaim("age_birth_year", it) }
            pid.documentNumber?.let { sdClaim("document_number", it) }
            pid.trustAnchor?.let { sdClaim("trust_anchor", it) }
        }
        val keyPair = pidIssuerKeyPair()
        val issuer = NimbusSdJwtOps.issuer(signer = keyPair.jwsSigner(), signAlgorithm = JWSAlgorithm.ES256) {
            type(JOSEObjectType(SupportedFormat.SD_JWT.value))
            x509CertChain(keyPair.parsedX509CertChain.map { Base64.encode(it.encoded) })
        }
        return issuer.issue(sdJwtSpec).getOrThrow().serialize()
    }

    fun issueMDocPid(pid: Credential.MdocPidCredential, keyAttestation: KeyAttestation): String {
        val deviceKeyInfo = DeviceKeyInfo(
            DataElement.fromCBOR(
                OneKey(keyAttestation.jwk.toECKey().toPublicKey(), null).AsCBOR().EncodeToBytes()
            )
        )
        val mdoc = MDocBuilder(DocType.PID.uri)

        mdoc.addItemToSign(Namespace.EU_EUROPA_EC_EUDI_PID_1.uri, "family_name", StringElement(pid.familyName))
        mdoc.addItemToSign(Namespace.EU_EUROPA_EC_EUDI_PID_1.uri, "given_name", StringElement(pid.givenName))
        mdoc.addItemToSign(Namespace.EU_EUROPA_EC_EUDI_PID_1.uri, "birth_date", FullDateElement(pid.birthDate))
        mdoc.addItemToSign(Namespace.EU_EUROPA_EC_EUDI_PID_1.uri, "birth_place", StringElement(pid.birthPlace))
        mdoc.addItemToSign(Namespace.EU_EUROPA_EC_EUDI_PID_1.uri, "nationality", StringElement(pid.nationality))
        pid.residentAddress?.let {
            mdoc.addItemToSign(Namespace.EU_EUROPA_EC_EUDI_PID_1.uri, "resident_address", StringElement(it))
        }
        pid.residentCountry?.let {
            mdoc.addItemToSign(Namespace.EU_EUROPA_EC_EUDI_PID_1.uri, "resident_country", StringElement(it))
        }
        pid.residentCity?.let {
            mdoc.addItemToSign(Namespace.EU_EUROPA_EC_EUDI_PID_1.uri, "resident_city", StringElement(it))
        }
        pid.residentState?.let {
            mdoc.addItemToSign(Namespace.EU_EUROPA_EC_EUDI_PID_1.uri, "resident_state", StringElement(it))
        }
        pid.residentPostalCode?.let {
            mdoc.addItemToSign(Namespace.EU_EUROPA_EC_EUDI_PID_1.uri, "resident_postal_code", StringElement(it))
        }
        pid.residentStreet?.let {
            mdoc.addItemToSign(Namespace.EU_EUROPA_EC_EUDI_PID_1.uri, "resident_street", StringElement(it))
        }
        pid.residentHouseNumber?.let {
            mdoc.addItemToSign(Namespace.EU_EUROPA_EC_EUDI_PID_1.uri, "resident_house_number", StringElement(it))
        }
        mdoc.addItemToSign(
            Namespace.EU_EUROPA_EC_EUDI_PID_1.uri,
            "personal_administrative_number",
            StringElement(pid.familyName)
        )
        pid.portrait?.let {
            mdoc.addItemToSign(Namespace.EU_EUROPA_EC_EUDI_PID_1.uri, "portrait", ByteStringElement(it))
        }
        mdoc.addItemToSign(Namespace.EU_EUROPA_EC_EUDI_PID_1.uri, "given_name_birth", StringElement(pid.givenNameBirth))
        mdoc.addItemToSign(
            Namespace.EU_EUROPA_EC_EUDI_PID_1.uri,
            "family_name_birth",
            StringElement(pid.familyNameBirth)
        )
        pid.sex?.let {
            mdoc.addItemToSign(Namespace.EU_EUROPA_EC_EUDI_PID_1.uri, "sex", NumberElement(it))
        }
        pid.emailAddress?.let {
            mdoc.addItemToSign(Namespace.EU_EUROPA_EC_EUDI_PID_1.uri, "email_address", StringElement(it))
        }
        pid.mobilePhoneNumber?.let {
            mdoc.addItemToSign(Namespace.EU_EUROPA_EC_EUDI_PID_1.uri, "mobile_phone_number", StringElement(it))
        }
        mdoc.addItemToSign(Namespace.EU_EUROPA_EC_EUDI_PID_1.uri, "issuance_date", FullDateElement(pid.issuanceDate))
        mdoc.addItemToSign(Namespace.EU_EUROPA_EC_EUDI_PID_1.uri, "expiry_date", FullDateElement(pid.expiryDate))
        mdoc.addItemToSign(
            Namespace.EU_EUROPA_EC_EUDI_PID_1.uri,
            "issuing_authority",
            StringElement(pid.issuingAuthority)
        )
        mdoc.addItemToSign(Namespace.EU_EUROPA_EC_EUDI_PID_1.uri, "issuing_country", StringElement(pid.issuingCountry))
        pid.issuingJurisdiction?.let {
            mdoc.addItemToSign(Namespace.EU_EUROPA_EC_EUDI_PID_1.uri, "issuing_jurisdiction", StringElement(it))
        }
        mdoc.addItemToSign(Namespace.EU_EUROPA_EC_EUDI_PID_1.uri, "age_over_16", BooleanElement(pid.ageOver16))
        mdoc.addItemToSign(Namespace.EU_EUROPA_EC_EUDI_PID_1.uri, "age_over_18", BooleanElement(pid.ageOver18))
        mdoc.addItemToSign(Namespace.EU_EUROPA_EC_EUDI_PID_1.uri, "age_over_21", BooleanElement(pid.ageOver21))
        pid.ageInYears?.let {
            mdoc.addItemToSign(Namespace.EU_EUROPA_EC_EUDI_PID_1.uri, "age_in_years", NumberElement(pid.ageInYears))
        }
        pid.ageBirthYear?.let {
            mdoc.addItemToSign(Namespace.EU_EUROPA_EC_EUDI_PID_1.uri, "age_birth_year", NumberElement(pid.ageBirthYear))
        }
        pid.documentNumber?.let {
            mdoc.addItemToSign(Namespace.EU_EUROPA_EC_EUDI_PID_1.uri, "document_number", StringElement(it))
        }

        val status = Status(
            statusList = StatusListInfo(
                index = 123456u,
                uri = "https://raw.githubusercontent.com/open-eid/eudi-wallet-poc/refs/heads/master/statuslists/1" // TODO: Use official status list
            )
        )

        return mdoc.sign(
            ValidityInfo(Clock.System.now(), Instant.fromEpochSeconds(1740045600L), Instant.fromEpochSeconds(1771581600L)),
            deviceKeyInfo,
            pidIssuerCryptoProvider(),
            MOCK_PID_ISSUER_KEY_ID,
            status
        ).toCBORHex()
    }

    fun issueMDocMdl(mdl: Credential.MdocMdlCredential, keyAttestation: KeyAttestation): String {
        val deviceKeyInfo = DeviceKeyInfo(
            DataElement.fromCBOR(
                OneKey(keyAttestation.jwk.toECKey().toPublicKey(), null).AsCBOR().EncodeToBytes()
            )
        )

        fun getDrivingPrivileges(mdl: Credential.MdocMdlCredential): ListElement {
            return ListElement(
                mdl.drivingPrivileges.map { dp ->
                    MapElement(
                        mapOf(
                            MapKey("vehicle_category_code") to StringElement(dp.vehicleCategoryCode),
                            *listOfNotNull(
                                dp.issueDate?.let { MapKey("issue_date") to FullDateElement(it) },
                                dp.expiryDate?.let { MapKey("expiry_date") to FullDateElement(it) },
                                dp.codes?.takeIf { it.isNotEmpty() }?.let {
                                    MapKey("codes") to ListElement(
                                        it.map { code ->
                                            MapElement(
                                                buildMap {
                                                    put(MapKey("code"), StringElement(code.code))
                                                    code.sign?.let { put(MapKey("sign"), StringElement(it)) }
                                                    code.value?.let { put(MapKey("value"), StringElement(it)) }
                                                }
                                            )
                                        }
                                    )
                                }
                            ).toTypedArray()
                        )
                    )
                }
            )
        }

        val mdoc = MDocBuilder(DocType.MDL.uri)
            .addItemToSign(Namespace.ORG_ISO_18013_5_1.uri, "family_name", StringElement(mdl.familyName))
            .addItemToSign(Namespace.ORG_ISO_18013_5_1.uri, "given_name", StringElement(mdl.givenName))
            .addItemToSign(Namespace.ORG_ISO_18013_5_1.uri, "birth_date", FullDateElement(mdl.birthDate))
            .addItemToSign(Namespace.ORG_ISO_18013_5_1.uri, "issue_date", FullDateElement(mdl.issueDate))
            .addItemToSign(Namespace.ORG_ISO_18013_5_1.uri, "expiry_date", FullDateElement(mdl.expiryDate))
            .addItemToSign(Namespace.ORG_ISO_18013_5_1.uri, "issuing_country", StringElement(mdl.issuingCountry))
            .addItemToSign(Namespace.ORG_ISO_18013_5_1.uri, "issuing_authority", StringElement(mdl.issuingAuthority))
            .addItemToSign(Namespace.ORG_ISO_18013_5_1.uri, "document_number", StringElement(mdl.documentNumber))
            .addItemToSign(Namespace.ORG_ISO_18013_5_1.uri, "portrait", ByteStringElement(mdl.portrait))
            .addItemToSign(Namespace.ORG_ISO_18013_5_1.uri, "driving_privileges", getDrivingPrivileges(mdl))
            .addItemToSign(
                Namespace.ORG_ISO_18013_5_1.uri,
                "un_distinguishing_sign",
                StringElement(mdl.unDistinguishingSign)
            )

        mdl.administrativeNumber?.let {
            mdoc.addItemToSign(Namespace.ORG_ISO_18013_5_1.uri, "administrative_number", StringElement(it))
        }
        mdl.sex?.let {
            mdoc.addItemToSign(Namespace.ORG_ISO_18013_5_1.uri, "sex", NumberElement(it))
        }
        mdl.height?.let {
            mdoc.addItemToSign(Namespace.ORG_ISO_18013_5_1.uri, "height", NumberElement(it))
        }
        mdl.weight?.let {
            mdoc.addItemToSign(Namespace.ORG_ISO_18013_5_1.uri, "weight", NumberElement(it))
        }
        mdl.eyeColor?.let {
            mdoc.addItemToSign(Namespace.ORG_ISO_18013_5_1.uri, "eye_colour", StringElement(it))
        }
        mdl.hairColor?.let {
            mdoc.addItemToSign(Namespace.ORG_ISO_18013_5_1.uri, "hair_colour", StringElement(it))
        }
        mdl.birthPlace?.let {
            mdoc.addItemToSign(Namespace.ORG_ISO_18013_5_1.uri, "birth_place", StringElement(it))
        }
        mdl.residentAddress?.let {
            mdoc.addItemToSign(Namespace.ORG_ISO_18013_5_1.uri, "resident_address", StringElement(it))
        }
        mdl.portraitCaptureDate?.let {
            mdoc.addItemToSign(Namespace.ORG_ISO_18013_5_1.uri, "portrait_capture_date", FullDateElement(it))
        }
        mdl.ageInYears?.let {
            mdoc.addItemToSign(Namespace.ORG_ISO_18013_5_1.uri, "age_in_years", NumberElement(it))
        }
        mdl.ageBirthYear?.let {
            mdoc.addItemToSign(Namespace.ORG_ISO_18013_5_1.uri, "age_birth_year", NumberElement(it))
        }
        mdl.ageOver16?.let {
            mdoc.addItemToSign(Namespace.ORG_ISO_18013_5_1.uri, "age_over_16", BooleanElement(it))
        }
        mdl.ageOver18?.let {
            mdoc.addItemToSign(Namespace.ORG_ISO_18013_5_1.uri, "age_over_18", BooleanElement(it))
        }
        mdl.ageOver21?.let {
            mdoc.addItemToSign(Namespace.ORG_ISO_18013_5_1.uri, "age_over_21", BooleanElement(it))
        }
        mdl.issuingJurisdiction?.let {
            mdoc.addItemToSign(Namespace.ORG_ISO_18013_5_1.uri, "issuing_jurisdiction", StringElement(it))
        }
        mdl.nationality?.let {
            mdoc.addItemToSign(Namespace.ORG_ISO_18013_5_1.uri, "nationality", StringElement(it))
        }
        mdl.residentCity?.let {
            mdoc.addItemToSign(Namespace.ORG_ISO_18013_5_1.uri, "resident_city", StringElement(it))
        }
        mdl.residentState?.let {
            mdoc.addItemToSign(Namespace.ORG_ISO_18013_5_1.uri, "resident_state", StringElement(it))
        }
        mdl.residentPostalCode?.let {
            mdoc.addItemToSign(Namespace.ORG_ISO_18013_5_1.uri, "resident_postal_code", StringElement(it))
        }
        mdl.residentCountry?.let {
            mdoc.addItemToSign(Namespace.ORG_ISO_18013_5_1.uri, "resident_country", StringElement(it))
        }
        mdl.biometricTemplateFace?.let {
            mdoc.addItemToSign(Namespace.ORG_ISO_18013_5_1.uri, "biometric_template_face", ByteStringElement(it))
        }
        mdl.biometricTemplateFinger?.let {
            mdoc.addItemToSign(Namespace.ORG_ISO_18013_5_1.uri, "biometric_template_finger", ByteStringElement(it))
        }
        mdl.biometricTemplateSignatureSign?.let {
            mdoc.addItemToSign(
                Namespace.ORG_ISO_18013_5_1.uri,
                "biometric_template_signature_sign",
                ByteStringElement(it)
            )
        }
        mdl.biometricTemplateIris?.let {
            mdoc.addItemToSign(Namespace.ORG_ISO_18013_5_1.uri, "biometric_template_iris", ByteStringElement(it))
        }
        mdl.familyNameNationalCharacter?.let {
            mdoc.addItemToSign(Namespace.ORG_ISO_18013_5_1.uri, "family_name_national_character", StringElement(it))
        }
        mdl.givenNameNationalCharacter?.let {
            mdoc.addItemToSign(Namespace.ORG_ISO_18013_5_1.uri, "given_name_national_character", StringElement(it))
        }
        mdl.signatureUsualMark?.let {
            mdoc.addItemToSign(Namespace.ORG_ISO_18013_5_1.uri, "signature_usual_mark", ByteStringElement(it))
        }

        val status = Status(
            statusList = StatusListInfo(
                index = 654321u,
                uri = "https://raw.githubusercontent.com/open-eid/eudi-wallet-poc/refs/heads/master/statuslists/2" // TODO: Use official status list
            )
        )

        return mdoc.sign(
            ValidityInfo(Clock.System.now(), Instant.fromEpochSeconds(1740045600L), Instant.fromEpochSeconds(1771581600L)),
            deviceKeyInfo,
            mdlIssuerCryptoProvider(),
            MOCK_MDL_ISSUER_KEY_ID,
            status
        ).toCBORHex()
    }

    fun pidIssuerCryptoProvider(): SimpleCOSECryptoProvider {
        val keyPair = pidIssuerKeyPair()
        return SimpleCOSECryptoProvider(
            listOf(
                COSECryptoProviderKeyInfo(
                    keyID = keyPair.keyID,
                    algorithmID = AlgorithmID.ECDSA_256,
                    publicKey = keyPair.toPublicKey(),
                    privateKey = keyPair.toPrivateKey(),
                    x5Chain = keyPair.parsedX509CertChain,
                    trustedRootCAs = emptyList()
                )
            )
        )
    }

    fun mdlIssuerCryptoProvider(): SimpleCOSECryptoProvider {
        val keyPair = mdlIssuerKeyPair()
        return SimpleCOSECryptoProvider(
            listOf(
                COSECryptoProviderKeyInfo(
                    keyID = keyPair.keyID,
                    algorithmID = AlgorithmID.ECDSA_256,
                    publicKey = keyPair.toPublicKey(),
                    privateKey = keyPair.toPrivateKey(),
                    x5Chain = keyPair.parsedX509CertChain,
                    trustedRootCAs = emptyList()
                )
            )
        )
    }

    fun pidIssuerKeyPair(): ECKey {
        val keyStore = KeyStore.getInstance("PKCS12")
        context.assets.open("keys/doc_signer_pid_issuer.p12").use { inputStream ->
            keyStore.load(inputStream, "changeit".toCharArray())
        }
        return ECKey.load(keyStore, "pid_issuer", "changeit".toCharArray())
    }

    fun mdlIssuerKeyPair(): ECKey {
        val keyStore = KeyStore.getInstance("PKCS12")
        context.assets.open("keys/doc_signer_mdl_issuer.p12").use { inputStream ->
            keyStore.load(inputStream, "changeit".toCharArray())
        }
        return ECKey.load(keyStore, "mdl_issuer", "changeit".toCharArray())
    }
}
