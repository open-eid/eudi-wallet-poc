package ee.cyber.wallet.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ee.cyber.wallet.ui.theme.PreviewThemes
import ee.cyber.wallet.ui.theme.WalletThemePreviewSurface

@Composable
fun PrimaryButton(modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit = {}, content: @Composable RowScope.() -> Unit) {
    Button(
        modifier = modifier.defaultMinSize(minWidth = 160.dp, minHeight = 48.dp),
        enabled = enabled,
        onClick = onClick
    ) {
        content()
    }
}

@Composable
fun PrimaryButton(modifier: Modifier = Modifier, text: String, enabled: Boolean = true, onClick: () -> Unit = {}) {
    PrimaryButton(modifier = modifier, enabled = enabled, onClick = onClick) {
        Text(text)
    }
}

@Composable
@PreviewThemes
private fun PrimaryButtonPreview() {
    WalletThemePreviewSurface {
        Column {
            PrimaryButton(text = "Primary")
            PrimaryButton {
                Text(text = "Primary2")
            }
        }
    }
}

@Composable
fun SecondaryButton(modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit = {}, content: @Composable RowScope.() -> Unit) {
    OutlinedButton(
        modifier = modifier.defaultMinSize(minWidth = 160.dp, minHeight = 48.dp),
        enabled = enabled,
        border = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.primary),
        onClick = onClick
    ) {
        content()
    }
}

@Composable
fun SecondaryButton(modifier: Modifier = Modifier, text: String, enabled: Boolean = true, onClick: () -> Unit = {}) {
    SecondaryButton(modifier = modifier, enabled = enabled, onClick = onClick) {
        Text(text)
    }
}

@Composable
@PreviewThemes
private fun SecondaryButtonPreview() {
    WalletThemePreviewSurface {
        Column {
            SecondaryButton(text = "Secondary")
            SecondaryButton {
                Text(text = "Secondary2")
            }
        }
    }
}
