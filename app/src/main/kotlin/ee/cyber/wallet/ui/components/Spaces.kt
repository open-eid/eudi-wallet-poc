package ee.cyber.wallet.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun VSpace(height: Dp = 0.dp) = Spacer(Modifier.height(height))

@Composable
fun VSpace(height: Int) = VSpace(height.dp)

@Composable
fun HSpace(width: Dp = 0.dp) = Spacer(Modifier.width(width))

@Composable
fun HSpace(width: Int) = HSpace(width.dp)

@Composable
fun ColumnScope.WSpace(weight: Float = 1.0f) = Spacer(Modifier.weight(weight))

@Composable
fun RowScope.WSpace(weight: Float = 1.0f) = Spacer(Modifier.weight(weight))
