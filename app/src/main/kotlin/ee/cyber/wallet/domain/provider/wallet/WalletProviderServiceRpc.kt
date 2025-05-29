package ee.cyber.wallet.domain.provider.wallet

import com.google.protobuf.kotlin.toByteString
import com.nimbusds.jose.jwk.JWK
import ee.cyber.wallet.provider.EeWalletInstanceRegistrationServiceGrpcKt
import ee.cyber.wallet.provider.EeWalletProtectedServiceGrpcKt
import ee.cyber.wallet.provider.WalletProvider
import ee.cyber.wallet.provider.activateInstanceRequest
import ee.cyber.wallet.provider.attestKeyRequest
import ee.cyber.wallet.provider.deviceData
import ee.cyber.wallet.provider.generateKeyRequest
import ee.cyber.wallet.provider.registerWalletInstanceRequest
import ee.cyber.wallet.provider.signRequest
import ee.cyber.wallet.util.TrustAllX509TrustManager
import ee.cyber.wallet.util.sha256
import ee.cyber.wallet.util.toBase64String
import io.grpc.Channel
import io.grpc.ManagedChannel
import io.grpc.Metadata
import io.grpc.Metadata.Key
import io.grpc.okhttp.OkHttpChannelBuilder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asExecutor
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.net.URI
import javax.net.ssl.SSLContext

class WalletProviderServiceRpc(
    private val rpcUrl: String,
    private val dispatcher: CoroutineDispatcher
) : WalletProviderService {

    private val logger = LoggerFactory.getLogger("WalletProviderServiceRpc")

    private fun Channel.registrationService() = EeWalletInstanceRegistrationServiceGrpcKt.EeWalletInstanceRegistrationServiceCoroutineStub(this)
    private fun Channel.protectedService() = EeWalletProtectedServiceGrpcKt.EeWalletProtectedServiceCoroutineStub(this)

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

    override suspend fun registerWalletInstance(deviceData: DeviceData): WalletInstanceCredentials = rpc {
        logger.debug("registerWalletInstance: {}", deviceData)
        registrationService()
            .registerWalletInstance(
                registerWalletInstanceRequest {
                    this.deviceData = deviceData {
                        this.modelName = modelName
                    }
                }
            ).let { resp ->
                WalletInstanceCredentials(
                    instanceId = resp.credentials.instanceUuid,
                    instancePassword = resp.credentials.instancePassword
                )
            }
    }

    override suspend fun activateInstance(personalDataAccessToken: ByteArray, credentials: WalletInstanceCredentials): Unit = rpc {
        logger.debug("activateInstance: {}", personalDataAccessToken.toByteString())
        protectedService()
            .activateInstance(
                activateInstanceRequest {
                    this.personalDataAccessToken = personalDataAccessToken.toByteString()
                },
                authentication(credentials)
            ).takeIf { it.result == WalletProvider.WalletInstanceActivationResult.SUCCESS } ?: throw IllegalStateException("failed to activate instance")
    }

    override fun supportsKey(keyType: KeyType): Boolean = keyType == KeyType.RSA

    override suspend fun generateKey(keyType: KeyType, credentials: WalletInstanceCredentials): KeyAttestation {
        logger.debug("generateKey: {}", keyType)
        if (!supportsKey(keyType)) throw IllegalArgumentException("Key type npt supported: $keyType")
        return rpc {
            protectedService()
                .generateKey(
                    generateKeyRequest {
                        this.keyType = keyType.toProto()
                    },
                    authentication(credentials)
                ).let { resp ->
                    KeyAttestation(
                        keyId = resp.keyUUID,
                        attestation = resp.keyAttestation,
                        keyType = keyType
                    )
                }
        }
    }

    override suspend fun sign(keyAttestation: KeyAttestation, dataToBeSigned: ByteArray, credentials: WalletInstanceCredentials): ByteArray = rpc {
        logger.debug("sign: {}", keyAttestation.keyType)
        val hash = dataToBeSigned.sha256()
        protectedService()
            .sign(
                signRequest {
                    keyUUID = keyAttestation.keyId
                    hashToBeSigned = hash.toByteString()
                },
                authentication(credentials)
            ).signature.toByteArray()
    }

    override suspend fun attestKey(keyId: String, keyType: KeyType, jwk: JWK, credentials: WalletInstanceCredentials): KeyAttestation = rpc {
        logger.debug("attestKey: {}", keyType)

        protectedService()
            .attestKey(
                attestKeyRequest {
                    this.jwk = jwk.toPublicJWK().toJSONString()
                },
                authentication(credentials)
            ).let { resp ->
                KeyAttestation(
                    keyId = keyId,
                    attestation = resp.keyAttestation,
                    keyType = keyType
                )
            }
    }

    private fun authentication(credentials: WalletInstanceCredentials): Metadata = Metadata().apply {
        val auth = "${credentials.instanceId}:${credentials.instancePassword}"
        put(Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER), "Basic ${auth.toByteArray().toBase64String()}")
    }

    private fun KeyType.toProto() = when (this) {
        KeyType.RSA -> WalletProvider.KeyType.RSA
        KeyType.EC -> WalletProvider.KeyType.EC
    }
}
