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
import ee.cyber.wallet.UserSessionProto
import ee.cyber.wallet.data.datastore.UserSessionDataSource
import ee.cyber.wallet.security.AndroidEncryptionManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserSessionDataSourceModule {

    @Singleton
    @Provides
    fun providesUserSessionDataSource(dataStore: DataStore<UserSessionProto>) =
        UserSessionDataSource(dataStore)

    @Provides
    @Singleton
    fun providesUserSessionDataStore(
        @ApplicationContext context: Context,
        @Dispatcher(WalletDispatchers.IO) dispatcher: CoroutineDispatcher,
        @ApplicationScope scope: CoroutineScope,
        androidEncryptionManager: AndroidEncryptionManager
    ): DataStore<UserSessionProto> =
        DataStoreFactory.create(
            serializer = UserSessionSerializer(androidEncryptionManager),
            scope = CoroutineScope(scope.coroutineContext + dispatcher)
        ) {
            context.dataStoreFile("user_session.pb")
        }

    private class UserSessionSerializer(
        private val encryptionManager: AndroidEncryptionManager
    ) : Serializer<UserSessionProto> {
        override val defaultValue: UserSessionProto = UserSessionProto.getDefaultInstance()

        override suspend fun readFrom(input: InputStream): UserSessionProto =

            try {
                UserSessionProto.parseFrom(encryptionManager.decrypt(KEY_ALIAS, input))
            } catch (exception: InvalidProtocolBufferException) {
                throw CorruptionException("Cannot read proto.", exception)
            }

        override suspend fun writeTo(t: UserSessionProto, output: OutputStream) {
            encryptionManager.encrypt(KEY_ALIAS, t.toByteArray(), output)
        }

        companion object {
            private const val KEY_ALIAS = "user-session-key"
        }
    }
}
