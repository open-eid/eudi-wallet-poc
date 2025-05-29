package ee.cyber.wallet.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ee.cyber.wallet.AppConfig
import ee.cyber.wallet.domain.credentials.CredentialIssuanceService
import ee.cyber.wallet.domain.provider.pid.PidProviderService
import ee.cyber.wallet.domain.provider.pid.PidProviderServiceMock
import ee.cyber.wallet.domain.provider.pid.PidProviderServiceRpc
import ee.cyber.wallet.domain.provider.wallet.WalletProviderService
import ee.cyber.wallet.domain.provider.wallet.WalletProviderServiceMock
import ee.cyber.wallet.domain.provider.wallet.WalletProviderServiceRpc
import ee.cyber.wallet.security.EncryptedKeyStoreManager
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class WalletProviderModule {

    @Singleton
    @Provides
    fun providesWalletProviderService(
        keyStoreManager: EncryptedKeyStoreManager,
        @Dispatcher(WalletDispatchers.IO) dispatcher: CoroutineDispatcher
    ): WalletProviderService {
        return if (AppConfig.useMocks) {
            WalletProviderServiceMock(keyStoreManager, dispatcher)
        } else {
            WalletProviderServiceRpc(AppConfig.walletProviderRpcUrl, dispatcher)
        }
    }

    @Singleton
    @Provides
    fun providesPidProviderService(
        @ApplicationContext
        context: Context,
        credentialIssuanceService: CredentialIssuanceService,
        @Dispatcher(WalletDispatchers.IO) dispatcher: CoroutineDispatcher
    ): PidProviderService {
        return if (AppConfig.useMocks) {
            PidProviderServiceMock(context, credentialIssuanceService)
        } else {
            PidProviderServiceRpc(AppConfig.pidProviderUrl, dispatcher)
        }
    }
}
