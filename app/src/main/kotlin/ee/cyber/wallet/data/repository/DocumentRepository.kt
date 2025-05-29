package ee.cyber.wallet.data.repository

import ee.cyber.wallet.data.database.dao.AttestationDao
import ee.cyber.wallet.data.database.dao.KeyAttestationDao
import ee.cyber.wallet.data.database.toEntity
import ee.cyber.wallet.data.database.toModel
import ee.cyber.wallet.domain.documents.CredentialDocument
import ee.cyber.wallet.domain.documents.CredentialToDocumentMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.slf4j.LoggerFactory

class DocumentRepository(
    private val credentialToDocumentMapper: CredentialToDocumentMapper,
    private val attestationDao: AttestationDao,
    private val keyAttestationDao: KeyAttestationDao
) {

    private val logger = LoggerFactory.getLogger("DocumentRepository")

    val documents: Flow<List<CredentialDocument>> = attestationDao.getAll().map { attestation ->
        attestation.mapNotNull { credentialToDocumentMapper.convert(it.toModel()) }
    }

    fun getDocumentById(id: String): Flow<CredentialDocument?> = attestationDao.getById(id)
        .map { attestation ->
            attestation?.let { credentialToDocumentMapper.convert(it.toModel()) }
        }

    suspend fun addDocument(document: CredentialDocument) {
        attestationDao.insert(document.attestation.toEntity())
    }

    suspend fun deleteDocument(id: String) {
        val keyAttestationId = attestationDao.getById(id).first()?.keyAttestation?.id
        attestationDao.deleteById(id)
        keyAttestationId?.also { keyAttestationDao.deleteById(it) }
    }

    suspend fun deleteAll() {
        attestationDao.deleteAll()
    }
}
