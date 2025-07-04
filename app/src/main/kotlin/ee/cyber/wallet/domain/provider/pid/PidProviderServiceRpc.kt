package ee.cyber.wallet.domain.provider.pid

import com.google.protobuf.kotlin.toByteString
import ee.cyber.pid.provider.EePidIssuanceServiceGrpcKt
import ee.cyber.pid.provider.PidProvider.PidAttestationType
import ee.cyber.pid.provider.bindInstanceRequest
import ee.cyber.pid.provider.issuePidRequest
import ee.cyber.pid.provider.pIDAttestationRequest
import ee.cyber.wallet.domain.credentials.CredentialType
import ee.cyber.wallet.domain.provider.Attestation
import ee.cyber.wallet.util.TrustAllX509TrustManager
import io.grpc.Channel
import io.grpc.ManagedChannel
import io.grpc.okhttp.OkHttpChannelBuilder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asExecutor
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.net.URI
import java.util.UUID
import javax.net.ssl.SSLContext

class PidProviderServiceRpc(
    private val rpcUrl: String,
    private val dispatcher: CoroutineDispatcher
) : PidProviderService {

    private val logger = LoggerFactory.getLogger("PidProviderServiceRpc")

    private fun Channel.eePidIssuanceService() = EePidIssuanceServiceGrpcKt.EePidIssuanceServiceCoroutineStub(this)
    private suspend fun <T> rpc(block: suspend Channel.() -> T): T = RPCChannel().use { block(it.channel) }

    private inner class RPCChannel : Closeable {
        val channel: ManagedChannel by lazy {
            val uri = URI.create(rpcUrl)
            OkHttpChannelBuilder.forAddress(uri.host, uri.port).apply {
                logger.info("connecting to $uri")
                if (uri.scheme == "https") {
                    useTransportSecurity()

                    logger.warn("Using unsafe trust manager for development")
                    val sslContext = SSLContext.getInstance("TLS").apply {
                        init(null, arrayOf(TrustAllX509TrustManager()), null)
                    }
                    sslSocketFactory(sslContext.socketFactory)
                } else {
                    usePlaintext()
                }
                executor(dispatcher.asExecutor())
            }.build()
        }

        override fun close() {
            channel.shutdownNow()
        }
    }

    override suspend fun bindInstance(bindingToken: String): InstanceBinding = rpc {
        logger.debug("bindInstance: $bindingToken")
        eePidIssuanceService()
            .bindInstance(
                bindInstanceRequest {
                    this.bindingToken = bindingToken
                }
            ).let { resp ->
                InstanceBinding(
                    sessionToken = resp.sessionToken.toByteArray(),
                    cNonce = resp.cNonce.toByteArray(),
                    personalDataAccessToken = resp.personalDataAccessToken.toByteArray()
                )
            }
    }

    override suspend fun issuePid(sessionToken: ByteArray, requests: Map<AttestationType, AttestationRequest>): List<Attestation> = rpc {
        logger.debug("issuePid: {}, {}", sessionToken.toByteString(), requests)
        eePidIssuanceService()
            .issuePid(
                issuePidRequest {
                    this.sessionToken = sessionToken.toByteString()
                    requests.forEach { entry ->
                        this.pidAttestationRequests.add(
                            pIDAttestationRequest {
                                this.type = entry.key.toProto()
                                this.keyAttestation = entry.value.keyAttestation.jwt.serialize()
                                this.proofOfPossession = entry.value.proofOfPossession
                            }
                        )
                    }
                }
            ).let { resp ->
                resp.pidAttestationsList.map { pid ->
                    Attestation(
                        id = UUID.randomUUID().toString(),
                        type = when (pid.type) {
                            PidAttestationType.SD_JWT_VC -> CredentialType.PID_SD_JWT
                            PidAttestationType.MDOC -> CredentialType.PID_MDOC
                            else -> throw IllegalStateException()
                        },
                        credential = pid.attestation,
                        keyAttestation = requests.filterKeys { it == pid.type.toModel() }.values.first().keyAttestation
                    )
                }
            }
    }

    private fun PidAttestationType.toModel(): AttestationType = when (this) {
        PidAttestationType.SD_JWT_VC -> AttestationType.SD_JWT_VC
        PidAttestationType.MDOC -> AttestationType.MDOC
        PidAttestationType.UNRECOGNIZED -> throw IllegalStateException("Unrecognized value: ")
    }

    private fun AttestationType.toProto(): PidAttestationType = when (this) {
        AttestationType.SD_JWT_VC -> PidAttestationType.SD_JWT_VC
        AttestationType.MDOC -> PidAttestationType.MDOC
    }
}
