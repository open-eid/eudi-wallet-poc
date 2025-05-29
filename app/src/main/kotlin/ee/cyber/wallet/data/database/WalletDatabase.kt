package ee.cyber.wallet.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ee.cyber.wallet.data.database.dao.AttestationDao
import ee.cyber.wallet.data.database.dao.KeyAttestationDao
import ee.cyber.wallet.data.database.dao.LogRecordDao

@Database(
    entities = [
        LogEntryEntity::class,
        KeyAttestationEntity::class,
        AttestationEntity::class
    ],
    version = 1
)
@TypeConverters(
    InstantConverter::class,
    SafeAppErrorConverter::class,
    JsonConverter::class
)
abstract class WalletDatabase : RoomDatabase() {
    abstract fun logRecordDao(): LogRecordDao
    abstract fun keyAttestationDao(): KeyAttestationDao
    abstract fun attestationDao(): AttestationDao
}
