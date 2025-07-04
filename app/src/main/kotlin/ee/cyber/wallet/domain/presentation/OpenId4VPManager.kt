package ee.cyber.wallet.domain.presentation

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.util.Base64URL
import ee.cyber.wallet.crypto.CryptoProvider
import ee.cyber.wallet.crypto.deviceCryptoProvider
import ee.cyber.wallet.crypto.keyBindingSigner
import ee.cyber.wallet.domain.credentials.CredentialAttribute
import ee.cyber.wallet.domain.documents.CredentialDocument
import ee.cyber.wallet.domain.documents.DocumentField
import ee.cyber.wallet.domain.documents.mdoc.DeviceResponse
import ee.cyber.wallet.domain.documents.mdoc.MDocUtils.getDeviceAuthentication
import ee.cyber.wallet.domain.documents.supportedFormat
import eu.europa.ec.eudi.openid4vp.Consensus
import eu.europa.ec.eudi.openid4vp.DefaultHttpClientFactory
import eu.europa.ec.eudi.openid4vp.EncryptionParameters
import eu.europa.ec.eudi.openid4vp.KtorHttpClientFactory
import eu.europa.ec.eudi.openid4vp.PresentationQuery
import eu.europa.ec.eudi.openid4vp.Resolution
import eu.europa.ec.eudi.openid4vp.ResolvedRequestObject
import eu.europa.ec.eudi.openid4vp.ResponseMode
import eu.europa.ec.eudi.openid4vp.SiopOpenId4VPConfig
import eu.europa.ec.eudi.openid4vp.SiopOpenId4Vp
import eu.europa.ec.eudi.openid4vp.VerifiablePresentation
import eu.europa.ec.eudi.openid4vp.VerifierId
import eu.europa.ec.eudi.openid4vp.VpContent
import eu.europa.ec.eudi.prex.DescriptorMap
import eu.europa.ec.eudi.prex.FieldQueryResult
import eu.europa.ec.eudi.prex.Format
import eu.europa.ec.eudi.prex.Id
import eu.europa.ec.eudi.prex.JsonPath
import eu.europa.ec.eudi.prex.Match
import eu.europa.ec.eudi.prex.PresentationDefinition
import eu.europa.ec.eudi.prex.PresentationExchange
import eu.europa.ec.eudi.prex.PresentationSubmission
import eu.europa.ec.eudi.sdjwt.DefaultSdJwtOps.serializeWithKeyBinding
import eu.europa.ec.eudi.sdjwt.HashAlgorithm
import eu.europa.ec.eudi.sdjwt.NimbusSdJwtOps.kbJwtIssuer
import eu.europa.ec.eudi.sdjwt.SdJwt
import id.walt.mdoc.docrequest.MDocRequestBuilder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.UUID

private val formats = mapOf(
    SupportedFormat.SD_JWT to Format.format(
        buildJsonObject {
            putJsonObject(SupportedFormat.SD_JWT.value) { }
        }
    ),
    SupportedFormat.MSO_MDOC to Format.format(
        buildJsonObject {
            putJsonObject(SupportedFormat.MSO_MDOC.value) { }
        }
    )
)

enum class SupportedFormat(val value: String) {
    SD_JWT("vc+sd-jwt"),
    MSO_MDOC("mso_mdoc")
}

class OpenId4VPManager(
    private val dispatcher: CoroutineDispatcher,
    private val openId4VPConfig: SiopOpenId4VPConfig,
    private val cryptoProviderFactory: CryptoProvider.Factory,
    private val httpClientFactory: KtorHttpClientFactory = DefaultHttpClientFactory
) {

    private val openId4Vp: SiopOpenId4Vp by lazy { SiopOpenId4Vp(openId4VPConfig, httpClientFactory) }

    suspend fun handleRequestUri(uri: String): Resolution = withContext(dispatcher) {
        openId4Vp.resolveRequestUri(uri)
    }

    suspend fun sendResponse(requestObject: ResolvedRequestObject, consensus: Consensus, mdocNonce: String) = withContext(dispatcher) {
        EncryptionParameters.DiffieHellman(Base64URL.encode(mdocNonce)).let { encryptionParameters ->
            openId4Vp.dispatch(requestObject, consensus, encryptionParameters)
        }
    }

    /**
     * Matches credentials against a presentation definition.
     *
     * TODO: The whole combined presentation needs refactoring, due to the unsupported Presentation Exchange submission_requirements feature. The submission_requirements property
     *       defines which Input Descriptors are required for submission, overriding the default input evaluation behavior, in which all Input Descriptors are required.
     *
     * @param presentationDefinition The presentation definition specifying what the verifier wants
     * @param documents List of credential documents that the user has
     * @param disclosedFields Optional list of fields to disclose, defaults to all fields in the documents
     * @return A Pair containing:
     *         - List<CredentialClaim>: The formatted claims derived from the documents
     *         - Match: The match result indicating whether credentials satisfy requirements and which credentials match which requirements. Retrieving the matching document
     *         through the credentialDocument property of the claim whose uniqueId matches the candidateClaimId from the match results:
     *         val candidateClaimId = inputDescriptor.value.keys.first() // This works only for all Input Descriptors evaluator
     *         val credentialDocument = claims.first { it.uniqueId == candidateClaimId }.credentialDocument
     */
    fun matches(
        presentationDefinition: PresentationDefinition,
        documents: List<CredentialDocument>,
        disclosedFields: List<DocumentField> = documents.flatMap { document -> document.fields }.toList()
    ): Pair<List<CredentialClaim>, Match> {
        val claims = documents.map { document ->
            when (document) {
                is CredentialDocument.JwtDocument -> document.asClaim(document, formats[SupportedFormat.SD_JWT]!!, disclosedFields)
                is CredentialDocument.MDocDocument -> document.asClaim(document, formats[SupportedFormat.MSO_MDOC]!!, disclosedFields)
            }
        }
        return claims to PresentationExchange.matcher.match(presentationDefinition, claims)
    }

    private fun CredentialDocument.asClaim(credentialDocument: CredentialDocument, format: Format, disclosedFields: List<DocumentField> = fields): CredentialClaim =
        buildJsonObject {
            put("vct", type.uri)
            put("type", type.uri)
            val associated = disclosedFields
                .filter { field -> CredentialAttribute.find(field.namespace, field.name, type)?.disclosable == true }
                .groupBy { it.namespace.uri }

            associated.forEach {
                if (it.key.isNotEmpty()) {
                    put(
                        it.key,
                        buildJsonObject {
                            it.value.forEach { field ->
                                put(field.name, field.value)
                            }
                        }
                    )
                } else {
                    it.value.forEach { field ->
                        field.element?.let { element ->
                            put(field.name, element)
                        }
                    }
                }
            }
        }.let { json ->
            object : CredentialClaim {
                override val uniqueId = UUID.randomUUID().toString()
                override val format = format
                override fun asJsonString(): String = json.toString()
                override val credentialDocument = credentialDocument
            }
        }

    suspend fun buildConsensus(
        request: ResolvedRequestObject,
        documents: List<CredentialDocument>,
        disclosedFields: List<DocumentField>,
        mdocNonce: String
    ): Consensus = withContext(dispatcher) {
        suspend fun buildOpenId4VPAuthorizationConsensus(
            request: ResolvedRequestObject.OpenId4VPAuthorization,
            documents: List<CredentialDocument>,
            disclosures: List<DocumentField>
        ): Consensus {
            val presentationDefinition = createPresentationDefinition(request.presentationQuery) // request.presentationDefinition?
            // TODO: Figure out how to use disclosedFields.
            // val documentMatches = matches(presentationDefinition, documents, disclosedFields)
            val documentMatches = matches(presentationDefinition, documents)

            when (val match = documentMatches.second) {
                is Match.NotMatched -> return Consensus.NegativeConsensus
                is Match.Matched -> {
                    val claims = documentMatches.first
                    val mdocNonce = mdocNonce
                    val presentations = mutableListOf<String>()
                    val descriptorMaps = mutableListOf<DescriptorMap>()
                    match.matches.onEachIndexed { index, inputDescriptor ->
                        val candidateClaimId = inputDescriptor.value
                            .filter { (_, candidateClaim) ->
                                candidateClaim.matches.any { it.value is FieldQueryResult.CandidateField.Found }
                            }
                            .keys
                            .first()
                        val document = claims.first { it.uniqueId == candidateClaimId }.credentialDocument
                        presentations.add(presentDocument(document, disclosures, request.client.id, request.responseMode, request.nonce, mdocNonce))
                        var path = JsonPath.jsonPath("$[$index]")!!
                        if (match.matches.size == 1) {
                            path = JsonPath.jsonPath("$")!!
                        }
                        val descriptorMap = DescriptorMap(
                            id = inputDescriptor.key,
                            format = document.supportedFormat().value,
                            path = path
                        )
                        descriptorMaps.add(descriptorMap)
                    }

                    return Consensus.PositiveConsensus.VPTokenConsensus(
                        vpContent = VpContent.PresentationExchange(
                            verifiablePresentations = presentations.map { VerifiablePresentation.Generic(it) },
                            presentationSubmission = PresentationSubmission(
                                id = Id(UUID.randomUUID().toString()),
                                definitionId = presentationDefinition.id,
                                descriptorMaps = descriptorMaps
                            )
                        )
                    )
                }
            }
        }

        when (request) {
            is ResolvedRequestObject.OpenId4VPAuthorization -> buildOpenId4VPAuthorizationConsensus(request, documents, disclosedFields)
            else -> Consensus.NegativeConsensus
        }
    }

    private fun createPresentationDefinition(presentationQuery: PresentationQuery): PresentationDefinition {
        return when (presentationQuery) {
            is PresentationQuery.ByPresentationDefinition -> {
                PresentationDefinition(
                    id = presentationQuery.value.id,
                    inputDescriptors = presentationQuery.value.inputDescriptors,
                    format = presentationQuery.value.format
                )
            }
            is PresentationQuery.ByDigitalCredentialsQuery -> {
                throw NotImplementedError()
            }
        }
    }

    private suspend fun presentDocument(
        document: CredentialDocument,
        disclosedFields: List<DocumentField> = document.fields,
        clientId: VerifierId,
        responseMode: ResponseMode,
        nonce: String,
        mdocNonce: String
    ): String = when (document) {
        is CredentialDocument.JwtDocument -> presentSdJwt(document, disclosedFields, clientId, nonce)
        is CredentialDocument.MDocDocument -> presentMDoc(document, disclosedFields, clientId, responseMode, nonce, mdocNonce)
    }

    // TODO: handle not disclosed optional fields
    private suspend fun presentMDoc(
        document: CredentialDocument.MDocDocument,
        disclosedFields: List<DocumentField>,
        verifierId: VerifierId,
        responseMode: ResponseMode,
        nonce: String,
        mdocGeneratedNonce: String
    ): String {
        val docType = document.type.uri
        val mDocRequest = MDocRequestBuilder(docType).apply {
            disclosedFields.forEach {
                addDataElementRequest(it.namespace.uri, it.name, true)
            }
        }.build(null)
        val cryptoProvider = cryptoProviderFactory.forKeyType(document.attestation.keyAttestation.keyType)
        val keyId = document.attestation.keyAttestation.keyId
        val responseURI: String = when (responseMode) {
            is ResponseMode.DirectPostJwt -> responseMode.responseURI.toString()
            is ResponseMode.DirectPost -> responseMode.responseURI.toString()
            is ResponseMode.FragmentJwt -> responseMode.redirectUri.toString()
            is ResponseMode.Fragment -> responseMode.redirectUri.toString()
            is ResponseMode.QueryJwt -> responseMode.redirectUri.toString()
            is ResponseMode.Query -> responseMode.redirectUri.toString()
        }
        // mDoc generated nonce is used for ISO/IEC 18013-7 Annex B
        // and empty string is used for Potential profile.
        val mdocNonce: String = when (responseMode) {
            is ResponseMode.DirectPostJwt -> mdocGeneratedNonce
            is ResponseMode.DirectPost -> ""
            is ResponseMode.FragmentJwt -> mdocGeneratedNonce
            is ResponseMode.Fragment -> ""
            is ResponseMode.QueryJwt -> mdocGeneratedNonce
            is ResponseMode.Query -> ""
        }

        val mdoc = document.mDoc.presentWithDeviceSignature(
            mDocRequest = mDocRequest,
            deviceAuthentication = getDeviceAuthentication(verifierId.clientId, responseURI, nonce, mdocNonce, docType),
            cryptoProvider = cryptoProvider.deviceCryptoProvider(keyId),
            keyID = keyId
        )

        return DeviceResponse(listOf(mdoc)).toCBORBase64URL()
    }

    private suspend fun presentSdJwt(
        document: CredentialDocument.JwtDocument,
        disclosedFields: List<DocumentField>,
        verifierId: VerifierId,
        nonce: String
    ): String {
        val issueTime = Date.from(Instant.now().truncatedTo(ChronoUnit.SECONDS))
        val jwt = document.sdJwt.jwt
        val hashAlg = jwt.second["_sd_alg"]?.jsonPrimitive?.let { HashAlgorithm.fromString(it.content) } ?: HashAlgorithm.SHA_256
        val toBeDisclosed = document.sdJwt.disclosures.filter { disclosure -> disclosedFields.any { it.name == disclosure.claim().first } }
        val presentationSdJwt = SdJwt(jwt, toBeDisclosed)
        val keyManager = cryptoProviderFactory.forKeyType(document.attestation.keyAttestation.keyType)
        val keyBindingSigner = keyManager.keyBindingSigner(document.attestation.keyAttestation.keyId)

        // TODO: Derive JWSAlgorithm from key
        val buildKbJwt = kbJwtIssuer(keyBindingSigner, JWSAlgorithm.ES256, keyBindingSigner.publicKey) { // TODO: use hashAlg
            audience(verifierId.clientId)
            claim("nonce", nonce)
            issueTime(issueTime)
        }

        return presentationSdJwt.serializeWithKeyBinding(buildKbJwt).getOrThrow()
    }
}
