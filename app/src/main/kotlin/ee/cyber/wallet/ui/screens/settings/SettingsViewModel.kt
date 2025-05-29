package ee.cyber.wallet.ui.screens.settings

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.cyber.wallet.data.repository.AccountRepository
import ee.cyber.wallet.domain.AndroidLocaleManager
import ee.cyber.wallet.ui.util.LanguageResource
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UiState(
    val language: LanguageResource
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val androidLocaleManager: AndroidLocaleManager,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _state = mutableStateOf(
        UiState(LanguageResource.fromLocale(androidLocaleManager.getApplicationLocale()))
    )
    val state: State<UiState> by lazy { _state }

    fun updateLocales() {
        val locale = androidLocaleManager.getApplicationLocale()
        _state.value = _state.value.copy(language = LanguageResource.fromLocale(locale))
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun deleteAllAndRestart() {
        GlobalScope.launch {
            accountRepository.deleteAllData()
        }
    }
}
