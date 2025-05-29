package ee.cyber.wallet.ui.screens.error

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ee.cyber.wallet.R
import ee.cyber.wallet.domain.AppError
import ee.cyber.wallet.ui.components.AppContent
import ee.cyber.wallet.ui.components.ErrorCard
import ee.cyber.wallet.ui.components.PrimaryButton
import ee.cyber.wallet.ui.components.WSpace
import ee.cyber.wallet.ui.theme.PreviewThemes
import ee.cyber.wallet.ui.theme.WalletThemePreviewSurface

@PreviewThemes
@Composable
private fun AppErrorScreenPreview() {
    WalletThemePreviewSurface {
        AppErrorScreen(AppError.CONNECTION_ERROR)
    }
}

@Composable
fun AppErrorScreen(appError: AppError, onCloseClicked: () -> Unit = {}) {
    AppContent {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            WSpace()
            ErrorCard(message = stringResource(appError.resId))
            WSpace()
            PrimaryButton(text = stringResource(R.string.close_btn), onClick = onCloseClicked)
        }
    }
}
