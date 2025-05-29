package ee.cyber.wallet.ui.screens.scanner

import android.Manifest
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.PermissionStatus
import ee.cyber.wallet.R
import ee.cyber.wallet.ui.components.AppContent
import ee.cyber.wallet.ui.components.BarcodeScanner
import ee.cyber.wallet.ui.components.CameraSettingsCard
import ee.cyber.wallet.ui.components.SecondaryButton
import ee.cyber.wallet.ui.components.VSpace
import ee.cyber.wallet.ui.components.WSpace
import ee.cyber.wallet.ui.theme.PreviewThemes
import ee.cyber.wallet.ui.theme.PreviewThemesSmallScreen
import ee.cyber.wallet.ui.theme.WalletThemePreviewSurface
import ee.cyber.wallet.ui.util.requestPermission
import org.slf4j.LoggerFactory

@Composable
@PreviewThemes
private fun ScannerScreenPreview() {
    WalletThemePreviewSurface {
        Scanner(PermissionStatus.Granted)
    }
}

@Composable
@PreviewThemesSmallScreen
private fun ScannerScreenSmallPreview() {
    WalletThemePreviewSurface {
        Scanner(PermissionStatus.Granted)
    }
}

@Composable
@PreviewThemes
private fun ScannerScreenNoPermissionsPreview() {
    WalletThemePreviewSurface {
        Scanner(PermissionStatus.Denied(false))
    }
}

private val logger = LoggerFactory.getLogger("ScannerScreen")

@Composable
fun ScannerScreen(onBack: () -> Unit = {}, onQRCodeScanned: (String) -> Unit = {}) {
    val permissionState = requestPermission(permission = Manifest.permission.CAMERA)

    Scanner(
        permissionState.status,
        onBarcodeDetected = {
            onQRCodeScanned(it)
        },
        onCancelClicked = { onBack() }
    )
}

@Composable
private fun Scanner(
    permissionStatus: PermissionStatus,
    onBarcodeDetected: (String) -> Unit = {},
    onCancelClicked: () -> Unit = {}
) {
    AppContent {
        when (permissionStatus) {
            PermissionStatus.Granted -> {
                ScannerContent(
                    onBarcodeDetected = onBarcodeDetected,
                    onCancelClicked = onCancelClicked
                )
            }

            is PermissionStatus.Denied -> {
                NoPermissionContent(onCancelClicked)
            }
        }
    }
}

@Composable
private fun NoPermissionContent(onCancelClicked: () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WSpace()
        CameraSettingsCard()
        WSpace()
        SecondaryButton(text = stringResource(R.string.cancel_btn), onClick = onCancelClicked)
    }
}

@Composable
private fun ScannerContent(onBarcodeDetected: (String) -> Unit = {}, onCancelClicked: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = stringResource(R.string.scanner_scan_qr_code), style = MaterialTheme.typography.headlineMedium)
            Text(text = stringResource(R.string.scanner_focus_note), style = MaterialTheme.typography.bodyMedium)
        }
        VSpace(24.dp)
        BarcodeScanner(
            modifier = Modifier.requiredSize(320.dp),
            onBarcodeDetected = {
                onBarcodeDetected(it)
            }
        )
        WSpace()
        SecondaryButton(text = stringResource(R.string.cancel_btn), onClick = onCancelClicked)
    }
}
