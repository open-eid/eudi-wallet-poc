package ee.cyber.wallet.ui.screens.activity

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.cyber.wallet.data.repository.TransactionLogRepository
import javax.inject.Inject

@HiltViewModel
class ActivityLogViewModel @Inject constructor(
    transactionLogRepository: TransactionLogRepository
) : ViewModel() {

    val transactionLogs = transactionLogRepository.transactionLogs
}
