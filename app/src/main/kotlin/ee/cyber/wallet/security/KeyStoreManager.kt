package ee.cyber.wallet.security

import java.security.KeyStore
import java.security.spec.AlgorithmParameterSpec

interface KeyStoreManager {

    fun keyStore(): KeyStore

    fun containsKey(alias: String): Boolean

    fun deleteKey(alias: String)

    fun createKeyPair(alias: String, keyAlgorithm: String, certSignAlgorithm: String, paramSpec: AlgorithmParameterSpec)

    fun clearAll()
}
