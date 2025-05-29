package ee.cyber.wallet.ui.util

import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

@Composable
fun ContentAlpha(alpha: Float, content: @Composable () -> Unit) = CompositionLocalProvider(
    value = LocalContentColor provides LocalContentColor.current.copy(alpha = alpha),
    content = content
)

@Composable
fun ContentColor(color: Color, content: @Composable () -> Unit) = CompositionLocalProvider(
    value = LocalContentColor provides color,
    content = content
)
