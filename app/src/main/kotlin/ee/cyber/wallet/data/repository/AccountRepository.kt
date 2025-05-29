package ee.cyber.wallet.data.repository

import ee.cyber.wallet.crypto.LocalCryptoProvider
import ee.cyber.wallet.crypto.RemoteCryptoProvider
import ee.cyber.wallet.data.database.WalletDatabase
import ee.cyber.wallet.data.database.dao.AttestationDao
import ee.cyber.wallet.data.database.dao.KeyAttestationDao
import ee.cyber.wallet.data.datastore.AuthorizationStateDataSource
import ee.cyber.wallet.data.datastore.UserPreferencesDataSource
import ee.cyber.wallet.data.datastore.UserSessionDataSource
import ee.cyber.wallet.security.EncryptedKeyStoreManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class AccountRepository(
    private val dispatcher: CoroutineDispatcher,
    private val walletDatabase: WalletDatabase,
    private val remoteKeyManager: RemoteCryptoProvider,
    private val localKeyManager: LocalCryptoProvider,
    private val encryptedKeyStoreManager: EncryptedKeyStoreManager,
    private val androidKeyStoreManager: EncryptedKeyStoreManager,
    private val attestationDao: AttestationDao,
    private val keyAttestationDao: KeyAttestationDao,
    private val authorizationStateDataSource: AuthorizationStateDataSource,
    private val userSessionDataSource: UserSessionDataSource,
    private val userPreferencesDataSource: UserPreferencesDataSource
) {
    suspend fun deleteAllData() {
        withContext(dispatcher) {
            runCatching { walletDatabase.clearAllTables() }
            runCatching { encryptedKeyStoreManager.clearAll() }
            runCatching { androidKeyStoreManager.clearAll() }

            remoteKeyManager.clearAll()
            localKeyManager.clearAll()
            attestationDao.deleteAll()
            keyAttestationDao.deleteAll()
            authorizationStateDataSource.clearAll()
            userSessionDataSource.clearAll()
            userPreferencesDataSource.clearAll()
        }
    }
}
