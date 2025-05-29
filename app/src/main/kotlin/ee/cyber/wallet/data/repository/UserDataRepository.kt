package ee.cyber.wallet.data.repository

import ee.cyber.wallet.data.datastore.UserPreferencesDataSource
import ee.cyber.wallet.data.datastore.UserSessionDataSource
import ee.cyber.wallet.ui.model.UserData
import kotlinx.coroutines.flow.combine

class UserDataRepository(
    userPreferencesDataSource: UserPreferencesDataSource,
    userSessionDataSource: UserSessionDataSource
) {

    val userData = combine(
        userPreferencesDataSource.userPreferences,
        userSessionDataSource.userSession
    ) { prefs, session ->
        UserData(prefs = prefs, session = session)
    }
}
