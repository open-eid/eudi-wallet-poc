package ee.cyber.wallet.data.datastore

import androidx.datastore.core.DataStore
import ee.cyber.wallet.WalletInstanceCredentialsProto
import ee.cyber.wallet.copy
import ee.cyber.wallet.domain.provider.wallet.WalletInstanceCredentials
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class WalletInstanceCredentialsDataSource(private val dataStore: DataStore<WalletInstanceCredentialsProto>) {

    val credentials = dataStore.data
        .map { WalletInstanceCredentials(it.instanceId, it.instancePassword) }
        .distinctUntilChanged()

    suspend fun updateCredentials(instanceId: String, instancePassword: String) =
        runCatching {
            dataStore.updateData {
                it.copy {
                    this.instanceId = instanceId
                    this.instancePassword = instancePassword
                }
            }
        }

    suspend fun clearAll() = runCatching {
        dataStore.updateData { WalletInstanceCredentialsProto.getDefaultInstance() }
    }
}
