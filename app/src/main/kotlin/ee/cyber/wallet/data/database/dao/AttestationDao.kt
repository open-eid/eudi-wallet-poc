package ee.cyber.wallet.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import ee.cyber.wallet.data.database.AttestationAttestationKey
import ee.cyber.wallet.data.database.AttestationEntity
import ee.cyber.wallet.data.database.AttestationFields
import ee.cyber.wallet.data.database.TABLE_ATTESTATION
import kotlinx.coroutines.flow.Flow

@Dao
interface AttestationDao {

    @Transaction
    @Query("SELECT * FROM $TABLE_ATTESTATION ORDER BY ${AttestationFields.ISSUED_AT} DESC")
    fun getAll(): Flow<List<AttestationAttestationKey>>

    @Transaction
    @Query("SELECT * FROM $TABLE_ATTESTATION WHERE ${AttestationFields.ID} = :id")
    fun getById(id: String): Flow<AttestationAttestationKey?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: AttestationEntity)

    @Query("DELETE FROM $TABLE_ATTESTATION WHERE ${AttestationFields.ID} = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM $TABLE_ATTESTATION")
    suspend fun deleteAll()
}
