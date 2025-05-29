package ee.cyber.wallet.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ee.cyber.wallet.data.database.WalletDatabase
import ee.cyber.wallet.data.database.dao.AttestationDao
import ee.cyber.wallet.data.database.dao.KeyAttestationDao
import ee.cyber.wallet.data.database.dao.LogRecordDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    @Singleton
    fun providesWalletDatabase(
        @ApplicationContext context: Context
    ): WalletDatabase = Room.databaseBuilder(
        context = context,
        klass = WalletDatabase::class.java,
        name = "wallet-db"
    ).build()

    @Provides
    @Singleton
    fun providesProductsDao(database: WalletDatabase): LogRecordDao = database.logRecordDao()

    @Provides
    @Singleton
    fun providesKeyAttestationDao(database: WalletDatabase): KeyAttestationDao = database.keyAttestationDao()

    @Provides
    @Singleton
    fun providesAttestationDao(database: WalletDatabase): AttestationDao = database.attestationDao()
}
