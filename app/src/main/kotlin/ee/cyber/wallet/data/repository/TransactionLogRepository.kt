package ee.cyber.wallet.data.repository

import ee.cyber.wallet.data.database.LogEntryEntity
import ee.cyber.wallet.data.database.dao.LogRecordDao
import ee.cyber.wallet.domain.AppError
import ee.cyber.wallet.domain.credentials.DocType
import kotlinx.datetime.Clock
import kotlinx.serialization.json.JsonObject

class TransactionLogRepository(
    private val logRecordDao: LogRecordDao
) {

    val transactionLogs = logRecordDao.getAll()

    fun getTransactionLog(id: String) = logRecordDao.getById(id.toLong())

    suspend fun addTransactionLog(party: String, docType: DocType, attributes: JsonObject? = null, error: AppError? = null) =
        logRecordDao.insert(
            LogEntryEntity(
                date = Clock.System.now(),
                party = party,
                docType = docType,
                attributes = attributes,
                error = error
            )
        )
}
