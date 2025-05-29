package ee.cyber.wallet.domain.credentials

import ee.cyber.pid.provider.PidIssuance
import ee.cyber.pid.provider.PidIssuanceServiceGrpcKt
import ee.cyber.pid.provider.eePidIssuanceRequest
import ee.cyber.wallet.domain.provider.Attestation
import ee.cyber.wallet.domain.provider.wallet.KeyAttestation
import ee.cyber.wallet.util.TrustAllX509TrustManager
import io.grpc.okhttp.OkHttpChannelBuilder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asExecutor
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.net.URI
import java.util.UUID
import javax.net.ssl.SSLContext

class RpcCredentialIssuanceService(
    private val rpcUrl: String,
    private val dispatcher: CoroutineDispatcher
) : CredentialIssuanceService {

    private val logger = LoggerFactory.getLogger("RpcPidIssuanceService")

    override fun supports(credentialType: CredentialType): Boolean = when (credentialType) {
        CredentialType.PID_SD_JWT, CredentialType.PID_MDOC -> true
        else -> false
    }

    override suspend fun issueCredential(credential: Credential, keyAttestation: KeyAttestation): Attestation {
        logger.debug("issueCredential: {}", credential)
        return when (credential) {
            is Credential.JwtPidCredential -> rpc { issueSdJwtPid(credential, keyAttestation) }
            is Credential.MdocPidCredential -> rpc { issueMDocPid(credential, keyAttestation) }
            else -> TODO("${credential.type} not supported yet")
        }.let {
            Attestation(
                id = UUID.randomUUID().toString(),
                credential = it,
                type = credential.type,
                keyAttestation = keyAttestation
            )
        }
    }

    private suspend fun <T> rpc(block: suspend IssuerRPC.() -> T): T = IssuerRPC().use { block(it) }

    private inner class IssuerRPC : Closeable {
        private val channel by lazy {
            val uri = URI.create(rpcUrl)
            val builder = OkHttpChannelBuilder.forAddress(uri.host, uri.port)
            logger.info("connecting to $uri")
            if (uri.scheme == "https") {
                builder.useTransportSecurity()
                logger.warn("Using unsafe trust manager for development")
                val sslContext = SSLContext.getInstance("TLS").apply {
                    init(null, arrayOf(TrustAllX509TrustManager()), null)
                }
                builder.sslSocketFactory(sslContext.socketFactory)
            } else {
                builder.usePlaintext()
            }
            builder.executor(dispatcher.asExecutor()).build()
        }

        private val issuer by lazy { PidIssuanceServiceGrpcKt.PidIssuanceServiceCoroutineStub(channel) }

        suspend fun issueSdJwtPid(pid: Credential.JwtPidCredential, keyAttestation: KeyAttestation): String {
            val request = eePidIssuanceRequest {
                this.docType = PidIssuance.DocType.SD_JWT
                this.sub = pid.personalAdministrativeNumber
                this.givenName = pid.givenName
                this.familyName = pid.familyName
                this.birthdate = pid.birthDate.toString()
                this.holderPubKeyJson = keyAttestation.jwk.toPublicJWK().toJSONString()
            }
            return issuer.issuePid(request).credential
        }

        suspend fun issueMDocPid(pid: Credential.MdocPidCredential, keyAttestation: KeyAttestation): String {
            val request = eePidIssuanceRequest {
                this.docType = PidIssuance.DocType.MSO_MDOC
                this.sub = pid.personalAdministrativeNumber
                this.givenName = pid.givenName
                this.familyName = pid.familyName
                this.birthdate = pid.birthDate.toString()
                this.holderPubKeyJson = keyAttestation.jwk.toPublicJWK().toJSONString()
            }
            return issuer.issuePid(request).credential
        }

        override fun close() {
            channel.shutdownNow()
        }
    }
}
