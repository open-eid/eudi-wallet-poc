package ee.cyber.wallet.ui.model

enum class DarkThemeConfig {
    FOLLOW_SYSTEM, LIGHT, DARK
}

data class UserPreferences(
    val darkThemeConfig: DarkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
    val blePeripheralMode: Boolean = true
)
