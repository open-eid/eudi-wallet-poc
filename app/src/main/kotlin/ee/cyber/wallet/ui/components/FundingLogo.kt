package ee.cyber.wallet.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ee.cyber.wallet.R
import ee.cyber.wallet.ui.theme.PreviewThemes
import ee.cyber.wallet.ui.theme.WalletThemePreviewSurface

@Composable
@PreviewThemes
private fun FundingLogoPreview() {
    WalletThemePreviewSurface {
        Box(Modifier.fillMaxSize()) {
            FundingLogo()
        }
    }
}

@Composable
fun FundingLogo(painter: Painter = painterResource(R.drawable.funding_logo)) {
    Image(
        modifier = Modifier.width(200.dp),
        painter = painter,
        contentDescription = ""
    )
}
