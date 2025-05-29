package ee.cyber.wallet.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import ee.cyber.wallet.R
import ee.cyber.wallet.ui.theme.PreviewThemes
import ee.cyber.wallet.ui.theme.WalletThemePreviewSurface

@Composable
@PreviewThemes
private fun ConfirmationDialogPreview() {
    WalletThemePreviewSurface {
        Box(Modifier.fillMaxSize()) {
            ConfirmationDialog(
                title = "Lorem ipsum",
                text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                confirmButtonText = stringResource(R.string.confirm_btn),
                cancelButtonText = stringResource(R.string.cancel_btn)
            )
        }
    }
}

@Composable
fun ConfirmationDialog(
    title: String,
    text: String,
    confirmButtonText: String = stringResource(R.string.confirm_btn),
    onConfirm: () -> Unit = {},
    cancelButtonText: String = stringResource(R.string.cancel_btn),
    onCancel: () -> Unit = {},
    cancellable: Boolean = true
) {
    AlertDialog(
        onDismissRequest = { if (cancellable) onCancel() },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmButtonText,
                    fontWeight = FontWeight.Normal
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(
                    text = cancelButtonText,
                    fontWeight = FontWeight.Normal
                )
            }
        },
        title = { Text(title) },
        text = { Text(text) }
    )
}
