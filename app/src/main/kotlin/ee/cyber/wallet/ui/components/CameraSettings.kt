package ee.cyber.wallet.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ee.cyber.wallet.R
import ee.cyber.wallet.ui.theme.PreviewThemes
import ee.cyber.wallet.ui.theme.WalletThemePreviewSurface

@Composable
@PreviewThemes
private fun CameraSettingsCardPreview() {
    WalletThemePreviewSurface {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CameraSettingsCard()
        }
    }
}

@Composable
fun CameraSettingsCard() {
    val context = LocalContext.current
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = stringResource(R.string.scanner_no_permissions_title), style = MaterialTheme.typography.titleMedium)
            VSpace(8.dp)
            Text(text = stringResource(R.string.scanner_no_permissions_description), style = MaterialTheme.typography.bodyMedium)
            VSpace(24.dp)
            PrimaryButton(text = stringResource(R.string.scanner_go_to_settings), onClick = { context.showAppInfo() })
        }
    }
}

private fun Context.showAppInfo() {
    startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            data = Uri.parse("package:$packageName")
        }
    )
}
