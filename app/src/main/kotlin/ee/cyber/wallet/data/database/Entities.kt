package ee.cyber.wallet.data.database

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import ee.cyber.wallet.domain.AppError
import ee.cyber.wallet.domain.credentials.CredentialType
import ee.cyber.wallet.domain.credentials.DocType
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonObject

object ActivityLogFields {
    const val ID = "id"
    const val DATE = "date"
    const val PARTY = "party"
    const val DOC_TYPE = "doc_type"
    const val ATTRIBUTES = "attributes"
    const val ERROR = "error"
}

const val TABLE_ACTIVITY_LOGS = "activity_logs"

@Entity(
    tableName = TABLE_ACTIVITY_LOGS,
    indices = [
        Index(value = [ActivityLogFields.DATE])
    ]
)
data class LogEntryEntity(
    @ColumnInfo(ActivityLogFields.ID)
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(ActivityLogFields.DATE)
    val date: Instant,
    @ColumnInfo(ActivityLogFields.PARTY)
    val party: String,
    @ColumnInfo(ActivityLogFields.DOC_TYPE)
    val docType: DocType,
    @ColumnInfo(ActivityLogFields.ATTRIBUTES)
    val attributes: JsonObject? = null,
    @ColumnInfo(ActivityLogFields.ERROR)
    val error: AppError? = null
)

object KeyAttestationFields {
    const val ID = "id"
    const val ATTESTATION = "attestation"
    const val KEY_TYPE = "key_type"
}

const val TABLE_KEY_ATTESTATIONS = "key_attestations"

@Entity(
    tableName = TABLE_KEY_ATTESTATIONS
)
data class KeyAttestationEntity(
    @ColumnInfo(KeyAttestationFields.ID)
    @PrimaryKey
    val id: String,
    @ColumnInfo(KeyAttestationFields.ATTESTATION)
    val attestation: String,
    @ColumnInfo(KeyAttestationFields.KEY_TYPE)
    val keyType: String
)

object AttestationFields {
    const val ID = "id"
    const val CREDENTIAL = "credential"
    const val ISSUED_AT = "issued_at"
    const val TYPE = "type"
    const val KEY_ATTESTATION = "key_attestation"
}

const val TABLE_ATTESTATION = "attestations"

@Entity(
    tableName = TABLE_ATTESTATION,
    indices = [
        Index(value = [AttestationFields.KEY_ATTESTATION])
    ]
)
data class AttestationEntity(
    @ColumnInfo(AttestationFields.ID)
    @PrimaryKey
    val id: String,
    @ColumnInfo(AttestationFields.ISSUED_AT)
    val issuedAt: Instant = Clock.System.now(),
    @ColumnInfo(AttestationFields.CREDENTIAL)
    val credential: String,
    @ColumnInfo(AttestationFields.TYPE)
    val type: CredentialType,
    @ColumnInfo(AttestationFields.KEY_ATTESTATION)
    val keyAttestationId: String
)

data class AttestationAttestationKey(
    @Embedded val attestation: AttestationEntity,
    @Relation(
        parentColumn = AttestationFields.KEY_ATTESTATION,
        entityColumn = KeyAttestationFields.ID
    )
    val keyAttestation: KeyAttestationEntity
)
