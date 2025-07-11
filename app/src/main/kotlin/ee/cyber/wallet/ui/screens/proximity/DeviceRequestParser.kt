package ee.cyber.wallet.ui.screens.proximity

import com.android.identity.cbor.Bstr
import com.android.identity.cbor.Cbor
import com.android.identity.cbor.CborArray
import com.android.identity.cbor.DataItem
import com.android.identity.cbor.Tagged
import com.android.identity.cose.Cose
import com.android.identity.cose.CoseNumberLabel
import com.android.identity.crypto.Algorithm
import com.android.identity.crypto.X509CertChain

/**
 * Helper class for parsing the bytes of `DeviceRequest`
 * [CBOR](http://cbor.io/)
 * as specified in ISO/IEC 18013-5:2021 section 8.3 Device Retrieval.
 *
 * @param encodedDeviceRequest the bytes of the `DeviceRequest` CBOR.
 * @param encodedSessionTranscript the bytes of `SessionTranscript`.
 */
class DeviceRequestParser(
    private val encodedDeviceRequest: ByteArray,
    private val encodedSessionTranscript: ByteArray
) {
    private var skipReaderAuthParseAndCheck = false

    /**
     * Sets a flag to skip force skip parsing the reader auth structure.
     *
     * This flag is useful when the user knows that:
     * - they will ignore the reader auth result (optional in 18013-5)
     * - and explicitly don't want to parse it
     *
     * For example, if this code is to be used in production and there is uncertainty about which
     * devices will have which security providers, and there is concern about running into parsing
     * / validating issues.
     *
     * By default this value is set to false.
     *
     * @param skipReaderAuthParseAndCheck a flag to skip force skip parsing the reader auth structure.
     * @return the `DeviceRequestParser`.
     */
    fun setSkipReaderAuthParseAndCheck(skipReaderAuthParseAndCheck: Boolean) = apply {
        this.skipReaderAuthParseAndCheck = skipReaderAuthParseAndCheck
    }

    /**
     * Parses the device request.
     *
     *
     * This parser will successfully parse requests where the request is signed by the reader
     * but the signature check fails. The method [DocRequest.readerAuthenticated]
     * can used to get additional information whether `ItemsRequest` was authenticated.
     *
     * A `GeneralSecurityException` may be thrown if there issues within the default
     * security provider. Use `setSkipReaderAuthParseAndCheck` to skip some usage of
     * security provider in reader auth parsing.
     *
     * @return a [DeviceRequestParser.DeviceRequest] with the parsed data.
     * @throws IllegalArgumentException if the given data isn't valid CBOR or not conforming
     * to the CDDL for its type.
     * @throws IllegalStateException    if required data hasn't been set using the setter
     * methods on this class.
     */
    fun parse(): DeviceRequest = DeviceRequest().apply {
        parse(
            encodedDeviceRequest,
            Cbor.decode(encodedSessionTranscript),
            skipReaderAuthParseAndCheck
        )
    }

    /**
     * An object used to represent data parsed from `DeviceRequest`
     * [CBOR](http://cbor.io/)
     * as specified in *ISO/IEC 18013-5* section 8.3 *Device Retrieval*.
     */
    class DeviceRequest internal constructor() {
        private val _docRequests = mutableListOf<DocRequest>()

        /**
         * The document requests in the `DeviceRequest` CBOR.
         */
        val docRequests: List<DocRequest>
            get() = _docRequests

        /**
         * The version string set in the `DeviceRequest` CBOR.
         */
        lateinit var version: String

        internal fun parse(
            encodedDeviceRequest: ByteArray,
            sessionTranscript: DataItem,
            skipReaderAuthParseAndCheck: Boolean
        ) {
            val request = Cbor.decode(encodedDeviceRequest)
            version = request["version"].asTstr
            require(version.compareTo("1.0") >= 0) { "Given version '$version' not >= '1.0'" }
            var readerCertChain: X509CertChain? = null
            request.getOrNull("docRequests")?.let { docRequests ->
                val docRequestsDataItems = docRequests.asArray
                for (docRequestDataItem in docRequestsDataItems) {
                    val itemsRequestBytesDataItem = docRequestDataItem["itemsRequest"]
                    val itemsRequest = itemsRequestBytesDataItem.asTaggedEncodedCbor
                    val readerAuth = docRequestDataItem.getOrNull("readerAuth")
                    var encodedReaderAuth: ByteArray? = null
                    var readerAuthenticated = false
                    if (!skipReaderAuthParseAndCheck && readerAuth != null) {
                        encodedReaderAuth = Cbor.encode(readerAuth)
                        val readerAuthCoseSign1 = readerAuth.asCoseSign1
                        val readerCertChainDataItem =
                            readerAuthCoseSign1.unprotectedHeaders[CoseNumberLabel(Cose.COSE_LABEL_X5CHAIN)]
                        val signatureAlgorithm = Algorithm.fromInt(
                            readerAuthCoseSign1.protectedHeaders[
                                CoseNumberLabel(Cose.COSE_LABEL_ALG)
                            ]!!.asNumber.toInt()
                        )
                        readerCertChain = readerCertChainDataItem!!.asX509CertChain
                        val readerKey = readerCertChain!!.certificates[0].ecPublicKey
                        val encodedReaderAuthentication = Cbor.encode(
                            CborArray.builder()
                                .add("ReaderAuthentication")
                                .add(sessionTranscript)
                                .add(itemsRequestBytesDataItem)
                                .end()
                                .build()
                        )
                        val readerAuthenticationBytes =
                            Cbor.encode(Tagged(24, Bstr(encodedReaderAuthentication)))
                        readerAuthenticated = Cose.coseSign1Check(
                            readerKey,
                            readerAuthenticationBytes,
                            readerAuthCoseSign1,
                            signatureAlgorithm
                        )
                    }
                    val requestInfo: MutableMap<String, ByteArray> = HashMap()
                    itemsRequest.getOrNull("requestInfo")?.let { requestInfoDataItem ->
                        for (keyDataItem in requestInfoDataItem.asMap.keys) {
                            val key = keyDataItem.asTstr
                            val encodedValue = Cbor.encode(requestInfoDataItem[keyDataItem])
                            requestInfo[key] = encodedValue
                        }
                    }
                    val docType = itemsRequest["docType"].asTstr
                    val builder = DocRequest.Builder(
                        docType,
                        Cbor.encode(itemsRequest),
                        requestInfo,
                        encodedReaderAuth,
                        readerCertChain,
                        readerAuthenticated
                    )

                    // parse nameSpaces
                    val nameSpaces = itemsRequest["nameSpaces"]
                    parseNamespaces(nameSpaces, builder)
                    _docRequests.add(builder.build())
                }
            }
        }

        private fun parseNamespaces(nameSpaces: DataItem, builder: DocRequest.Builder) {
            for ((nameSpaceDataItem, itemsMap) in nameSpaces.asMap) {
                val nameSpace = nameSpaceDataItem.asTstr
                for ((itemKeyDataItem, itemValDataItem) in itemsMap.asMap) {
                    val itemKey = itemKeyDataItem.asTstr
                    val intentToRetain = itemValDataItem.asBoolean
                    builder.addEntry(nameSpace, itemKey, intentToRetain)
                }
            }
        }

        companion object {
            const val TAG = "DeviceRequest"
        }
    }

    /**
     * An object used to represent data parsed from the `DocRequest`
     * [CBOR](http://cbor.io/) (part of `DeviceRequest`)
     * as specified in *ISO/IEC 18013-5* section 8.3 *Device Retrieval*.
     */
    class DocRequest internal constructor(
        val docType: String,
        val itemsRequest: ByteArray,
        val requestInfo: Map<String, ByteArray>,
        val readerAuth: ByteArray?,
        val readerCertificateChain: X509CertChain?,
        val readerAuthenticated: Boolean
    ) {

        internal val requestMap = mutableMapOf<String, MutableMap<String, Boolean>>()
        val namespaces: List<String>
            get() = requestMap.keys.toList()

        /**
         * Gets the names of data elements in the given namespace.
         *
         * @param namespaceName the name of the namespace.
         * @return A list of data element names
         * @throws IllegalArgumentException if the given namespace wasn't requested.
         */
        fun getEntryNames(namespaceName: String): List<String> {
            val innerMap = requestMap[namespaceName]
                ?: throw IllegalArgumentException("Namespace wasn't requested")
            return innerMap.keys.toList()
        }

        /**
         * Gets the intent-to-retain value set by the reader for a data element in the request.
         *
         * @param namespaceName the name of the namespace.
         * @param entryName     the name of the data element
         * @return whether the reader intents to retain the value.
         * @throws IllegalArgumentException if the given data element or namespace wasn't
         * requested.
         */
        fun getIntentToRetain(
            namespaceName: String,
            entryName: String
        ): Boolean {
            val innerMap = requestMap[namespaceName]
                ?: throw IllegalArgumentException("Namespace wasn't requested")
            return innerMap[entryName]
                ?: throw IllegalArgumentException("Data element wasn't requested")
        }

        internal class Builder(
            docType: String,
            encodedItemsRequest: ByteArray,
            requestInfo: Map<String, ByteArray>,
            encodedReaderAuth: ByteArray?,
            readerCertChain: X509CertChain?,
            readerAuthenticated: Boolean
        ) {
            private val result = DocRequest(
                docType,
                encodedItemsRequest,
                requestInfo,
                encodedReaderAuth,
                readerCertChain,
                readerAuthenticated
            )
            fun addEntry(
                namespaceName: String,
                entryName: String,
                intentToRetain: Boolean
            ) = apply {
                var innerMap = result.requestMap[namespaceName]
                if (innerMap == null) {
                    innerMap = mutableMapOf()
                    result.requestMap[namespaceName] = innerMap
                }
                innerMap[entryName] = intentToRetain
            }

            fun build(): DocRequest {
                return result
            }
        }
    }
}
