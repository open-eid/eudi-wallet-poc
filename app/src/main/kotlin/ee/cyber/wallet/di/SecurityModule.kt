package ee.cyber.wallet.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ee.cyber.wallet.crypto.CryptoProvider
import ee.cyber.wallet.crypto.LocalCryptoProvider
import ee.cyber.wallet.crypto.RemoteCryptoProvider
import ee.cyber.wallet.data.database.dao.KeyAttestationDao
import ee.cyber.wallet.data.datastore.WalletInstanceCredentialsDataSource
import ee.cyber.wallet.domain.provider.wallet.WalletProviderService
import ee.cyber.wallet.security.AndroidEncryptionManager
import ee.cyber.wallet.security.EncryptedKeyStoreManager
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    @Singleton
    @Provides
    fun providesEncryptedKeyStore(
        @ApplicationContext context: Context
    ) = EncryptedKeyStoreManager(context)

    @Singleton
    @Provides
    fun providesAndroidEncryptionManager(
        @Dispatcher(WalletDispatchers.IO) dispatcher: CoroutineDispatcher
    ) = AndroidEncryptionManager(dispatcher)

    @Singleton
    @Provides
    fun providesRemoteCryptoProvider(
        @Dispatcher(WalletDispatchers.IO) dispatcher: CoroutineDispatcher,
        keyAttestationDao: KeyAttestationDao,
        walletProviderService: WalletProviderService,
        walletInstanceCredentialsDataSource: WalletInstanceCredentialsDataSource
    ): RemoteCryptoProvider = RemoteCryptoProvider(
        dispatcher = dispatcher,
        keyAttestationDao = keyAttestationDao,
        walletProviderService = walletProviderService,
        walletInstanceCredentialsDataSource = walletInstanceCredentialsDataSource
    )

    @Singleton
    @Provides
    fun providesLocalCryptoProvider(
        encryptedKeyStoreManager: EncryptedKeyStoreManager,
        keyAttestationDao: KeyAttestationDao,
        walletProviderService: WalletProviderService,
        walletInstanceCredentialsDataSource: WalletInstanceCredentialsDataSource
    ): LocalCryptoProvider = LocalCryptoProvider(
        keyAttestationDao = keyAttestationDao,
        keyStoreManager = encryptedKeyStoreManager,
        walletProviderService = walletProviderService,
        walletInstanceCredentialsDataSource = walletInstanceCredentialsDataSource
    )

    @Singleton
    @Provides
    fun providesCryptoProviderFactory(
        remoteCryptoProvider: RemoteCryptoProvider,
        localCryptoProvider: LocalCryptoProvider
    ): CryptoProvider.Factory = CryptoProvider.Factory(listOf(remoteCryptoProvider, localCryptoProvider))
}
