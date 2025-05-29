package ee.cyber.wallet.ui.screens.settings.language

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import ee.cyber.wallet.R
import ee.cyber.wallet.ui.components.AppContent
import ee.cyber.wallet.ui.components.LanguageItemCard
import ee.cyber.wallet.ui.components.SimpleNavigationHeader
import ee.cyber.wallet.ui.theme.PreviewThemes
import ee.cyber.wallet.ui.theme.WalletThemePreviewSurface
import ee.cyber.wallet.ui.util.LanguageResource

@Composable
@PreviewThemes
private fun LanguageScreenPreview() {
    WalletThemePreviewSurface {
        LanguageContent(
            UiState(
                listOf(
                    LanguageItem(LanguageResource.EN, true),
                    LanguageItem(LanguageResource.ET, false)
                )
            )
        )
    }
}

@Composable
fun LanguageScreen(viewModel: LanguageViewModel, onNavigateBack: () -> Unit = {}) {
    val state by viewModel.state
    val locale = Locale.current
    LaunchedEffect(locale) {
        viewModel.updateLocales()
    }
    LanguageContent(
        state = state,
        onLanguageSelected = { viewModel.changeAppLanguage(it) },
        onNavigateBack = onNavigateBack
    )
}

@Composable
private fun LanguageContent(state: UiState, onLanguageSelected: (LanguageItem) -> Unit = {}, onNavigateBack: () -> Unit = {}) {
    AppContent(
        header = {
            SimpleNavigationHeader(title = stringResource(R.string.language_title), onNavigateBack = onNavigateBack)
        }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.languages, key = { it.languageResource.tag }) { language ->
                    LanguageItemCard(
                        title = stringResource(language.languageResource.resId),
                        subTitle = stringResource(language.languageResource.localizedResId),
                        selected = language.selected,
                        onClick = { if (!language.selected) onLanguageSelected(language) }
                    )
                }
            }
        }
    }
}
