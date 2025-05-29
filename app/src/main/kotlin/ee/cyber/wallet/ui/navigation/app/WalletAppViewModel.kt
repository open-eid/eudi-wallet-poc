package ee.cyber.wallet.ui.navigation.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.cyber.wallet.data.repository.DocumentRepository
import ee.cyber.wallet.data.repository.UserDataRepository
import ee.cyber.wallet.data.repository.WalletCredentialsRepository
import ee.cyber.wallet.ui.util.toStateFlow
import javax.inject.Inject

@HiltViewModel
class WalletAppViewModel @Inject constructor(
    userDataRepository: UserDataRepository,
    documentRepository: DocumentRepository,
    walletCredentialsRepository: WalletCredentialsRepository
) : ViewModel() {

    val userData = userDataRepository.userData.toStateFlow(viewModelScope, null)
    val walletCredentials = walletCredentialsRepository.credentials.toStateFlow(viewModelScope, null)

    val documents = documentRepository.documents.toStateFlow(viewModelScope, null)
}
