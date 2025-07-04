package ee.cyber.wallet.ui.screens.settings

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.cyber.wallet.data.datastore.UserPreferencesDataSource
import ee.cyber.wallet.data.repository.AccountRepository
import ee.cyber.wallet.domain.AndroidLocaleManager
import ee.cyber.wallet.ui.util.LanguageResource
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UiState(
    val language: LanguageResource,
    val blePeripheralMode: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val androidLocaleManager: AndroidLocaleManager,
    private val accountRepository: AccountRepository,
    private val userPreferencesDataSource: UserPreferencesDataSource
) : ViewModel() {

    private val _state = mutableStateOf(
        UiState(LanguageResource.fromLocale(androidLocaleManager.getApplicationLocale()))
    )
    val state: State<UiState> by lazy { _state }

    init {
        userPreferencesDataSource.userPreferences
            .onEach { prefs ->
                _state.value = _state.value.copy(blePeripheralMode = prefs.blePeripheralMode)
            }
            .launchIn(viewModelScope)
    }

    fun updateLocales() {
        val locale = androidLocaleManager.getApplicationLocale()
        _state.value = _state.value.copy(language = LanguageResource.fromLocale(locale))
    }

    fun toggleBleMode() {
        viewModelScope.launch {
            userPreferencesDataSource.setBlePeripheralMode(!_state.value.blePeripheralMode)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun deleteAllAndRestart() {
        GlobalScope.launch {
            accountRepository.deleteAllData()
        }
    }
}
