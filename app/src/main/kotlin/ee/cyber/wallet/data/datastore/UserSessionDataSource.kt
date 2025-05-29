package ee.cyber.wallet.data.datastore

import androidx.datastore.core.DataStore
import ee.cyber.wallet.UserSessionProto
import ee.cyber.wallet.copy
import ee.cyber.wallet.ui.model.UserSession
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class UserSessionDataSource(private val dataStore: DataStore<UserSessionProto>) {

    val userSession = dataStore.data.map { UserSession(it.pin) }.distinctUntilChanged()

    suspend fun updatePin(pin: String) =
        runCatching {
            dataStore.updateData {
                it.copy {
                    this.pin = pin
                }
            }
        }

    suspend fun clearAll() = runCatching {
        dataStore.updateData { UserSessionProto.getDefaultInstance() }
    }
}
