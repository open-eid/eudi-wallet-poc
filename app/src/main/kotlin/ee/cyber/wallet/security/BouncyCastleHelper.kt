package ee.cyber.wallet.security

import org.bouncycastle.jcajce.util.BCJcaJceHelper
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Provider
import java.security.cert.CertificateFactory

object BouncyCastleHelper {

    private val BC_HELPER = BCJcaJceHelper()
    private val provider: Provider = BouncyCastleProvider()

    fun createBCKeyStore(store: String): KeyStore {
        return BC_HELPER.createKeyStore(store)
    }

    fun createBCCertificateFactory(certType: String): CertificateFactory {
        return BC_HELPER.createCertificateFactory(certType)
    }

    fun createBCContentSignerBuilder(signatureAlgorithm: String): JcaContentSignerBuilder =
        JcaContentSignerBuilder(signatureAlgorithm).setProvider(provider)

    fun createKeyPairGenerator(algorithm: String): KeyPairGenerator {
        return BC_HELPER.createKeyPairGenerator(algorithm)
    }
}
