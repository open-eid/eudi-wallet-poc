package ee.cyber.wallet.domain.documents.mdoc

import id.walt.mdoc.dataelement.BooleanElement
import id.walt.mdoc.dataelement.ByteStringElement
import id.walt.mdoc.dataelement.DataElement
import id.walt.mdoc.dataelement.DateTimeElement
import id.walt.mdoc.dataelement.EncodedCBORElement
import id.walt.mdoc.dataelement.FullDateElement
import id.walt.mdoc.dataelement.ListElement
import id.walt.mdoc.dataelement.MapElement
import id.walt.mdoc.dataelement.NullElement
import id.walt.mdoc.dataelement.NumberElement
import id.walt.mdoc.dataelement.StringElement
import id.walt.mdoc.mdocauth.DeviceAuthentication
import org.kotlincrypto.hash.sha2.SHA256
import java.security.SecureRandom
import java.util.Base64

fun DataElement.value(): Any? = when (this) {
    is ListElement -> this.value.map { it.value() }.joinToString(", ")
    is ByteStringElement -> Base64.getEncoder().encodeToString(this.value)
    else -> when (this) {
        is NumberElement -> this.value
        is BooleanElement -> this.value
        is StringElement -> this.value
        is NullElement -> this.value
        is DateTimeElement -> this.value
        is FullDateElement -> this.value
        is MapElement -> this.value
        is EncodedCBORElement -> this.value
        else -> throw IllegalArgumentException("Unknown DataElement type")
    }
}

object MDocUtils {

    @JvmStatic
    fun generateMdocGeneratedNonce(): String {
        val secureRandom = SecureRandom()
        val bytes = ByteArray(16)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    @JvmStatic
    fun getDeviceAuthentication(clientId: String, responseUri: String, nonce: String, mdocNonce: String, docType: String): DeviceAuthentication {
        val sessionTranscript = ListElement(
            listOf(
                NullElement(),
                NullElement(),
                generateMDocOID4VPHandover(clientId, responseUri, nonce, mdocNonce)
            )
        )
        val deviceNameSpaces = EncodedCBORElement(MapElement(mapOf()))
        return DeviceAuthentication(sessionTranscript, docType, deviceNameSpaces)
    }

    /**
     * Generate OID4VPHandover for session transcript of MDoc device authentication, as defined in ISO-18013-7 Annex B, B.4.4
     * @param authorizationRequest OpenID4VP presentation request
     * @param mdocNonce MDoc generated random nonce
     */
    @JvmStatic
    fun generateMDocOID4VPHandover(clientId: String, responseUri: String, nonce: String, mdocNonce: String): ListElement {
        val clientIdToHash = ListElement(listOf(StringElement(clientId), StringElement(mdocNonce)))
        val responseUriToHash = ListElement(listOf(StringElement(responseUri), StringElement(mdocNonce)))

        return ListElement(
            listOf(
                ByteStringElement(SHA256().digest(clientIdToHash.toCBOR())),
                ByteStringElement(SHA256().digest(responseUriToHash.toCBOR())),
                StringElement(nonce)
            )
        )
    }
}
