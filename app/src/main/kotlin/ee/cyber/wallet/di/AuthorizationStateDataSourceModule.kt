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
import ee.cyber.wallet.AuthorizationRequestStateProto
import ee.cyber.wallet.data.datastore.AuthorizationStateDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthorizationStateDataSourceModule {

    @Provides
    @Singleton
    fun providesAuthorizationStateDataStore(
        @ApplicationContext context: Context,
        @Dispatcher(WalletDispatchers.IO) dispatcher: CoroutineDispatcher,
        @ApplicationScope scope: CoroutineScope
    ): DataStore<AuthorizationRequestStateProto> =
        DataStoreFactory.create(
            serializer = AuthorizationStateSerializer(),
            scope = CoroutineScope(scope.coroutineContext + dispatcher)
        ) {
            context.dataStoreFile("issuer_authorization.pb")
        }

    @Singleton
    @Provides
    fun providesAuthorizationStateDataSource(dataStore: DataStore<AuthorizationRequestStateProto>) = AuthorizationStateDataSource(dataStore)

    private class AuthorizationStateSerializer : Serializer<AuthorizationRequestStateProto> {
        override val defaultValue: AuthorizationRequestStateProto = AuthorizationRequestStateProto.getDefaultInstance()

        override suspend fun readFrom(input: InputStream): AuthorizationRequestStateProto =
            try {
                AuthorizationRequestStateProto.parseFrom(input)
            } catch (exception: InvalidProtocolBufferException) {
                throw CorruptionException("Cannot read proto.", exception)
            }

        override suspend fun writeTo(t: AuthorizationRequestStateProto, output: OutputStream) = t.writeTo(output)
    }
}
