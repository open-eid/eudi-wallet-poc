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
import ee.cyber.wallet.UserPreferencesProto
import ee.cyber.wallet.data.datastore.UserPreferencesDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserPreferencesDataSourceModule {

    @Provides
    @Singleton
    fun providesUserPreferencesDataStore(
        @ApplicationContext context: Context,
        @Dispatcher(WalletDispatchers.IO) dispatcher: CoroutineDispatcher,
        @ApplicationScope scope: CoroutineScope
    ): DataStore<UserPreferencesProto> =
        DataStoreFactory.create(
            serializer = UserPreferencesSerializer,
            scope = CoroutineScope(scope.coroutineContext + dispatcher)
        ) {
            context.dataStoreFile("user_preferences.pb")
        }

    @Singleton
    @Provides
    fun providesUserPreferencesDataSource(dataStore: DataStore<UserPreferencesProto>) =
        UserPreferencesDataSource(dataStore)

    private object UserPreferencesSerializer : Serializer<UserPreferencesProto> {
        override val defaultValue: UserPreferencesProto = UserPreferencesProto.getDefaultInstance()

        override suspend fun readFrom(input: InputStream): UserPreferencesProto =
            try {
                UserPreferencesProto.parseFrom(input)
            } catch (exception: InvalidProtocolBufferException) {
                throw CorruptionException("Cannot read proto.", exception)
            }

        override suspend fun writeTo(t: UserPreferencesProto, output: OutputStream) {
            t.writeTo(output)
        }
    }
}
