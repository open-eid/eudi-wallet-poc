package ee.cyber.wallet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ee.cyber.wallet.ui.theme.PreviewThemes
import ee.cyber.wallet.ui.theme.WalletThemePreviewSurface

@Composable
@PreviewThemes
private fun AppContentPreview() {
    WalletThemePreviewSurface {
        AppContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Blue)
            )
        }
    }
}

@Composable
@PreviewThemes
private fun AppContentCustomHeaderPreview() {
    WalletThemePreviewSurface {
        AppContent(
            header = {
                SimpleNavigationHeader(title = "This is a title")
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Blue)
            )
        }
    }
}

@Composable
fun AppContent(
    modifier: Modifier = Modifier,
    header: @Composable (() -> Unit) = { AppHeader() },
    content: @Composable (ColumnScope.() -> Unit)
) {
    Column(modifier = modifier) {
        header()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}
