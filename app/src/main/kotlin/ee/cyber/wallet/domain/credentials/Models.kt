package ee.cyber.wallet.domain.credentials

import com.fasterxml.jackson.annotation.JsonProperty
import eu.europa.ec.eudi.openid4vci.AuthorizationRequestPrepared
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

enum class CredentialType(val value: String) {
    PID_SD_JWT("eu.europa.ec.eudiw.pid_vc_sd_jwt"), // TODO: eu.europa.ec.eudi.pid_vc_sd_jwt?
    PID_MDOC("eu.europa.ec.eudiw.pid_mso_mdoc"), // TODO: eu.europa.ec.eudi.pid_mso_mdoc?
    MDL("org.iso.18013.5.1.mDL")
}

enum class DocType(val uri: String) {
    PID_SD_JWT("urn:eudi:pid:1"),
    PID("eu.europa.ec.eudi.pid.1"),
    MDL("org.iso.18013.5.1.mDL")
}

enum class Namespace(val uri: String) {
    EU_EUROPA_EC_EUDI_PID_1("eu.europa.ec.eudi.pid.1"),
    EU_EUROPA_EC_EUDI_PID_EE_1("eu.europa.ec.eudi.pid.ee.1"),
    ORG_ISO_18013_5_1("org.iso.18013.5.1"),
    NONE("");

    companion object {
        fun byUri(uri: String): Namespace? = entries.firstOrNull { it.uri == uri }
    }
}

enum class CredentialAttribute(
    val docType: DocType,
    val namespace: Namespace,
    val fieldName: String,
    val disclosable: Boolean
) {
    /*
        PID (SD-JWT)
     */
    JWT_PID_1_FAMILY_NAME(DocType.PID_SD_JWT, Namespace.NONE, "family_name", true),
    JWT_PID_1_GIVEN_NAME(DocType.PID_SD_JWT, Namespace.NONE, "given_name", true),
    JWT_PID_1_BIRTHDATE(DocType.PID_SD_JWT, Namespace.NONE, "birthdate", true), // ARF PID rulebook
    JWT_PID_1_BIRTH_DATE(DocType.PID_SD_JWT, Namespace.NONE, "birth_date", true), // EU 2024/2977 regulation
    JWT_PID_1_PLACE_OF_BIRTH(DocType.PID_SD_JWT, Namespace.NONE, "place_of_birth", true),
    JWT_PID_1_PLACE_OF_BIRTH_LOCALITY(DocType.PID_SD_JWT, Namespace.NONE, "place_of_birth.locality", true), // ARF PID rulebook
    JWT_PID_1_BIRTH_PLACE(DocType.PID_SD_JWT, Namespace.NONE, "birth_place", true), // EU 2024/2977 regulation
    JWT_PID_1_NATIONALITIES(DocType.PID_SD_JWT, Namespace.NONE, "nationalities", true),
    JWT_PID_1_ADDRESS(DocType.PID_SD_JWT, Namespace.NONE, "address", true), // ARF PID rulebook
    JWT_PID_1_NATIONALITY(DocType.PID_SD_JWT, Namespace.NONE, "nationality", true), // EU 2024/2977 regulation
    JWT_PID_1_ADDRESS_FORMATTED(DocType.PID_SD_JWT, Namespace.NONE, "address.formatted", true), // ARF PID rulebook
    JWT_PID_1_RESIDENT_ADDRESS(DocType.PID_SD_JWT, Namespace.NONE, "resident_address", true),
    JWT_PID_1_ADDRESS_COUNTRY(DocType.PID_SD_JWT, Namespace.NONE, "address.country", true), // ARF PID rulebook
    JWT_PID_1_RESIDENT_COUNTRY(DocType.PID_SD_JWT, Namespace.NONE, "resident_country", true),
    JWT_PID_1_ADDRESS_LOCALITY(DocType.PID_SD_JWT, Namespace.NONE, "address.locality", true), // ARF PID rulebook
    JWT_PID_1_RESIDENT_CITY(DocType.PID_SD_JWT, Namespace.NONE, "resident_city", true), // EU 2024/2977 regulation
    JWT_PID_1_ADDRESS_REGION(DocType.PID_SD_JWT, Namespace.NONE, "address.region", true), // ARF PID rulebook
    JWT_PID_1_RESIDENT_REGION(DocType.PID_SD_JWT, Namespace.NONE, "resident_state", true), // EU 2024/2977 regulation
    JWT_PID_1_ADDRESS_POSTAL_CODE(DocType.PID_SD_JWT, Namespace.NONE, "address.postal_code", true), // ARF PID rulebook
    JWT_PID_1_RESIDENT_POSTAL_CODE(DocType.PID_SD_JWT, Namespace.NONE, "resident_postal_code", true), // EU 2024/2977 regulation
    JWT_PID_1_ADDRESS_STREET_ADDRESS(DocType.PID_SD_JWT, Namespace.NONE, "address.street_address", true), // ARF PID rulebook
    JWT_PID_1_RESIDENT_STREET(DocType.PID_SD_JWT, Namespace.NONE, "resident_street", true), // EU 2024/2977 regulation
    JWT_PID_1_ADDRESS_HOUSE_NUMBER(DocType.PID_SD_JWT, Namespace.NONE, "address.house_number", true), // ARF PID rulebook
    JWT_PID_1_RESIDENT_HOUSE_NUMBER(DocType.PID_SD_JWT, Namespace.NONE, "resident_house_number", true), // EU 2024/2977 regulation
    JWT_PID_1_PERSONAL_ADMINISTRATIVE_NUMBER(DocType.PID_SD_JWT, Namespace.NONE, "personal_administrative_number", true),
    JWT_PID_1_PICTURE(DocType.PID_SD_JWT, Namespace.NONE, "picture", true), // ARF PID rulebook
    JWT_PID_1_PORTRAIT(DocType.PID_SD_JWT, Namespace.NONE, "portrait", true), // EU 2024/2977 regulation
    JWT_PID_1_BIRTH_GIVEN_NAME(DocType.PID_SD_JWT, Namespace.NONE, "birth_given_name", true), // ARF PID rulebook
    JWT_PID_1_GIVEN_NAME_BIRTH(DocType.PID_SD_JWT, Namespace.NONE, "given_name_birth", true), // EU 2024/2977 regulation
    JWT_PID_1_BIRTH_FAMILY_NAME(DocType.PID_SD_JWT, Namespace.NONE, "birth_family_name", true), // ARF PID rulebook
    JWT_PID_1_FAMILY_NAME_BIRTH(DocType.PID_SD_JWT, Namespace.NONE, "family_name_birth", true), // EU 2024/2977 regulation
    JWT_PID_1_SEX(DocType.PID_SD_JWT, Namespace.NONE, "sex", true),
    JWT_PID_1_EMAIL(DocType.PID_SD_JWT, Namespace.NONE, "email", true), // ARF PID rulebook
    JWT_PID_1_EMAIL_ADDRESS(DocType.PID_SD_JWT, Namespace.NONE, "email_address", true),
    JWT_PID_1_PHONE_NUMBER(DocType.PID_SD_JWT, Namespace.NONE, "phone_number", true), // ARF PID rulebook
    JWT_PID_1_MOBILE_PHONE_NUMBER(DocType.PID_SD_JWT, Namespace.NONE, "mobile_phone_number", true), // EU 2024/2977 regulation
    JWT_PID_1_DATE_OF_ISSUANCE(DocType.PID_SD_JWT, Namespace.NONE, "date_of_issuance", true), // ARF PID rulebook
    JWT_PID_1_ISSUANCE_DATE(DocType.PID_SD_JWT, Namespace.NONE, "issuance_date", true), // EU 2024/2977 regulation
    JWT_PID_1_DATE_OF_EXPIRY(DocType.PID_SD_JWT, Namespace.NONE, "date_of_expiry", true), // ARF PID rulebook
    JWT_PID_1_EXPIRY_DATE(DocType.PID_SD_JWT, Namespace.NONE, "expiry_date", true), // EU 2024/2977 regulation
    JWT_PID_1_ISSUING_AUTHORITY(DocType.PID_SD_JWT, Namespace.NONE, "issuing_authority", true),
    JWT_PID_1_ISSUING_COUNTRY(DocType.PID_SD_JWT, Namespace.NONE, "issuing_country", true),
    JWT_PID_1_ISSUING_JURISDICTION(DocType.PID_SD_JWT, Namespace.NONE, "issuing_jurisdiction", true),
    JWT_PID_1_AGE_EQUAL_OR_OVER(DocType.PID_SD_JWT, Namespace.NONE, "age_equal_or_over", true),
    JWT_PID_1_AGE_EQUAL_OR_OVER_16(DocType.PID_SD_JWT, Namespace.NONE, "age_equal_or_over.16", true), // ARF PID rulebook
    JWT_PID_1_AGE_OVER_16(DocType.PID_SD_JWT, Namespace.NONE, "age_over_16", true), // EU 2024/2977 regulation
    JWT_PID_1_AGE_EQUAL_OR_OVER_18(DocType.PID_SD_JWT, Namespace.NONE, "age_equal_or_over.18", true), // ARF PID rulebook
    JWT_PID_1_AGE_OVER_18(DocType.PID_SD_JWT, Namespace.NONE, "age_over_18", true), // EU 2024/2977 regulation
    JWT_PID_1_AGE_EQUAL_OR_OVER_21(DocType.PID_SD_JWT, Namespace.NONE, "age_equal_or_over.21", true), // ARF PID rulebook
    JWT_PID_1_AGE_OVER_21(DocType.PID_SD_JWT, Namespace.NONE, "age_over_21", true), // EU 2024/2977 regulation
    JWT_PID_1_AGE_IN_YEARS(DocType.PID_SD_JWT, Namespace.NONE, "age_in_years", true),
    JWT_PID_1_AGE_BIRTH_YEAR(DocType.PID_SD_JWT, Namespace.NONE, "age_birth_year", true),
    JWT_PID_1_DOCUMENT_NUMBER(DocType.PID_SD_JWT, Namespace.NONE, "document_number", true),
    JWT_PID_1_TRUST_ANCHOR(DocType.PID_SD_JWT, Namespace.NONE, "trust_anchor", true),

    /*
        PID (MDOC)
     */
    MDOC_PID_1_FAMILY_NAME(DocType.PID, Namespace.EU_EUROPA_EC_EUDI_PID_1, "family_name", true),
    MDOC_PID_1_GIVEN_NAME(DocType.PID, Namespace.EU_EUROPA_EC_EUDI_PID_1, "given_name", true),
    MDOC_PID_1_BIRTH_DATE(DocType.PID, Namespace.EU_EUROPA_EC_EUDI_PID_1, "birth_date", true),
    MDOC_PID_1_BIRTH_PLACE(DocType.PID, Namespace.EU_EUROPA_EC_EUDI_PID_1, "birth_place", true),
    MDOC_PID_1_NATIONALITY(DocType.PID, Namespace.EU_EUROPA_EC_EUDI_PID_1, "nationality", true),
    MDOC_PID_1_RESIDENT_ADDRESS(DocType.PID, Namespace.EU_EUROPA_EC_EUDI_PID_1, "resident_address", true),
    MDOC_PID_1_RESIDENT_COUNTRY(DocType.PID, Namespace.EU_EUROPA_EC_EUDI_PID_1, "resident_country", true),
    MDOC_PID_1_RESIDENT_STATE(DocType.PID, Namespace.EU_EUROPA_EC_EUDI_PID_1, "resident_state", true),
    MDOC_PID_1_RESIDENT_CITY(DocType.PID, Namespace.EU_EUROPA_EC_EUDI_PID_1, "resident_city", true),
    MDOC_PID_1_RESIDENT_POSTAL_CODE(DocType.PID, Namespace.EU_EUROPA_EC_EUDI_PID_1, "resident_postal_code", true),
    MDOC_PID_1_RESIDENT_STREET(DocType.PID, Namespace.EU_EUROPA_EC_EUDI_PID_1, "resident_street", true),
    MDOC_PID_1_RESIDENT_HOUSE_NUMBER(DocType.PID, Namespace.EU_EUROPA_EC_EUDI_PID_1, "resident_house_number", true),
    MDOC_PID_1_PERSONAL_ADMINISTRATIVE_NUMBER(DocType.PID, Namespace.EU_EUROPA_EC_EUDI_PID_1, "personal_administrative_number", true),
    MDOC_PID_1_PORTRAIT(DocType.PID, Namespace.EU_EUROPA_EC_EUDI_PID_1, "portrait", true),
    MDOC_PID_1_GIVEN_NAME_BIRTH(DocType.PID, Namespace.EU_EUROPA_EC_EUDI_PID_1, "given_name_birth", true),
    MDOC_PID_1_FAMILY_NAME_BIRTH(DocType.PID, Namespace.EU_EUROPA_EC_EUDI_PID_1, "family_name_birth", true),
    MDOC_PID_1_SEX(DocType.PID, Namespace.EU_EUROPA_EC_EUDI_PID_1, "sex", true),
    MDOC_PID_1_EMAIL_ADDRESS(DocType.PID, Namespace.EU_EUROPA_EC_EUDI_PID_1, "email_address", true),
    MDOC_PID_1_MOBILE_PHONE_NUMBER(DocType.PID, Namespace.EU_EUROPA_EC_EUDI_PID_1, "mobile_phone_number", true),
    MDOC_PID_1_ISSUANCE_DATE(DocType.PID, Namespace.EU_EUROPA_EC_EUDI_PID_1, "issuance_date", true),
    MDOC_PID_1_EXPIRY_DATE(DocType.PID, Namespace.EU_EUROPA_EC_EUDI_PID_1, "expiry_date", true),
    MDOC_PID_1_ISSUING_AUTHORITY(DocType.PID, Namespace.EU_EUROPA_EC_EUDI_PID_1, "issuing_authority", true),
    MDOC_PID_1_ISSUING_COUNTRY(DocType.PID, Namespace.EU_EUROPA_EC_EUDI_PID_1, "issuing_country", true),
    MDOC_PID_1_ISSUING_JURISDICTION(DocType.PID, Namespace.EU_EUROPA_EC_EUDI_PID_1, "issuing_jurisdiction", true),
    MDOC_PID_1_AGE_OVER_16(DocType.PID, Namespace.EU_EUROPA_EC_EUDI_PID_1, "age_over_16", true),
    MDOC_PID_1_AGE_OVER_18(DocType.PID, Namespace.EU_EUROPA_EC_EUDI_PID_1, "age_over_18", true),
    MDOC_PID_1_AGE_OVER_21(DocType.PID, Namespace.EU_EUROPA_EC_EUDI_PID_1, "age_over_21", true),
    MDOC_PID_1_AGE_IN_YEARS(DocType.PID, Namespace.EU_EUROPA_EC_EUDI_PID_1, "age_in_years", true),
    MDOC_PID_1_AGE_BIRTH_YEAR(DocType.PID, Namespace.EU_EUROPA_EC_EUDI_PID_1, "age_birth_year", true),
    MDOC_PID_1_DOCUMENT_NUMBER(DocType.PID, Namespace.EU_EUROPA_EC_EUDI_PID_1, "document_number", true),
    MDOC_PID_1_TRUST_ANCHOR(DocType.PID, Namespace.EU_EUROPA_EC_EUDI_PID_1, "trust_anchor", true),

    /*
        MDL
     */
    ORG_ISO_18013_5_1_FAMILY_NAME(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "family_name", true),
    ORG_ISO_18013_5_1_GIVEN_NAME(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "given_name", true),
    ORG_ISO_18013_5_1_BIRTH_DATE(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "birth_date", true),
    ORG_ISO_18013_5_1_ISSUE_DATE(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "issue_date", true),
    ORG_ISO_18013_5_1_EXPIRY_DATE(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "expiry_date", true),
    ORG_ISO_18013_5_1_ISSUING_COUNTRY(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "issuing_country", true),
    ORG_ISO_18013_5_1_ISSUING_AUTHORITY(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "issuing_authority", true),
    ORG_ISO_18013_5_1_DOCUMENT_NUMBER(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "document_number", true),
    ORG_ISO_18013_5_1_PORTRAIT(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "portrait", true),
    ORG_ISO_18013_5_1_DRIVING_PRIVILEGES(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "driving_privileges", true),
    ORG_ISO_18013_5_1_UN_DISTINGUISHING_SIGN(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "un_distinguishing_sign", true),
    ORG_ISO_18013_5_1_ADMINISTRATIVE_NUMBER(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "administrative_number", true),
    ORG_ISO_18013_5_1_SEX(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "sex", true),
    ORG_ISO_18013_5_1_HEIGHT(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "height", true),
    ORG_ISO_18013_5_1_WEIGHT(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "weight", true),
    ORG_ISO_18013_5_1_EYE_COLOUR(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "eye_colour", true),
    ORG_ISO_18013_5_1_HAIR_COLOUR(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "hair_colour", true),
    ORG_ISO_18013_5_1_BIRTH_PLACE(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "birth_place", true),
    ORG_ISO_18013_5_1_RESIDENT_ADDRESS(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "resident_address", true),
    ORG_ISO_18013_5_1_PORTRAIT_CAPTURE_DATE(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "portrait_capture_date", true),
    ORG_ISO_18013_5_1_AGE_IN_YEARS(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "age_in_years", true),
    ORG_ISO_18013_5_1_AGE_BIRTH_YEAR(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "age_birth_year", true),
    ORG_ISO_18013_5_1_AGE_OVER_16(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "age_over_16", true),
    ORG_ISO_18013_5_1_AGE_OVER_18(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "age_over_18", true),
    ORG_ISO_18013_5_1_AGE_OVER_21(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "age_over_21", true),
    ORG_ISO_18013_5_1_ISSUING_JURISDICTION(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "issuing_jurisdiction", true),
    ORG_ISO_18013_5_1_NATIONALITY(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "nationality", true),
    ORG_ISO_18013_5_1_RESIDENT_CITY(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "resident_city", true),
    ORG_ISO_18013_5_1_RESIDENT_STATE(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "resident_state", true),
    ORG_ISO_18013_5_1_RESIDENT_POSTAL_CODE(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "resident_postal_code", true),
    ORG_ISO_18013_5_1_RESIDENT_COUNTRY(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "resident_country", true),
    ORG_ISO_18013_5_1_BIOMETRIC_TEMPLATE_FACE(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "biometric_template_face", true),
    ORG_ISO_18013_5_1_BIOMETRIC_TEMPLATE_FINGER(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "biometric_template_finger", true),
    ORG_ISO_18013_5_1_BIOMETRIC_TEMPLATE_SIGNATURE_SIGN(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "biometric_template_signature_sign", true),
    ORG_ISO_18013_5_1_BIOMETRIC_TEMPLATE_IRIS(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "biometric_template_iris", true),
    ORG_ISO_18013_5_1_FAMILY_NAME_NATIONAL_CHARACTER(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "family_name_national_character", true),
    ORG_ISO_18013_5_1_GIVEN_NAME_NATIONAL_CHARACTER(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "given_name_national_character", true),
    ORG_ISO_18013_5_1_SIGNATURE_USUAL_MARK(DocType.MDL, Namespace.ORG_ISO_18013_5_1, "signature_usual_mark", true);

    val presentationDefinitionPath by lazy {
        if (namespace == Namespace.NONE) {
            "$.$fieldName"
        } else {
            "$['${namespace.uri}']['$fieldName']"
        }
    }

    companion object {
        fun find(namespace: Namespace, fieldName: String, docType: DocType): CredentialAttribute? =
            entries.find { it.namespace == namespace && it.fieldName == fieldName && it.docType == docType }

        fun findByPath(path: String): CredentialAttribute? = entries.find { it.presentationDefinitionPath == path }
    }
}

fun CredentialAttribute.isGivenName() = this in listOf(
    CredentialAttribute.JWT_PID_1_GIVEN_NAME,
    CredentialAttribute.MDOC_PID_1_GIVEN_NAME
)

fun CredentialAttribute.isFamilyName() = this in listOf(
    CredentialAttribute.JWT_PID_1_FAMILY_NAME,
    CredentialAttribute.MDOC_PID_1_FAMILY_NAME
)

@Serializable
sealed class Credential(
    val type: CredentialType
) {

    @Suppress("ArrayInDataClass")
    data class JwtArfPidCredential(
        val familyName: String,
        val givenName: String,
        val birthdate: LocalDate,
        val placeOfBirth: PlaceOfBirth,
        val nationalities: List<String>,
        val address: Address? = null,
        val personalAdministrativeNumber: String,
        val picture: ByteArray? = null,
        val birthGivenName: String,
        val birthFamilyName: String,
        val sex: Int? = null,
        val email: String? = null,
        val phoneNumber: String? = null,
        val dateOfIssuance: LocalDate,
        val dateOfExpiry: LocalDate,
        val issuingAuthority: String,
        val issuingCountry: String,
        val issuingJurisdiction: String? = null,
        val ageEqualOrOver: AgeEqualOrOver?,
        val ageInYears: Int? = null,
        val ageBirthYear: Int? = null,
        val documentNumber: String? = null,
        val trustAnchor: String? = null
    ) : Credential(
        type = CredentialType.PID_SD_JWT
    )

    data class PlaceOfBirth(
        val country: String? = null,
        val region: String? = null,
        val locality: String? = null
    )

    data class Address(
        val formatted: String? = null,
        val country: String? = null,
        val region: String? = null,
        val locality: String? = null,
        val postalCode: String? = null,
        val streetAddress: String? = null,
        val houseNumber: String? = null
    )

    data class AgeEqualOrOver(
        @JsonProperty("16")
        val is16: Boolean,
        @JsonProperty("18")
        val is18: Boolean,
        @JsonProperty("21")
        val is21: Boolean
    )

    open class JwtPidCredential(
        open val familyName: String,
        open val givenName: String,
        open val birthDate: LocalDate,
        val birthPlace: String,
        open val nationality: String,
        open val residentAddress: String? = null,
        open val residentCountry: String? = null,
        open val residentState: String? = null,
        open val residentCity: String? = null,
        open val residentPostalCode: String? = null,
        open val residentStreet: String? = null,
        open val residentHouseNumber: String? = null,
        open val personalAdministrativeNumber: String,
        open val portrait: ByteArray? = null,
        open val givenNameBirth: String,
        open val familyNameBirth: String,
        open val sex: Int? = null,
        open val emailAddress: String? = null,
        open val mobilePhoneNumber: String? = null,
        open val issuanceDate: LocalDate,
        open val expiryDate: LocalDate,
        open val issuingAuthority: String,
        open val issuingCountry: String,
        open val issuingJurisdiction: String? = null,
        open val ageOver16: Boolean,
        open val ageOver18: Boolean,
        open val ageOver21: Boolean,
        open val ageInYears: Int? = null,
        open val ageBirthYear: Int? = null,
        open val documentNumber: String? = null,
        open val trustAnchor: String? = null

    ) : Credential(
        type = CredentialType.PID_SD_JWT
    )

    data class MdocPidCredential(
        val familyName: String,
        val givenName: String,
        val birthDate: LocalDate,
        val birthPlace: String,
        val nationality: String,
        val residentAddress: String? = null,
        val residentCountry: String? = null,
        val residentState: String? = null,
        val residentCity: String? = null,
        val residentPostalCode: String? = null,
        val residentStreet: String? = null,
        val residentHouseNumber: String? = null,
        val personalAdministrativeNumber: String,
        val portrait: ByteArray? = null,
        val givenNameBirth: String,
        val familyNameBirth: String,
        val sex: Int? = null,
        val emailAddress: String? = null,
        val mobilePhoneNumber: String? = null,
        val issuanceDate: LocalDate,
        val expiryDate: LocalDate,
        val issuingAuthority: String,
        val issuingCountry: String,
        val issuingJurisdiction: String? = null,
        val ageOver16: Boolean,
        val ageOver18: Boolean,
        val ageOver21: Boolean,
        val ageInYears: Int? = null,
        val ageBirthYear: Int? = null,
        val documentNumber: String? = null,
        val trustAnchor: String? = null
    ) : Credential(
        type = CredentialType.PID_MDOC
    )

    data class MdocMdlCredential(
        val familyName: String,
        val givenName: String,
        val birthDate: LocalDate,
        val issueDate: LocalDate,
        val expiryDate: LocalDate,
        val issuingCountry: String,
        val issuingAuthority: String,
        val documentNumber: String,
        val portrait: ByteArray,
        val drivingPrivileges: List<DrivingPrivilege>,
        val unDistinguishingSign: String,
        val administrativeNumber: String? = null,
        val sex: Int? = null,
        val height: Int? = null,
        val weight: Int? = null,
        val eyeColor: String? = null,
        val hairColor: String? = null,
        val birthPlace: String? = null,
        val residentAddress: String? = null,
        val portraitCaptureDate: LocalDate? = null,
        val ageInYears: Int? = null,
        val ageBirthYear: Int? = null,
        val ageOver16: Boolean? = null,
        val ageOver18: Boolean? = null,
        val ageOver21: Boolean? = null,
        val issuingJurisdiction: String? = null,
        val nationality: String? = null,
        val residentCity: String? = null,
        val residentState: String? = null,
        val residentPostalCode: String? = null,
        val residentCountry: String? = null,
        val biometricTemplateFace: ByteArray? = null,
        val biometricTemplateFinger: ByteArray? = null,
        val biometricTemplateSignatureSign: ByteArray? = null,
        val biometricTemplateIris: ByteArray? = null,
        val familyNameNationalCharacter: String? = null,
        val givenNameNationalCharacter: String? = null,
        val signatureUsualMark: ByteArray? = null
    ) : Credential(
        type = CredentialType.MDL
    )

    data class DrivingPrivilege(
        val vehicleCategoryCode: String,
        val issueDate: LocalDate? = null,
        val expiryDate: LocalDate? = null,
        val codes: List<Code>? = null
    ) {
        data class Code(
            val code: String,
            val sign: String? = null,
            val value: String? = null
        )
    }
}

data class IssuanceAuthorizationState(
    val request: AuthorizationRequestPrepared,
    val keyId: String
)
