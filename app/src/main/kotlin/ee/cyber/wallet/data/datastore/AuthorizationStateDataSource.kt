package ee.cyber.wallet.data.datastore

import androidx.datastore.core.DataStore
import ee.cyber.wallet.AuthorizationRequestStateProto
import ee.cyber.wallet.authorizationStateWithKeyProto
import ee.cyber.wallet.copy
import ee.cyber.wallet.domain.credentials.IssuanceAuthorizationState
import ee.cyber.wallet.util.fromBase64Json
import ee.cyber.wallet.util.toBase64Json
import eu.europa.ec.eudi.openid4vci.AuthorizationRequestPrepared
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

class AuthorizationStateDataSource(private val dataStore: DataStore<AuthorizationRequestStateProto>) {

    suspend fun get(state: String) = runCatching {
        dataStore.data
            .mapNotNull { it.statesMap[state] }
            .map {
                IssuanceAuthorizationState(
                    request = it.state.fromBase64Json<AuthorizationRequestPrepared>(),
                    keyId = it.keyId
                )
            }
            .firstOrNull()
    }

    suspend fun add(state: String, authorizationState: IssuanceAuthorizationState) = runCatching {
        dataStore.updateData {
            it.copy {
                this.states.put(
                    state,
                    authorizationStateWithKeyProto {
                        this.state = authorizationState.request.toBase64Json()
                        this.keyId = authorizationState.keyId
                    }
                )
            }
        }
    }

    suspend fun remove(state: String) = runCatching {
        dataStore.updateData { proto ->
            proto.copy {
                this.states.remove(state)
            }
        }
    }

    suspend fun clearAll() = runCatching {
        dataStore.updateData { AuthorizationRequestStateProto.getDefaultInstance() }
    }
}
