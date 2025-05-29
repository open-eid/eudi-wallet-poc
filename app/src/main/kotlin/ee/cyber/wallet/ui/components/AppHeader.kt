package ee.cyber.wallet.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ee.cyber.wallet.R
import ee.cyber.wallet.ui.theme.PreviewThemes
import ee.cyber.wallet.ui.theme.WalletThemePreviewSurface

@PreviewThemes
@Composable
private fun AppHeaderPreview() {
    WalletThemePreviewSurface {
        AppHeader()
    }
}

@Composable
fun AppHeader(painter: Painter = painterResource(R.drawable.placeholder_logo)) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Image(painter = painter, contentDescription = "")
    }
}

@PreviewThemes
@Composable
private fun SimpleNavigationHeaderPreview() {
    WalletThemePreviewSurface {
        SimpleNavigationHeader(
            "Some header",
            listOf(
                MenuItem("Item1"),
                MenuItem("Item2")
            )
        )
    }
}

@Composable
fun SimpleNavigationHeader(title: String, menuItems: List<MenuItem> = listOf(), onNavigateBack: () -> Unit = {}) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "")
        }
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        WSpace()
        if (menuItems.isNotEmpty()) Menu(items = menuItems)
        HSpace(4)
    }
}
