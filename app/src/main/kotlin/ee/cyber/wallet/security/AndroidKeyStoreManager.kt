package ee.cyber.wallet.security

import org.slf4j.LoggerFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.spec.AlgorithmParameterSpec

object AndroidKeyStoreManager : KeyStoreManager {

    private const val KEY_STORE = "AndroidKeyStore"

    private val logger = LoggerFactory.getLogger("AndroidKeyStoreManager")

    override fun keyStore(): KeyStore =
        KeyStore.getInstance(KEY_STORE).apply { load(null) }

    override fun containsKey(alias: String): Boolean = keyStore().containsAlias(alias)

    override fun deleteKey(alias: String) {
        runCatching {
            keyStore().deleteEntry(alias)
        }.onFailure { logger.error("failed to delete key $alias", it) }
    }

    override fun createKeyPair(alias: String, keyAlgorithm: String, certSignAlgorithm: String, paramSpec: AlgorithmParameterSpec) {
        KeyPairGenerator.getInstance(keyAlgorithm, KEY_STORE).apply {
            initialize(paramSpec)
        }.generateKeyPair()
    }

    override fun clearAll() {
        keyStore().aliases().toList().forEach {
            deleteKey(it)
        }
    }
}
