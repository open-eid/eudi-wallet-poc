package ee.cyber.wallet.ui.screens.presentation

import eu.europa.ec.eudi.openid4vp.Client
import eu.europa.ec.eudi.openid4vp.JarmRequirement
import eu.europa.ec.eudi.openid4vp.ResponseMode
import eu.europa.ec.eudi.openid4vp.VpFormat
import eu.europa.ec.eudi.prex.PresentationDefinition
import java.io.Serializable

data class OpenId4VPAuthorizationRequest(
    val client: Client,
    val responseMode: ResponseMode,
    val state: String?,
    val nonce: String,
    val jarmRequirement: JarmRequirement?,
    val vpFormats: List<VpFormat>,
    val presentationDefinition: PresentationDefinition
) : Serializable
