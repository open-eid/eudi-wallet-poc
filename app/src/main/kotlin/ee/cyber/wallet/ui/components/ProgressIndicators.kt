package ee.cyber.wallet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun FadedProgressIndicator(modifier: Modifier = Modifier, visible: Boolean = true) {
    FadedVisibility(modifier, visible) {
        if (LocalInspectionMode.current) {
            // show 75% in previews
            CircularProgressIndicator(progress = { 0.75f })
        } else {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun FullScreenFadedScrimProgressIndicator(modifier: Modifier = Modifier, visible: Boolean = true) {
    FadedVisibility(modifier = Modifier.fillMaxSize(), visible = visible) {
        Box(
            modifier
                .fillMaxSize()
                .clickable(enabled = false) { }
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            if (LocalInspectionMode.current) {
                // show 75% in previews
                CircularProgressIndicator(progress = { 0.75f })
            } else {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun FadedLinerProgressIndicator(height: Dp = 4.dp, visible: Boolean = true) {
    FadedVisibility(visible = visible) {
        if (LocalInspectionMode.current) {
            // show 75% in previews
            LinearProgressIndicator(
                modifier = Modifier
                    .height(height)
                    .fillMaxWidth(),
                progress = { 0.75f }
            )
        } else {
            LinearProgressIndicator(
                modifier = Modifier
                    .height(height)
                    .fillMaxWidth()
            )
        }
    }
}
