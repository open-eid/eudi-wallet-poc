package ee.cyber.wallet.ui.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn

fun <T> Flow<T>.toStateFlow(
    scope: CoroutineScope,
    default: T,
    started: SharingStarted = SharingStarted.Lazily
) = stateIn(
    scope = scope,
    started = started,
    initialValue = default
)

fun <T> Flow<T>.toSharedFlow(
    scope: CoroutineScope,
    started: SharingStarted = SharingStarted.Lazily
) = shareIn(
    scope = scope,
    started = started
)

fun <T, K> StateFlow<T>.mapState(
    scope: CoroutineScope,
    transform: (data: T) -> K
): StateFlow<K> = mapLatest {
    transform(it)
}.stateIn(scope, SharingStarted.Eagerly, transform(value))
