package ee.cyber.wallet.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ee.cyber.wallet.AppConfig
import ee.cyber.wallet.R
import ee.cyber.wallet.domain.credentials.CredentialType
import ee.cyber.wallet.ui.components.AppContent
import ee.cyber.wallet.ui.components.FundingLogo
import ee.cyber.wallet.ui.components.VSpace
import ee.cyber.wallet.ui.navigation.app.WalletAppState
import ee.cyber.wallet.ui.navigation.app.rememberAppState
import ee.cyber.wallet.ui.theme.PreviewThemes
import ee.cyber.wallet.ui.theme.PreviewThemesSmallScreen
import ee.cyber.wallet.ui.theme.WalletThemePreviewSurface
import ee.cyber.wallet.ui.util.ContentAlpha
import ee.cyber.wallet.ui.util.document

@PreviewThemesSmallScreen
@Composable
private fun DashboardScreenSmallPreview() {
    WalletThemePreviewSurface {
        Dashboard(
            appState = rememberAppState(
                documents = listOf(
                    document(CredentialType.PID_SD_JWT, listOf())
                )
            ),
            navigationHandler = DashboardNavigationHandler()
        )
    }
}

@PreviewThemes
@Composable
private fun DashboardScreenPreview() {
    WalletThemePreviewSurface {
        Dashboard(navigationHandler = DashboardNavigationHandler())
    }
}

@Composable
fun DashboardScreen(appState: WalletAppState, navigationHandler: DashboardNavigationHandler) {
    Dashboard(appState, navigationHandler)
}

@Composable
private fun Dashboard(appState: WalletAppState = rememberAppState(), navigationHandler: DashboardNavigationHandler) {
    AppContent {
        DashboardContent(
            modifier = Modifier.weight(1.0f),
            appState = appState,
            navigationHandler = navigationHandler
        )
        Footer()
    }
}

@Composable
private fun Footer() {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        FundingLogo()
        Text(text = "Version ${AppConfig.appVersion}", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun DashboardContent(modifier: Modifier = Modifier, appState: WalletAppState, navigationHandler: DashboardNavigationHandler) {
    Column(modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.dashboard_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(text = appState.fullName, style = MaterialTheme.typography.bodyMedium)
        }
        VSpace(16.dp)
        DashboardActions(appState.documents?.isNotEmpty() == true, navigationHandler)
        VSpace(16.dp)
        DashboardNavigation(navigationHandler)
    }
}

@Composable
private fun DashboardNavigation(navigationHandler: DashboardNavigationHandler) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DashboardNavigationItem(
            title = stringResource(R.string.dashboard_my_documents)
        ) { navigationHandler.navigateToMyDocuments() }
        DashboardNavigationItem(stringResource(R.string.dashboard_activity_log)) { navigationHandler.navigateToActivityLog() }
        DashboardNavigationItem(stringResource(R.string.dashboard_settings)) { navigationHandler.navigateToSettings() }
    }
}

@Composable
private fun DashboardNavigationItem(title: String, subtitle: String? = null, onClick: () -> Unit = {}) {
    Card(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(48.dp)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "")
        }
    }
}

@Composable
private fun DashboardActions(hasDocuments: Boolean, navigationHandler: DashboardNavigationHandler) {
    Row(
        Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ActionCard(
            modifier = Modifier.weight(1.0f),
            title = stringResource(R.string.dashboard_scan_qr_code),
            enabled = hasDocuments,
            icon = {
                Icon(
                    modifier = Modifier.size(48.dp),
                    imageVector = Icons.Outlined.PhotoCamera,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            subtitle = stringResource(R.string.dashboard_from_the_verifier)
        ) { navigationHandler.navigateToScanner() }
    }
}

@Composable
private fun ActionCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String?,
    enabled: Boolean = true,
    icon: @Composable ColumnScope.() -> Unit,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.defaultMinSize(minHeight = 68.dp),
        enabled = enabled,
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon()
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            ContentAlpha(0.6f) {
                Text(
                    text = subtitle.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
