package ee.cyber.wallet.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ee.cyber.wallet.AppConfig
import ee.cyber.wallet.crypto.CryptoProvider
import ee.cyber.wallet.data.repository.WalletCredentialsRepository
import ee.cyber.wallet.domain.credentials.CredentialIssuanceService
import ee.cyber.wallet.domain.credentials.CredentialIssuanceServiceMock
import ee.cyber.wallet.domain.credentials.RpcCredentialIssuanceService
import ee.cyber.wallet.domain.documents.CredentialToDocumentMapper
import ee.cyber.wallet.domain.provider.IssuePidUseCase
import ee.cyber.wallet.domain.provider.mdl.MdlProviderServiceMock
import ee.cyber.wallet.domain.provider.pid.PidProviderService
import ee.cyber.wallet.domain.provider.wallet.WalletProviderService
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class IssuerModule {

    @Singleton
    @Provides
    fun providesCredentialIssuanceService(
        @Dispatcher(WalletDispatchers.IO) dispatcher: CoroutineDispatcher,
        @ApplicationContext context: Context
    ): CredentialIssuanceService {
        return if (AppConfig.useMocks) {
            CredentialIssuanceServiceMock(context, dispatcher)
        } else {
            RpcCredentialIssuanceService(AppConfig.walletProviderRpcUrl, dispatcher)
        }
    }

    @Singleton
    @Provides
    fun providesMdlProviderServiceMock(
        @ApplicationContext
        context: Context,
        cryptoProviderFactory: CryptoProvider.Factory,
        credentialIssuanceService: CredentialIssuanceService
    ): MdlProviderServiceMock {
        return MdlProviderServiceMock(context, cryptoProviderFactory, credentialIssuanceService)
    }

    @Singleton
    @Provides
    fun providesIssuePidUseCase(
        cryptoProviderFactory: CryptoProvider.Factory,
        walletProviderService: WalletProviderService,
        pidProviderService: PidProviderService,
        walletCredentialsRepository: WalletCredentialsRepository,
        documentMapper: CredentialToDocumentMapper,
        @Dispatcher(WalletDispatchers.IO) dispatcher: CoroutineDispatcher
    ) = IssuePidUseCase(
        cryptoProviderFactory = cryptoProviderFactory,
        walletProviderService = walletProviderService,
        pidProviderService = pidProviderService,
        walletCredentialsRepository = walletCredentialsRepository,
        documentMapper = documentMapper,
        dispatcher = dispatcher
    )
}
