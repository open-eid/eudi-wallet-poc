package ee.cyber.wallet.ui.model

data class UserData(
    val prefs: UserPreferences,
    val session: UserSession
) {
    val isPinCreated
        get() = session.pin.isNotEmpty()
}
