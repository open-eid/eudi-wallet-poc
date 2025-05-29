package ee.cyber.wallet.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalInspectionMode
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState

@Composable
fun requestPermission(permission: String): PermissionState {
    val notificationsPermissionState = when {
        LocalInspectionMode.current -> remember {
            object : PermissionState {
                override val permission = permission
                override val status = PermissionStatus.Granted
                override fun launchPermissionRequest() = Unit
            }
        }

        else -> rememberPermissionState(permission)
    }

    if (notificationsPermissionState.status is PermissionStatus.Denied) {
        LaunchedEffect(Unit) {
            notificationsPermissionState.launchPermissionRequest()
        }
    }
    return notificationsPermissionState
}
