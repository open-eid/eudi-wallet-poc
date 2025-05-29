package ee.cyber.wallet.domain

import androidx.annotation.StringRes
import ee.cyber.wallet.R

enum class AppError(
    @StringRes val resId: Int
) {
    UNKNOWN_ERROR(R.string.error_presentation_unknown_error),
    CONNECTION_ERROR(R.string.error_presentation_rp_connection_failed),
    PRESENTATION_FETCH_REQUEST_ERROR(R.string.error_presentation_verification_error),
    PRESENTATION_RESPONSE_ERROR(R.string.error_presentation_response_error),
    PRESENTATION_VERIFIER_REJECTED_ERROR(R.string.error_presentation_verifier_error),
    PRESENTATION_UNSUPPORTED_REQUEST_ERROR(R.string.error_presentation_unsupported_request_type),
    PRESENTATION_MATCH_ERROR(R.string.error_presentation_match_error),
    PRESENTATION_INCORRECT_PIN_ERROR(R.string.error_presentation_incorrect_pin),
    ISSUANCE_AUTHORIZATION_ERROR(R.string.error_issuance_authorization_error),
    ISSUANCE_ERROR(R.string.error_issuance_common_error),
    USER_CANCELLED(R.string.error_presentation_user_cancelled)
}
