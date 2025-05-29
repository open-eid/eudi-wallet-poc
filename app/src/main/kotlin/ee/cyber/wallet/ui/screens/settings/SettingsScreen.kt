package ee.cyber.wallet.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import ee.cyber.wallet.R
import ee.cyber.wallet.ui.components.AppContent
import ee.cyber.wallet.ui.components.ConfirmationDialog
import ee.cyber.wallet.ui.components.SimpleNavigationHeader
import ee.cyber.wallet.ui.components.VSpace
import ee.cyber.wallet.ui.theme.PreviewThemes
import ee.cyber.wallet.ui.theme.WalletThemePreviewSurface
import ee.cyber.wallet.ui.util.LanguageResource

data class SettingsNavigationHandler(
    val navigateBack: () -> Unit = {},
    val navigateToLanguage: () -> Unit = {}
)

@Composable
@PreviewThemes
private fun SettingsScreenPreview() {
    WalletThemePreviewSurface {
        SettingsContent(UiState(LanguageResource.ET))
    }
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, navigationHandler: SettingsNavigationHandler) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val locale = Locale.current
    LaunchedEffect(locale) {
        viewModel.updateLocales()
    }
    val state by viewModel.state
    SettingsContent(
        state = state,
        onDeleteAllClicked = { showDeleteConfirmation = true },
        navigationHandler = navigationHandler
    )

    if (showDeleteConfirmation) {
        DeleteAllConfirmation(onConfirm = {
            showDeleteConfirmation = false
            viewModel.deleteAllAndRestart()
        }, onClose = {
            showDeleteConfirmation = false
        })
    }
}

@Composable
private fun DeleteAllConfirmation(onConfirm: () -> Unit, onClose: () -> Unit) {
    ConfirmationDialog(
        title = stringResource(R.string.settings_delete_account_confirmation_title),
        text = stringResource(R.string.settings_delete_account_confirmation_text),
        confirmButtonText = stringResource(R.string.delete_btn),
        onConfirm = onConfirm,
        cancelButtonText = stringResource(R.string.cancel_btn),
        onCancel = onClose
    )
}

@Composable
private fun SettingsContent(state: UiState, onDeleteAllClicked: () -> Unit = {}, navigationHandler: SettingsNavigationHandler = SettingsNavigationHandler()) {
    AppContent(
        header = {
            SimpleNavigationHeader(title = stringResource(R.string.settings_title), onNavigateBack = navigationHandler.navigateBack)
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SettingItem(title = stringResource(R.string.settings_language), subtitle = stringResource(state.language.resId), onClick = navigationHandler.navigateToLanguage)
            VSpace(16)
            DeleteDataItem(onDeleteAllClicked)
        }
    }
}

@Composable
private fun DeleteDataItem(onClick: () -> Unit = {}) {
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
                    text = stringResource(R.string.settings_delete_all_item_title),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(R.string.settings_delete_all_item_text),
                    fontWeight = FontWeight.Normal,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "")
        }
    }
}

@Composable
private fun SettingItem(title: String, subtitle: String?, onClick: () -> Unit = {}) {
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
                    style = MaterialTheme.typography.titleMedium
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
