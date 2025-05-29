package ee.cyber.wallet.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ee.cyber.wallet.data.database.ActivityLogFields
import ee.cyber.wallet.data.database.LogEntryEntity
import ee.cyber.wallet.data.database.TABLE_ACTIVITY_LOGS
import kotlinx.coroutines.flow.Flow

@Dao
interface LogRecordDao {

    @Query("SELECT * FROM $TABLE_ACTIVITY_LOGS ORDER BY ${ActivityLogFields.DATE} DESC")
    fun getAll(): Flow<List<LogEntryEntity>>

    @Query("SELECT * FROM $TABLE_ACTIVITY_LOGS WHERE ${ActivityLogFields.ID} = :id")
    fun getById(id: Long): Flow<LogEntryEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: LogEntryEntity)
}
