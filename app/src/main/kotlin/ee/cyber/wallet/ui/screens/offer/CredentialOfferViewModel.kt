package ee.cyber.wallet.ui.screens.offer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.cyber.wallet.domain.credentials.CredentialAttribute
import ee.cyber.wallet.domain.credentials.CredentialType
import ee.cyber.wallet.domain.credentials.DocType
import ee.cyber.wallet.domain.credentials.Namespace
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class UiState(
    val credentialType: CredentialType,
    val pidAttributes: List<CredentialAttribute>,
    val bindingToken: String? = null
)

@HiltViewModel
class CredentialOfferViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bindingToken = savedStateHandle.get<String?>("bindingToken")

    private val credentialType = savedStateHandle.get<CredentialType>("type") ?: CredentialType.PID_SD_JWT

    private val pidAttributes = when (credentialType) {
        CredentialType.PID_SD_JWT -> CredentialAttribute.entries.filter {
            (
                it != CredentialAttribute.JWT_PID_1_BIRTHDATE &&
                    it != CredentialAttribute.JWT_PID_1_PLACE_OF_BIRTH_LOCALITY &&
                    it != CredentialAttribute.JWT_PID_1_ADDRESS_FORMATTED &&
                    it != CredentialAttribute.JWT_PID_1_ADDRESS_COUNTRY &&
                    it != CredentialAttribute.JWT_PID_1_ADDRESS_LOCALITY &&
                    it != CredentialAttribute.JWT_PID_1_ADDRESS_REGION &&
                    it != CredentialAttribute.JWT_PID_1_ADDRESS_POSTAL_CODE &&
                    it != CredentialAttribute.JWT_PID_1_ADDRESS_STREET_ADDRESS &&
                    it != CredentialAttribute.JWT_PID_1_ADDRESS_HOUSE_NUMBER &&
                    it != CredentialAttribute.JWT_PID_1_PICTURE &&
                    it != CredentialAttribute.JWT_PID_1_BIRTH_GIVEN_NAME &&
                    it != CredentialAttribute.JWT_PID_1_BIRTH_FAMILY_NAME &&
                    it != CredentialAttribute.JWT_PID_1_EMAIL &&
                    it != CredentialAttribute.JWT_PID_1_PHONE_NUMBER &&
                    it != CredentialAttribute.JWT_PID_1_DATE_OF_ISSUANCE &&
                    it != CredentialAttribute.JWT_PID_1_DATE_OF_EXPIRY &&
                    it != CredentialAttribute.JWT_PID_1_AGE_EQUAL_OR_OVER_16 &&
                    it != CredentialAttribute.JWT_PID_1_AGE_EQUAL_OR_OVER_18 &&
                    it != CredentialAttribute.JWT_PID_1_AGE_EQUAL_OR_OVER_21 &&
                    it != CredentialAttribute.JWT_PID_1_NATIONALITIES &&
                    it != CredentialAttribute.JWT_PID_1_PLACE_OF_BIRTH &&
                    it != CredentialAttribute.JWT_PID_1_ADDRESS &&
                    it != CredentialAttribute.JWT_PID_1_AGE_EQUAL_OR_OVER
                ) && it.docType == DocType.PID_SD_JWT && it.namespace == Namespace.NONE && it.disclosable
        }

        CredentialType.PID_MDOC -> CredentialAttribute.entries.filter {
            it.docType == DocType.PID && it.namespace == Namespace.EU_EUROPA_EC_EUDI_PID_1 && it.disclosable
        }

        CredentialType.MDL -> CredentialAttribute.entries.filter {
            it.docType == DocType.MDL && it.namespace == Namespace.ORG_ISO_18013_5_1 && it.disclosable
        }

        CredentialType.AGE_VERIFICATION -> CredentialAttribute.entries.filter {
            it.docType == DocType.AGE_VERIFICATION && it.namespace == Namespace.EU_EUROPA_EC_EUDI_AGE_VERIFICATION_1 && it.disclosable
        }
    }.sortedBy { it.ordinal }

    private val _state = MutableStateFlow(
        UiState(
            credentialType = credentialType,
            pidAttributes = pidAttributes,
            bindingToken = bindingToken
        )
    )

    val state = _state.asStateFlow()
}
