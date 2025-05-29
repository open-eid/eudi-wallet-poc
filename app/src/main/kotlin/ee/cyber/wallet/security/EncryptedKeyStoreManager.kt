package ee.cyber.wallet.security

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import ee.cyber.wallet.security.BouncyCastleHelper.createBCCertificateFactory
import ee.cyber.wallet.security.BouncyCastleHelper.createBCContentSignerBuilder
import ee.cyber.wallet.security.BouncyCastleHelper.createBCKeyStore
import ee.cyber.wallet.security.BouncyCastleHelper.createKeyPairGenerator
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.slf4j.LoggerFactory
import java.io.File
import java.security.KeyPair
import java.security.KeyStore
import java.security.PublicKey
import java.security.cert.Certificate
import java.security.spec.AlgorithmParameterSpec
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

private const val CN = "CN=EE Wallet"

class EncryptedKeyStoreManager(private val context: Context) : KeyStoreManager {

    private val logger = LoggerFactory.getLogger("EncryptedKeyStoreManager")

    companion object {
        private const val KEY_STORE = "BKS"
        private const val KEY_STORE_FILENAME = "keystore.jks"
    }

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
    }

    private val file = File(context.filesDir, KEY_STORE_FILENAME)

    private val encryptedFile: EncryptedFile by lazy {
        EncryptedFile.Builder(
            context,
            file,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
    }

    override fun keyStore() = loadKeyStore(encryptedFile)

    override fun containsKey(alias: String): Boolean = keyStore().containsAlias(alias)

    override fun deleteKey(alias: String) {
        runCatching { keyStore().deleteEntry(alias) }
            .onFailure { logger.error("failed to delete key $alias", it) }
    }

    override fun createKeyPair(alias: String, keyAlgorithm: String, certSignAlgorithm: String, paramSpec: AlgorithmParameterSpec) {
        val keyPair = createKeyPairGenerator(keyAlgorithm).apply { initialize(paramSpec) }.generateKeyPair()
        keyStore().apply {
            setKeyEntry(alias, keyPair.private, "".toCharArray(), arrayOf(keyPair.buildSelfSignedCertificate(certSignAlgorithm)))
            save(encryptedFile)
        }
    }

    override fun clearAll() {
        keyStore().aliases().toList().forEach {
            deleteKey(it)
        }
        file.delete()
    }

    private fun loadKeyStore(ksFile: EncryptedFile): KeyStore {
        val keyStore = createBCKeyStore(KEY_STORE)
        val input = runCatching { ksFile.openFileInput() }
            .getOrNull()
        input?.use { keyStore.load(it, null) } ?: keyStore.load(null, null)
        return keyStore
    }

    private fun KeyStore.save(ksFile: EncryptedFile) {
        if (file.exists()) file.delete()
        ksFile.openFileOutput().use { output -> store(output, null) }
    }

    private fun KeyPair.buildSelfSignedCertificate(signAlgorithm: String): Certificate {
        val signer = createBCContentSignerBuilder(signAlgorithm).build(private)
        val certificateBytes = createCertificateBuilder(public).build(signer).encoded
        val certificateFactory = createBCCertificateFactory("X.509")
        return certificateBytes.inputStream().use { certificateFactory.generateCertificate(it) }
    }

    private fun createCertificateBuilder(publicKey: PublicKey): X509v3CertificateBuilder {
        val now = Instant.now()
        return X509v3CertificateBuilder(
            /* issuer = */
            X500Name(CN),
            /* serial = */
            System.currentTimeMillis().toBigInteger(),
            /* notBefore = */
            Date.from(now),
            /* notAfter = */
            Date.from(now.plus(60 * 30, ChronoUnit.DAYS)),
            /* subject = */
            X500Name(CN),
            /* publicKeyInfo = */
            SubjectPublicKeyInfo.getInstance(publicKey.encoded)
        )
    }
}
