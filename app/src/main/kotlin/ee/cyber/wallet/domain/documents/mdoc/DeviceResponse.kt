package ee.cyber.wallet.domain.documents.mdoc

import cbor.Cbor
import id.walt.mdoc.dataelement.MapElement
import id.walt.mdoc.dataelement.MapKey
import id.walt.mdoc.dataelement.NumberElement
import id.walt.mdoc.dataelement.StringElement
import id.walt.mdoc.dataelement.toDataElement
import id.walt.mdoc.dataretrieval.DeviceResponseStatus
import id.walt.mdoc.doc.MDoc
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.decodeFromHexString
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Device response data structure containing MDocs presented by device
 */
@Serializable
class DeviceResponse(
    val documents: List<MDoc>,
    val version: StringElement = "1.0".toDataElement(),
    val status: NumberElement = DeviceResponseStatus.OK.status.toDataElement()
    // waltid has invalid implementation of documentErrors, which according to ISO/IEC 18013-5:2021 is of type ListElement
    // As a workaround this field is removed to avoid parsing error.
    // val documentErrors: MapElement? = null
) {
    /**
     * Convert to CBOR map element
     */
    fun toMapElement() = MapElement(
        buildMap {
            put(MapKey("version"), version)
            put(MapKey("documents"), documents.map { it.toMapElement() }.toDataElement())
            put(MapKey("status"), status)
        }
    )

    /**
     * Serialize to CBOR data
     */
    fun toCBOR() = toMapElement().toCBOR()

    /**
     * Serialize to CBOR hex string
     */
    fun toCBORHex() = toMapElement().toCBORHex()

    /**
     * Serialize to CBOR base64 url-encoded string
     */
    @OptIn(ExperimentalEncodingApi::class)
    fun toCBORBase64URL() = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT).encode(toCBOR())

    companion object {
        /**
         * Deserialize from CBOR data
         */
        @OptIn(ExperimentalSerializationApi::class)
        fun fromCBOR(cbor: ByteArray) = Cbor.decodeFromByteArray<DeviceResponse>(cbor)

        /**
         * Deserialize from CBOR hex string
         */
        @OptIn(ExperimentalSerializationApi::class)
        fun fromCBORHex(cbor: String) = Cbor.decodeFromHexString<DeviceResponse>(cbor)

        @OptIn(ExperimentalEncodingApi::class)
        fun fromCBORBase64URL(cbor: String) = fromCBOR(Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT_OPTIONAL).decode(cbor))
    }
}
