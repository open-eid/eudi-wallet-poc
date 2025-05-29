package ee.cyber.wallet.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ee.cyber.wallet.data.database.KeyAttestationEntity
import ee.cyber.wallet.data.database.KeyAttestationFields
import ee.cyber.wallet.data.database.TABLE_KEY_ATTESTATIONS

@Dao
interface KeyAttestationDao {

    @Query("SELECT * FROM $TABLE_KEY_ATTESTATIONS WHERE ${KeyAttestationFields.ID} = :id")
    suspend fun getById(id: String): KeyAttestationEntity

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: KeyAttestationEntity)

    @Query("DELETE FROM $TABLE_KEY_ATTESTATIONS WHERE ${KeyAttestationFields.ID} = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM $TABLE_KEY_ATTESTATIONS")
    suspend fun deleteAll()
}
