package ee.cyber.wallet.di

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.dataStoreFile
import com.google.protobuf.InvalidProtocolBufferException
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ee.cyber.wallet.WalletInstanceCredentialsProto
import ee.cyber.wallet.data.datastore.WalletInstanceCredentialsDataSource
import ee.cyber.wallet.security.AndroidEncryptionManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WalletCredentialsDataSourceModule {

    @Singleton
    @Provides
    fun providesWalletInstanceCredentialsDataSource(dataStore: DataStore<WalletInstanceCredentialsProto>) =
        WalletInstanceCredentialsDataSource(dataStore)

    @Provides
    @Singleton
    fun providesWalletInstanceCredentialsDataStore(
        @ApplicationContext context: Context,
        @Dispatcher(WalletDispatchers.IO) dispatcher: CoroutineDispatcher,
        @ApplicationScope scope: CoroutineScope,
        androidEncryptionManager: AndroidEncryptionManager
    ): DataStore<WalletInstanceCredentialsProto> =
        DataStoreFactory.create(
            serializer = WalletInstanceCredentialsSerializer(androidEncryptionManager),
            scope = CoroutineScope(scope.coroutineContext + dispatcher)
        ) {
            context.dataStoreFile("wallet_credentials.pb")
        }

    private class WalletInstanceCredentialsSerializer(
        private val encryptionManager: AndroidEncryptionManager
    ) : Serializer<WalletInstanceCredentialsProto> {
        override val defaultValue: WalletInstanceCredentialsProto = WalletInstanceCredentialsProto.getDefaultInstance()

        override suspend fun readFrom(input: InputStream): WalletInstanceCredentialsProto =
            try {
                WalletInstanceCredentialsProto.parseFrom(encryptionManager.decrypt(KEY_ALIAS, input))
            } catch (exception: InvalidProtocolBufferException) {
                throw CorruptionException("Cannot read proto.", exception)
            }

        override suspend fun writeTo(t: WalletInstanceCredentialsProto, output: OutputStream) {
            encryptionManager.encrypt(KEY_ALIAS, t.toByteArray(), output)
        }

        companion object {
            private const val KEY_ALIAS = "wallet-credentials-key"
        }
    }
}
