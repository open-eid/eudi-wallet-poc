package ee.cyber.wallet.ui.screens.dashboard

data class DashboardNavigationHandler(
    val navigateBack: () -> Unit = {},
    val navigateToScanner: () -> Unit = {},
    val navigateToPresenter: () -> Unit = {},
    val navigateToMyDocuments: () -> Unit = {},
    val navigateToActivityLog: () -> Unit = {},
    val navigateToSettings: () -> Unit = {}
)
