package ee.cyber.wallet.ui.screens.documents

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ee.cyber.wallet.R
import ee.cyber.wallet.domain.credentials.CredentialAttribute
import ee.cyber.wallet.domain.credentials.CredentialType
import ee.cyber.wallet.domain.credentials.DocType
import ee.cyber.wallet.domain.documents.CredentialDocument
import ee.cyber.wallet.domain.documents.DocumentField

@Composable
fun String.humanReadableValue() = if (this == "true") stringResource(R.string.yes) else this

@Composable
fun DocumentField.label(docType: DocType): String = CredentialAttribute.find(namespace, name, docType)?.label() ?: name

@Composable
fun CredentialAttribute.label(): String = when (this) {
    CredentialAttribute.JWT_PID_1_PERSONAL_ADMINISTRATIVE_NUMBER,
    CredentialAttribute.MDOC_PID_1_PERSONAL_ADMINISTRATIVE_NUMBER -> stringResource(R.string.attr_identification_nr)

    CredentialAttribute.ORG_ISO_18013_5_1_ADMINISTRATIVE_NUMBER -> stringResource(R.string.attr_administrative_number)

    CredentialAttribute.JWT_PID_1_GIVEN_NAME,
    CredentialAttribute.MDOC_PID_1_GIVEN_NAME,
    CredentialAttribute.ORG_ISO_18013_5_1_GIVEN_NAME -> stringResource(R.string.attr_given_name)

    CredentialAttribute.JWT_PID_1_FAMILY_NAME,
    CredentialAttribute.MDOC_PID_1_FAMILY_NAME,
    CredentialAttribute.ORG_ISO_18013_5_1_FAMILY_NAME -> stringResource(R.string.attr_family_name)

    CredentialAttribute.JWT_PID_1_BIRTHDATE,
    CredentialAttribute.JWT_PID_1_BIRTH_DATE,
    CredentialAttribute.MDOC_PID_1_BIRTH_DATE,
    CredentialAttribute.ORG_ISO_18013_5_1_BIRTH_DATE -> stringResource(R.string.attr_date_of_birth)

    CredentialAttribute.JWT_PID_1_DATE_OF_ISSUANCE,
    CredentialAttribute.JWT_PID_1_ISSUANCE_DATE,
    CredentialAttribute.MDOC_PID_1_ISSUANCE_DATE,
    CredentialAttribute.ORG_ISO_18013_5_1_ISSUE_DATE -> stringResource(R.string.attr_issuance_date)

    CredentialAttribute.JWT_PID_1_ISSUING_AUTHORITY,
    CredentialAttribute.MDOC_PID_1_ISSUING_AUTHORITY,
    CredentialAttribute.ORG_ISO_18013_5_1_ISSUING_AUTHORITY -> stringResource(R.string.attr_issuing_authority)

    CredentialAttribute.JWT_PID_1_ISSUING_COUNTRY,
    CredentialAttribute.MDOC_PID_1_ISSUING_COUNTRY,
    CredentialAttribute.ORG_ISO_18013_5_1_ISSUING_COUNTRY -> stringResource(R.string.attr_issuing_country)

    CredentialAttribute.JWT_PID_1_DOCUMENT_NUMBER,
    CredentialAttribute.MDOC_PID_1_DOCUMENT_NUMBER,
    CredentialAttribute.ORG_ISO_18013_5_1_DOCUMENT_NUMBER -> stringResource(R.string.attr_document_number)

    CredentialAttribute.JWT_PID_1_DATE_OF_EXPIRY,
    CredentialAttribute.JWT_PID_1_EXPIRY_DATE,
    CredentialAttribute.MDOC_PID_1_EXPIRY_DATE,
    CredentialAttribute.ORG_ISO_18013_5_1_EXPIRY_DATE -> stringResource(R.string.attr_expiry_date)

    CredentialAttribute.ORG_ISO_18013_5_1_GIVEN_NAME_NATIONAL_CHARACTER -> stringResource(R.string.attr_given_name_national_character)
    CredentialAttribute.ORG_ISO_18013_5_1_FAMILY_NAME_NATIONAL_CHARACTER -> stringResource(R.string.attr_family_name_national_character)

    CredentialAttribute.JWT_PID_1_PLACE_OF_BIRTH_LOCALITY,
    CredentialAttribute.JWT_PID_1_BIRTH_PLACE,
    CredentialAttribute.MDOC_PID_1_BIRTH_PLACE,
    CredentialAttribute.ORG_ISO_18013_5_1_BIRTH_PLACE -> stringResource(R.string.attr_birth_place)

    CredentialAttribute.JWT_PID_1_PICTURE,
    CredentialAttribute.JWT_PID_1_PORTRAIT,
    CredentialAttribute.MDOC_PID_1_PORTRAIT,
    CredentialAttribute.ORG_ISO_18013_5_1_PORTRAIT -> stringResource(R.string.attr_portrait)

    CredentialAttribute.ORG_ISO_18013_5_1_SIGNATURE_USUAL_MARK -> stringResource(R.string.attr_signature_usual_mark)
    CredentialAttribute.ORG_ISO_18013_5_1_DRIVING_PRIVILEGES -> stringResource(R.string.attr_driving_privileges)
    CredentialAttribute.ORG_ISO_18013_5_1_UN_DISTINGUISHING_SIGN -> stringResource(R.string.attr_un_distinguishing_sign)

    CredentialAttribute.JWT_PID_1_BIRTH_GIVEN_NAME,
    CredentialAttribute.JWT_PID_1_GIVEN_NAME_BIRTH,
    CredentialAttribute.MDOC_PID_1_GIVEN_NAME_BIRTH -> stringResource(R.string.attr_given_name_birth)

    CredentialAttribute.JWT_PID_1_BIRTH_FAMILY_NAME,
    CredentialAttribute.JWT_PID_1_FAMILY_NAME_BIRTH,
    CredentialAttribute.MDOC_PID_1_FAMILY_NAME_BIRTH -> stringResource(R.string.attr_family_name_birth)

    CredentialAttribute.ORG_ISO_18013_5_1_NATIONALITY,
    CredentialAttribute.JWT_PID_1_NATIONALITIES,
    CredentialAttribute.JWT_PID_1_NATIONALITY,
    CredentialAttribute.MDOC_PID_1_NATIONALITY -> stringResource(R.string.attr_nationality)

    CredentialAttribute.ORG_ISO_18013_5_1_RESIDENT_ADDRESS,
    CredentialAttribute.JWT_PID_1_ADDRESS_FORMATTED,
    CredentialAttribute.JWT_PID_1_RESIDENT_ADDRESS,
    CredentialAttribute.MDOC_PID_1_RESIDENT_ADDRESS -> stringResource(R.string.attr_resident_address)

    CredentialAttribute.ORG_ISO_18013_5_1_RESIDENT_COUNTRY,
    CredentialAttribute.JWT_PID_1_ADDRESS_COUNTRY,
    CredentialAttribute.JWT_PID_1_RESIDENT_COUNTRY,
    CredentialAttribute.MDOC_PID_1_RESIDENT_COUNTRY -> stringResource(R.string.attr_resident_country)

    CredentialAttribute.ORG_ISO_18013_5_1_RESIDENT_CITY,
    CredentialAttribute.JWT_PID_1_RESIDENT_CITY,
    CredentialAttribute.JWT_PID_1_ADDRESS_LOCALITY,
    CredentialAttribute.MDOC_PID_1_RESIDENT_CITY -> stringResource(R.string.attr_resident_city)

    CredentialAttribute.ORG_ISO_18013_5_1_RESIDENT_STATE,
    CredentialAttribute.JWT_PID_1_ADDRESS_REGION,
    CredentialAttribute.JWT_PID_1_RESIDENT_REGION,
    CredentialAttribute.MDOC_PID_1_RESIDENT_STATE -> stringResource(R.string.attr_resident_state)

    CredentialAttribute.ORG_ISO_18013_5_1_RESIDENT_POSTAL_CODE,
    CredentialAttribute.JWT_PID_1_ADDRESS_POSTAL_CODE,
    CredentialAttribute.JWT_PID_1_RESIDENT_POSTAL_CODE,
    CredentialAttribute.MDOC_PID_1_RESIDENT_POSTAL_CODE -> stringResource(R.string.attr_resident_postal_code)

    CredentialAttribute.JWT_PID_1_ADDRESS_STREET_ADDRESS,
    CredentialAttribute.JWT_PID_1_RESIDENT_STREET,
    CredentialAttribute.MDOC_PID_1_RESIDENT_STREET -> stringResource(R.string.attr_resident_street)

    CredentialAttribute.JWT_PID_1_ADDRESS_HOUSE_NUMBER,
    CredentialAttribute.JWT_PID_1_RESIDENT_HOUSE_NUMBER,
    CredentialAttribute.MDOC_PID_1_RESIDENT_HOUSE_NUMBER -> stringResource(R.string.attr_resident_house_number)

    CredentialAttribute.ORG_ISO_18013_5_1_SEX,
    CredentialAttribute.JWT_PID_1_SEX,
    CredentialAttribute.MDOC_PID_1_SEX -> stringResource(R.string.attr_sex)

    CredentialAttribute.JWT_PID_1_EMAIL,
    CredentialAttribute.JWT_PID_1_EMAIL_ADDRESS,
    CredentialAttribute.MDOC_PID_1_EMAIL_ADDRESS -> stringResource(R.string.attr_email_address)

    CredentialAttribute.JWT_PID_1_PHONE_NUMBER,
    CredentialAttribute.JWT_PID_1_MOBILE_PHONE_NUMBER,
    CredentialAttribute.MDOC_PID_1_MOBILE_PHONE_NUMBER -> stringResource(R.string.attr_mobile_phone_number)

    CredentialAttribute.ORG_ISO_18013_5_1_ISSUING_JURISDICTION,
    CredentialAttribute.JWT_PID_1_ISSUING_JURISDICTION,
    CredentialAttribute.MDOC_PID_1_ISSUING_JURISDICTION -> stringResource(R.string.attr_issuing_jurisdiction)

    CredentialAttribute.ORG_ISO_18013_5_1_AGE_OVER_16,
    CredentialAttribute.JWT_PID_1_AGE_EQUAL_OR_OVER_16,
    CredentialAttribute.JWT_PID_1_AGE_OVER_16,
    CredentialAttribute.MDOC_PID_1_AGE_OVER_16 -> stringResource(R.string.attr_age_over_16)

    CredentialAttribute.JWT_PID_1_AGE_EQUAL_OR_OVER_18,
    CredentialAttribute.JWT_PID_1_AGE_OVER_18,
    CredentialAttribute.MDOC_PID_1_AGE_OVER_18,
    CredentialAttribute.ORG_ISO_18013_5_1_AGE_OVER_18 -> stringResource(R.string.attr_age_over_18)

    CredentialAttribute.ORG_ISO_18013_5_1_AGE_OVER_21,
    CredentialAttribute.JWT_PID_1_AGE_EQUAL_OR_OVER_21,
    CredentialAttribute.JWT_PID_1_AGE_OVER_21,
    CredentialAttribute.MDOC_PID_1_AGE_OVER_21 -> stringResource(R.string.attr_age_over_21)

    CredentialAttribute.ORG_ISO_18013_5_1_AGE_IN_YEARS,
    CredentialAttribute.JWT_PID_1_AGE_IN_YEARS,
    CredentialAttribute.MDOC_PID_1_AGE_IN_YEARS -> stringResource(R.string.attr_age_in_years)

    CredentialAttribute.ORG_ISO_18013_5_1_AGE_BIRTH_YEAR,
    CredentialAttribute.JWT_PID_1_AGE_BIRTH_YEAR,
    CredentialAttribute.MDOC_PID_1_AGE_BIRTH_YEAR -> stringResource(R.string.attr_age_birth_year)

    CredentialAttribute.ORG_ISO_18013_5_1_HEIGHT -> stringResource(R.string.attr_height)
    CredentialAttribute.ORG_ISO_18013_5_1_WEIGHT -> stringResource(R.string.attr_weight)
    CredentialAttribute.ORG_ISO_18013_5_1_EYE_COLOUR -> stringResource(R.string.attr_eye_colour)
    CredentialAttribute.ORG_ISO_18013_5_1_HAIR_COLOUR -> stringResource(R.string.attr_hair_colour)
    CredentialAttribute.ORG_ISO_18013_5_1_PORTRAIT_CAPTURE_DATE -> stringResource(R.string.attr_portrait_capture_date)
    CredentialAttribute.ORG_ISO_18013_5_1_BIOMETRIC_TEMPLATE_FACE -> stringResource(R.string.attr_biometric_template_face)
    CredentialAttribute.ORG_ISO_18013_5_1_BIOMETRIC_TEMPLATE_FINGER -> stringResource(R.string.attr_biometric_template_finger)
    CredentialAttribute.ORG_ISO_18013_5_1_BIOMETRIC_TEMPLATE_SIGNATURE_SIGN -> stringResource(R.string.attr_biometric_template_signature_sign)
    CredentialAttribute.ORG_ISO_18013_5_1_BIOMETRIC_TEMPLATE_IRIS -> stringResource(R.string.attr_biometric_template_iris)
    CredentialAttribute.JWT_PID_1_TRUST_ANCHOR,
    CredentialAttribute.MDOC_PID_1_TRUST_ANCHOR -> stringResource(R.string.attr_trust_anchor)
    CredentialAttribute.JWT_PID_1_PLACE_OF_BIRTH -> "required for field matching"
    CredentialAttribute.JWT_PID_1_ADDRESS -> "required for field matching"
    CredentialAttribute.JWT_PID_1_AGE_EQUAL_OR_OVER -> "required for field matching"
}

@Composable
fun CredentialType.docTypeName() = when (this) {
    CredentialType.PID_SD_JWT -> stringResource(R.string.doc_type_estonian_digital_id)
    CredentialType.PID_MDOC -> stringResource(R.string.doc_type_estonian_digital_id)
    CredentialType.MDL -> stringResource(R.string.doc_type_digital_driving_licence)
}

fun CredentialType.docType() = when (this) {
    CredentialType.PID_SD_JWT -> DocType.PID_SD_JWT
    CredentialType.PID_MDOC -> DocType.PID
    CredentialType.MDL -> DocType.MDL
}

@Composable
fun DocType.docTypeName() = when (this) {
    DocType.PID_SD_JWT -> stringResource(R.string.doc_type_estonian_digital_id)
    DocType.PID -> stringResource(R.string.doc_type_estonian_digital_id)
    DocType.MDL -> stringResource(R.string.doc_type_digital_driving_licence)
}

@Composable
fun CredentialType.issuerName() = when (this) {
    CredentialType.PID_SD_JWT -> stringResource(R.string.issuer_pid_sdjwt)
    CredentialType.PID_MDOC -> stringResource(R.string.issuer_pid_mdoc)
    CredentialType.MDL -> stringResource(R.string.issuer_mdl_mdoc)
}

@Composable
fun CredentialType.docTypeNameWithFormat() = when (this) {
    CredentialType.PID_SD_JWT -> "${stringResource(R.string.doc_type_estonian_digital_id)} (SD-JWT)"
    CredentialType.PID_MDOC -> "${stringResource(R.string.doc_type_estonian_digital_id)} (MDoc)"
    CredentialType.MDL -> "${stringResource(R.string.doc_type_digital_driving_licence)} (MDoc)"
}

fun CredentialDocument.credentialType(): CredentialType = when (this) {
    is CredentialDocument.JwtDocument -> if (type == DocType.PID_SD_JWT) CredentialType.PID_SD_JWT else throw IllegalStateException()
    is CredentialDocument.MDocDocument -> if (type == DocType.PID) CredentialType.PID_MDOC else CredentialType.MDL
}

@Composable
fun DocType.docTypeDescription(): String = when (this) {
    DocType.PID_SD_JWT -> stringResource(R.string.doc_type_primary_description)
    DocType.PID -> stringResource(R.string.doc_type_primary_description)
    DocType.MDL -> stringResource(R.string.doc_type_right_to_drive_description)
}

fun DocumentField.asCredentialAttribute(docType: DocType) = CredentialAttribute.entries.find { it.namespace == namespace && it.fieldName == name && it.docType == docType }

fun DocumentField.isExpiry() = when (name) {
    CredentialAttribute.JWT_PID_1_DATE_OF_EXPIRY.fieldName,
    CredentialAttribute.MDOC_PID_1_EXPIRY_DATE.fieldName,
    CredentialAttribute.ORG_ISO_18013_5_1_EXPIRY_DATE.fieldName -> true

    else -> false
}
