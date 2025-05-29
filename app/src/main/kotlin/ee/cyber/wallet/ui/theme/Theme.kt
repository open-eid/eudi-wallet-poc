package ee.cyber.wallet.ui.theme

import android.app.Activity
import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = blue_600,
    onPrimary = white,
    primaryContainer = natural_200,
    onPrimaryContainer = natural_900,
    background = natural_200,
    onBackground = natural_900,
    secondary = white,
    onSecondary = natural_900,
    surface = natural_200,
    onSurface = natural_900,
    surfaceVariant = white,
    onSurfaceVariant = natural_900,
    surfaceTint = white,
    tertiary = white,
    onTertiary = natural_900,
    error = red_600,
    onError = white,
    errorContainer = red_600,
    onErrorContainer = white,

//    secondaryContainer = white,
//    onSecondaryContainer = white,
//    tertiaryContainer = white,
//    onTertiaryContainer = white,
//    outline = white,
//    inverseSurface = white,
//    inverseOnSurface = white,
//    inversePrimary = white,
//    outlineVariant = white,
    scrim = natural_600
)

@Composable
fun WalletTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = LightColors.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}

@Composable
fun WalletThemePreviewSurface(
    content: @Composable () -> Unit
) {
    WalletTheme {
        Surface(content = content)
    }
}

// @Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
annotation class PreviewThemes

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, device = Devices.NEXUS_5)
annotation class PreviewThemesSmallScreen
