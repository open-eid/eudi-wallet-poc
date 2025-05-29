package ee.cyber.wallet.ui.components

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun HDivider(thickness: Int = 1, color: Color = MaterialTheme.colorScheme.background) = HorizontalDivider(thickness = thickness.dp, color = color)
