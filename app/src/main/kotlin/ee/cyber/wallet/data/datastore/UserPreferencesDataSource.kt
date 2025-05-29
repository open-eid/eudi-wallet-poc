package ee.cyber.wallet.data.datastore

import androidx.datastore.core.DataStore
import ee.cyber.wallet.DarkThemeConfigProto
import ee.cyber.wallet.UserPreferencesProto
import ee.cyber.wallet.copy
import ee.cyber.wallet.ui.model.DarkThemeConfig
import ee.cyber.wallet.ui.model.UserPreferences
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class UserPreferencesDataSource(
    private val dataStore: DataStore<UserPreferencesProto>
) {

    val userPreferences = dataStore.data.map { it.toModel() }.distinctUntilChanged()

    suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        runCatching {
            dataStore.updateData {
                it.copy {
                    this.darkThemeConfig = when (darkThemeConfig) {
                        DarkThemeConfig.DARK -> DarkThemeConfigProto.DARK_THEME_CONFIG_DARK
                        DarkThemeConfig.FOLLOW_SYSTEM -> DarkThemeConfigProto.DARK_THEME_CONFIG_FOLLOW_SYSTEM
                        DarkThemeConfig.LIGHT -> DarkThemeConfigProto.DARK_THEME_CONFIG_LIGHT
                    }
                }
            }
        }
    }

    private fun UserPreferencesProto.toModel(): UserPreferences {
        return UserPreferences(
            darkThemeConfig = when (darkThemeConfig) {
                DarkThemeConfigProto.DARK_THEME_CONFIG_DARK -> DarkThemeConfig.DARK
                DarkThemeConfigProto.DARK_THEME_CONFIG_LIGHT -> DarkThemeConfig.LIGHT
                else -> DarkThemeConfig.FOLLOW_SYSTEM
            }
        )
    }

    suspend fun clearAll() = runCatching {
        dataStore.updateData { UserPreferencesProto.getDefaultInstance() }
    }
}
