package ee.cyber.wallet.domain.credentials

import com.nimbusds.jose.jwk.Curve
import ee.cyber.wallet.AppConfig
import ee.cyber.wallet.crypto.CryptoProvider
import ee.cyber.wallet.crypto.popSigner
import ee.cyber.wallet.data.datastore.AuthorizationStateDataSource
import ee.cyber.wallet.domain.documents.CredentialDocument
import ee.cyber.wallet.domain.documents.CredentialToDocumentMapper
import ee.cyber.wallet.domain.provider.Attestation
import ee.cyber.wallet.domain.provider.wallet.KeyType
import eu.europa.ec.eudi.openid4vci.AuthorizationCode
import eu.europa.ec.eudi.openid4vci.AuthorizedRequest
import eu.europa.ec.eudi.openid4vci.CredentialConfigurationIdentifier
import eu.europa.ec.eudi.openid4vci.CredentialIssuanceError
import eu.europa.ec.eudi.openid4vci.CredentialIssuerId
import eu.europa.ec.eudi.openid4vci.CredentialOffer
import eu.europa.ec.eudi.openid4vci.CredentialResponseEncryptionPolicy
import eu.europa.ec.eudi.openid4vci.IssuanceRequestPayload
import eu.europa.ec.eudi.openid4vci.Issuer
import eu.europa.ec.eudi.openid4vci.KeyGenerationConfig
import eu.europa.ec.eudi.openid4vci.KtorHttpClientFactory
import eu.europa.ec.eudi.openid4vci.OpenId4VCIConfig
import eu.europa.ec.eudi.openid4vci.SubmissionOutcome
import eu.europa.ec.eudi.openid4vp.DefaultHttpClientFactory
import io.ktor.http.Url
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.UUID

class OpenId4VCIManager(
    private val credentialToDocumentMapper: CredentialToDocumentMapper,
    private val httpClientFactory: KtorHttpClientFactory = DefaultHttpClientFactory,
    private val authorizationStateDataSource: AuthorizationStateDataSource,
    private val issuerUrl: String,
    cryptoProviderFactory: CryptoProvider.Factory
) {

    private val remoteKeyManager = cryptoProviderFactory.forKeyType(KeyType.EC)

    private val logger = LoggerFactory.getLogger("OpenId4VCIManager")

    private val credentialConfigurationIds = listOf(CredentialType.MDL.value)

    private suspend fun createConfig(keyId: String): OpenId4VCIConfig {
        return OpenId4VCIConfig(
            clientId = AppConfig.clientId,
            authFlowRedirectionURI = URI.create(AppConfig.deepLinkIssuance),
            keyGenerationConfig = KeyGenerationConfig(Curve.P_256, 2048),
            credentialResponseEncryptionPolicy = CredentialResponseEncryptionPolicy.SUPPORTED,
            dPoPSigner = remoteKeyManager.popSigner(keyId)
        )
    }

    suspend fun prepareAuthorizationRequest(): String {
        authorizationStateDataSource.clearAll()

        val key = remoteKeyManager.generateKey(KeyType.EC)
        val credentialIssuerId = CredentialIssuerId(issuerUrl).getOrThrow()
        val (issuerMetadata, authorizationServersMetadata) = httpClientFactory().use { client ->
            Issuer.metaData(client, credentialIssuerId)
        }

        val identifiers = credentialConfigurationIds.map { CredentialConfigurationIdentifier(it) }

        val credentialOffer = CredentialOffer(
            credentialIssuerIdentifier = credentialIssuerId,
            credentialIssuerMetadata = issuerMetadata,
            authorizationServerMetadata = authorizationServersMetadata[0],
            credentialConfigurationIdentifiers = identifiers
        )

        val issuer = Issuer.make(
            config = createConfig(key.keyId),
            ktorHttpClientFactory = httpClientFactory,
            credentialOffer = credentialOffer
        ).getOrThrow()

        return issuer.prepareAuthorizationRequest().getOrThrow()
            .also {
                authorizationStateDataSource.add(it.state, IssuanceAuthorizationState(it, key.keyId))
            }.authorizationCodeURL.value.toString()
    }

    suspend fun getCredential(url: Url): CredentialDocument {
        val state = url.parameters["state"] ?: throw IllegalStateException("State is missing")
        val code = url.parameters["code"] ?: throw IllegalStateException("Code is missing")

        val stateOrNull = authorizationStateDataSource.get(state).getOrNull()
        val prepareAuthorizationCodeRequest = stateOrNull ?: throw IllegalStateException("Authorization request has not been prepared yet")
        val keyAttestation = remoteKeyManager.getKeyAttestation(prepareAuthorizationCodeRequest.keyId)

        val credentialIssuerId = CredentialIssuerId(issuerUrl).getOrThrow()
        val (issuerMetadata, authorizationServersMetadata) = httpClientFactory().use { client ->
            Issuer.metaData(client, credentialIssuerId)
        }

        val identifiers = credentialConfigurationIds.map { CredentialConfigurationIdentifier(it) }

        val credentialOffer = CredentialOffer(
            credentialIssuerIdentifier = credentialIssuerId,
            credentialIssuerMetadata = issuerMetadata,
            authorizationServerMetadata = authorizationServersMetadata[0],
            credentialConfigurationIdentifiers = identifiers
        )

        val issuer = Issuer.make(
            config = createConfig(keyAttestation.keyId),
            ktorHttpClientFactory = httpClientFactory,
            credentialOffer = credentialOffer
        ).getOrThrow()

        return with(issuer) {
            val authorizedRequest =
                prepareAuthorizationCodeRequest.request.authorizeWithAuthorizationCode(authorizationCode = AuthorizationCode(code), serverState = state).getOrThrow()
                    .also { logger.info("authorizedRequest: $it") }

            credentialOffer.credentialConfigurationIdentifiers.first().let { credentialIdentifier ->
                val outcome = when (authorizedRequest) {
                    is AuthorizedRequest.NoProofRequired -> submitProvidingNoProofs(issuer, authorizedRequest, credentialIdentifier, keyAttestation.keyId)
                    is AuthorizedRequest.ProofRequired -> submitProvidingProofs(issuer, authorizedRequest, credentialIdentifier, keyAttestation.keyId)
                }
                val attestation = Attestation(
                    id = UUID.randomUUID().toString(),
                    credential = outcome,
                    type = CredentialType.MDL,
                    keyAttestation = keyAttestation
                )
                credentialToDocumentMapper.convert(attestation)!!
            }
        }
    }

    private suspend fun submitProvidingNoProofs(
        issuer: Issuer,
        authorized: AuthorizedRequest.NoProofRequired,
        credentialConfigurationId: CredentialConfigurationIdentifier,
        keyId: String
    ): String {
        with(issuer) {
            val requestPayload = IssuanceRequestPayload.ConfigurationBased(credentialConfigurationId, null)
            val (newAuthorized, outcome) = authorized.request(requestPayload).getOrThrow()

            return when (outcome) {
                is SubmissionOutcome.Success -> outcome.credentials[0].credential.toString() // TODO: handle multiple credentials
                is SubmissionOutcome.Failed -> throw if (outcome.error is CredentialIssuanceError.InvalidProof) {
                    IllegalStateException("Although providing a proof with c_nonce the proof is still invalid")
                } else {
                    outcome.error
                }
                is SubmissionOutcome.Deferred -> TODO()
            }
        }
    }

    private suspend fun submitProvidingProofs(
        issuer: Issuer,
        authorized: AuthorizedRequest,
        credentialConfigurationId: CredentialConfigurationIdentifier,
        keyId: String
    ): String {
        with(issuer) {
            val requestPayload = IssuanceRequestPayload.ConfigurationBased(credentialConfigurationId, null)
            val proofSigner = remoteKeyManager.popSigner(keyId)
            val (newAuthorized, outcome) = authorized.request(requestPayload, listOf(proofSigner)).getOrThrow()
            return when (outcome) {
                is SubmissionOutcome.Success -> outcome.credentials[0].credential.toString() // TODO: handle multiple credentials
                is SubmissionOutcome.Failed -> throw if (outcome.error is CredentialIssuanceError.InvalidProof) {
                    IllegalStateException("Although providing a proof with c_nonce the proof is still invalid")
                } else {
                    outcome.error
                }
                is SubmissionOutcome.Deferred -> TODO()
            }
        }
    }

//    private suspend fun handleDeferred(
//        issuer: Issuer,
//        authorized: AuthorizedRequest,
//        deferred: IssuedCredential.Deferred
//    ): String {
//        with(issuer) {
//            val authorizedRequest = authorized.queryForDeferredCredential(deferred).getOrThrow()
//            return when (val deferredCredentialQueryOutcome = authorizedRequest.second) {
//                is DeferredCredentialQueryOutcome.Issued -> deferredCredentialQueryOutcome.credential.credential
//                is DeferredCredentialQueryOutcome.IssuancePending -> throw RuntimeException("Credential not ready yet. Try after ${deferredCredentialQueryOutcome.interval}")
//                is DeferredCredentialQueryOutcome.Errored -> throw RuntimeException(deferredCredentialQueryOutcome.error)
//            }
//        }
//    }
}
