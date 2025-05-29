package ee.cyber.wallet.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties.BLOCK_MODE_CBC
import android.security.keystore.KeyProperties.ENCRYPTION_PADDING_PKCS7
import android.security.keystore.KeyProperties.KEY_ALGORITHM_AES
import android.security.keystore.KeyProperties.PURPOSE_DECRYPT
import android.security.keystore.KeyProperties.PURPOSE_ENCRYPT
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class AndroidEncryptionManager(
    private val dispatcher: CoroutineDispatcher
) {

    private val log = LoggerFactory.getLogger("AndroidEncryptionManager")

    suspend fun encrypt(keyAlias: String, rawBytes: ByteArray, output: OutputStream) = withContext(dispatcher) {
        DataOutputStream(output).use {
            Cipher.getInstance(TRANSFORMATION).run {
                init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey(keyAlias))
                val encryptedBytes = doFinal(rawBytes)

                it.writeInt(iv.size)
                it.write(iv)
                it.writeInt(encryptedBytes.size)
                it.write(encryptedBytes)
            }
        }
    }

    suspend fun decrypt(keyAlias: String, inputStream: InputStream): ByteArray = withContext(dispatcher) {
        DataInputStream(inputStream).use {
            val iv = ByteArray(it.readInt())
            it.read(iv)
            val encryptedData = ByteArray(it.readInt())
            it.read(encryptedData)
            try {
                Cipher.getInstance(TRANSFORMATION).run {
                    init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(keyAlias), IvParameterSpec(iv))
                    doFinal(encryptedData)
                }
            } catch (e: Exception) {
                log.error("failed to decrypt data", e)
                byteArrayOf()
            }
        }
    }

    private fun keyStore() = KeyStore.getInstance(ANDROID_KEY_STORE).apply {
        load(null)
    }

    private fun getOrCreateSecretKey(keyAlias: String): SecretKey =
        getSecretKey(keyAlias) ?: KeyGenerator.getInstance(ALGORITHM, ANDROID_KEY_STORE).apply {
            init(
                KeyGenParameterSpec
                    .Builder(keyAlias, PURPOSE_ENCRYPT or PURPOSE_DECRYPT)
                    .setBlockModes(BLOCK_MODE)
                    .setKeySize(KEY_SIZE)
                    .setEncryptionPaddings(PADDING)
                    .setUserAuthenticationRequired(false)
                    .setRandomizedEncryptionRequired(true)
                    .apply {
                        // commented out due to unknown issue on Pixel 8, where decrypted value is wrong
//                        if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)) {
//                            setIsStrongBoxBacked(true)
//                        }
                    }
                    .build()
            )
        }.generateKey()

    private fun getSecretKey(keyAlias: String) = keyStore().getKey(keyAlias, charArrayOf()) as? SecretKey

    companion object {
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"

        private const val ALGORITHM = KEY_ALGORITHM_AES
        private const val BLOCK_MODE = BLOCK_MODE_CBC
        private const val PADDING = ENCRYPTION_PADDING_PKCS7
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING" // NON-NLS
        private const val KEY_SIZE = 256
    }
}
