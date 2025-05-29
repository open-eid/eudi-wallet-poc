package ee.cyber.wallet.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ee.cyber.wallet.crypto.LocalCryptoProvider
import ee.cyber.wallet.crypto.RemoteCryptoProvider
import ee.cyber.wallet.data.database.WalletDatabase
import ee.cyber.wallet.data.database.dao.AttestationDao
import ee.cyber.wallet.data.database.dao.KeyAttestationDao
import ee.cyber.wallet.data.database.dao.LogRecordDao
import ee.cyber.wallet.data.datastore.AuthorizationStateDataSource
import ee.cyber.wallet.data.datastore.UserPreferencesDataSource
import ee.cyber.wallet.data.datastore.UserSessionDataSource
import ee.cyber.wallet.data.datastore.WalletInstanceCredentialsDataSource
import ee.cyber.wallet.data.repository.AccountRepository
import ee.cyber.wallet.data.repository.DocumentRepository
import ee.cyber.wallet.data.repository.TransactionLogRepository
import ee.cyber.wallet.data.repository.UserDataRepository
import ee.cyber.wallet.data.repository.WalletCredentialsRepository
import ee.cyber.wallet.domain.documents.CredentialToDocumentMapper
import ee.cyber.wallet.domain.provider.wallet.WalletProviderService
import ee.cyber.wallet.security.EncryptedKeyStoreManager
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoriesModule {

    @Singleton
    @Provides
    fun providesDocumentRepository(
        credentialToDocumentMapper: CredentialToDocumentMapper,
        attestationDao: AttestationDao,
        keyAttestationDao: KeyAttestationDao
    ) = DocumentRepository(
        credentialToDocumentMapper = credentialToDocumentMapper,
        attestationDao = attestationDao,
        keyAttestationDao = keyAttestationDao
    )

    @Singleton
    @Provides
    fun providesUserDataRepository(
        userPreferencesDataSource: UserPreferencesDataSource,
        userSessionDataSource: UserSessionDataSource
    ) = UserDataRepository(userPreferencesDataSource, userSessionDataSource)

    @Singleton
    @Provides
    fun providesActivityLogRepository(logRecordDao: LogRecordDao) = TransactionLogRepository(logRecordDao)

    @Singleton
    @Provides
    fun providesAccountRepository(
        @Dispatcher(WalletDispatchers.IO) dispatcher: CoroutineDispatcher,
        walletDatabase: WalletDatabase,
        attestationDao: AttestationDao,
        keyAttestationDao: KeyAttestationDao,
        userSessionDataSource: UserSessionDataSource,
        userPreferencesDataSource: UserPreferencesDataSource,
        authorizationStateDataSource: AuthorizationStateDataSource,
        encryptedKeyStoreManager: EncryptedKeyStoreManager,
        androidKeyStoreManager: EncryptedKeyStoreManager,
        remoteKeyManager: RemoteCryptoProvider,
        localKeyManager: LocalCryptoProvider
    ) = AccountRepository(
        dispatcher = dispatcher,
        walletDatabase = walletDatabase,
        userSessionDataSource = userSessionDataSource,
        userPreferencesDataSource = userPreferencesDataSource,
        authorizationStateDataSource = authorizationStateDataSource,
        encryptedKeyStoreManager = encryptedKeyStoreManager,
        androidKeyStoreManager = androidKeyStoreManager,
        attestationDao = attestationDao,
        keyAttestationDao = keyAttestationDao,
        remoteKeyManager = remoteKeyManager,
        localKeyManager = localKeyManager
    )

    @Singleton
    @Provides
    fun providesWalletCredentialsRepository(
        walletInstanceCredentialsDataSource: WalletInstanceCredentialsDataSource,
        walletProviderService: WalletProviderService
    ): WalletCredentialsRepository = WalletCredentialsRepository(walletInstanceCredentialsDataSource, walletProviderService)
}
