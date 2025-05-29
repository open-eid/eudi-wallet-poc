package ee.cyber.wallet.ui.screens.pin

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ee.cyber.wallet.R
import ee.cyber.wallet.ui.components.AppContent
import ee.cyber.wallet.ui.components.AppHeader
import ee.cyber.wallet.ui.components.FullScreenFadedScrimProgressIndicator
import ee.cyber.wallet.ui.components.PrimaryButton
import ee.cyber.wallet.ui.components.VSpace
import ee.cyber.wallet.ui.components.WSpace
import ee.cyber.wallet.ui.theme.PreviewThemes
import ee.cyber.wallet.ui.theme.PreviewThemesSmallScreen
import ee.cyber.wallet.ui.theme.WalletThemePreviewSurface
import ee.cyber.wallet.ui.theme.green_600
import ee.cyber.wallet.ui.util.ContentColor
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

@Composable
@PreviewThemesSmallScreen
private fun PinEntryScreenPreview() {
    WalletThemePreviewSurface {
        PinEntry(
            PinViewState(
                isLoading = true,
                pinData = PinData.ConfirmPin.ConfirmPresentation("00", "Test Relying Party"),
                error = null
            )
        )
    }
}

@Composable
@PreviewThemesSmallScreen
private fun PinEntryIssuanceScreenPreview() {
    WalletThemePreviewSurface {
        PinEntry(
            PinViewState(
                isLoading = true,
                pinData = PinData.ConfirmPin.ConfirmIssuance("00", "Test Estonian Digital ID Issuer"),
                error = null
            )
        )
    }
}

@Composable
@PreviewThemes
private fun PinEntryScreenWithErrorPreview() {
    WalletThemePreviewSurface {
        PinEntry(
            PinViewState(
                isLoading = false,
                pinData = PinData.ConfirmPin.ConfirmPresentation("", "TEST RP"),
                error = PinError.IncorrectPin(2)
            )
        )
    }
}

@Composable
@PreviewThemes
private fun CreatePinScreenPreview() {
    WalletThemePreviewSurface {
        PinEntry(
            PinViewState(
                pinData = PinData.CreatePin("", false)
            )
        )
    }
}

@Composable
@PreviewThemes
private fun CreatePinConfirmScreenPreview() {
    WalletThemePreviewSurface {
        PinEntry(
            PinViewState(
                pinData = PinData.CreatePin("", true)
            )
        )
    }
}

@Composable
@PreviewThemes
private fun CreatePinConfirmScreenErrorPreview() {
    WalletThemePreviewSurface {
        PinEntry(
            PinViewState(
                pinData = PinData.CreatePin("", true),
                error = PinError.NotMatched
            )
        )
    }
}

@Composable
fun PinEntryScreen(viewModel: PinEntryViewModel, onSuccess: (pin: String) -> Unit, onFailure: () -> Unit = {}, onCancel: () -> Unit = {}) {
    LaunchedEffect(Unit) {
        viewModel.effect.onEach { effect ->
            when (effect) {
                is Effect.Result.Success -> {
                    onSuccess(effect.pin)
                }

                is Effect.Result.Failure -> {
                    onFailure()
                }

                is Effect.Result.Cancel -> {
                    onCancel()
                }
            }
        }.collect()
    }

    PinEntry(state = viewModel.state.value) {
        viewModel.onNumPadAction(it)
    }
}

@Composable
@PreviewThemes
private fun PinCreatedScreenPreview() {
    WalletThemePreviewSurface {
        PinCreatedScreen {
        }
    }
}

@Composable
fun PinCreatedScreen(onContinueClicked: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        AppContent {
            Text(
                modifier = Modifier.padding(horizontal = 24.dp),
                text = stringResource(R.string.pin_pin_successfully_created),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = green_600,
                textAlign = TextAlign.Center
            )
            WSpace()
            Icon(
                modifier = Modifier.size(128.dp),
                painter = painterResource(R.drawable.check_circle_w200),
                contentDescription = "",
                tint = green_600
            )
            WSpace()
            PrimaryButton(text = stringResource(R.string.continue_btn), onClick = onContinueClicked)
        }
    }
}

@Composable
private fun PinEntry(state: PinViewState, onAction: (NumPadAction) -> Unit = {}) {
    Box(Modifier.fillMaxSize()) {
        val configuration = LocalConfiguration.current
        AppContent(
            header = {
                if (configuration.screenHeightDp.dp > 640.dp) {
                    AppHeader()
                }
            }
        ) {
            PinEntryContent(
                modifier = Modifier.weight(1f),
                onAction = onAction,
                state = state
            )
        }
        FullScreenFadedScrimProgressIndicator(visible = state.isLoading)
    }
}

@Composable
private fun PinEntryContent(modifier: Modifier = Modifier, state: PinViewState, onAction: (NumPadAction) -> Unit = {}) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (state.pinData) {
            is PinData.ConfirmPin -> {
                val title = when (state.pinData) {
                    is PinData.ConfirmPin.ConfirmPresentation -> stringResource(R.string.pin_confirm_sharing_the_data_with)
                    is PinData.ConfirmPin.ConfirmIssuance -> stringResource(R.string.pin_confirm_issuance_by)
                }
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                Text(text = state.pinData.party, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            is PinData.CreatePin -> {
                Text(text = stringResource(R.string.pin_welcome_to_ee_wallet), style = MaterialTheme.typography.bodyLarge)
            }
        }
        VSpace(16.dp)
        NumPadView(
            state = state,
            onAction = onAction
        )
    }
}

@Composable
private fun NumPadView(modifier: Modifier = Modifier, state: PinViewState, onAction: (NumPadAction) -> Unit = {}) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val title = when (state.pinData) {
            is PinData.ConfirmPin -> stringResource(R.string.pin_enter_wallet_pin)
            is PinData.CreatePin -> {
                if (state.pinData.confirmation) stringResource(R.string.pin_re_enter_your_pin) else stringResource(R.string.pin_enter_your_new_pin)
            }
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold
        )

        Column(
            modifier = Modifier.shake(enabled = state.error != null),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ContentColor(if (state.error == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error) {
                Text(
                    text = state.error?.errorMessage().orEmpty(),
                    style = MaterialTheme.typography.titleMedium
                )
                VSpace(4.dp)
                Dots(state.pinData.pin.length)
            }
        }

        ContentColor(MaterialTheme.colorScheme.primary) {
            NumPad(
                modifier = Modifier.weight(1f),
                onAction = onAction
            )
        }
    }
}

@Composable
fun Dots(length: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        repeat(PIN_LENGTH) {
            Icon(
                imageVector = if (it >= length) Icons.Outlined.Circle else Icons.Filled.Circle,
                contentDescription = ""
            )
        }
    }
}

@Composable
fun NumPad(modifier: Modifier = Modifier, onAction: (NumPadAction) -> Unit = {}) {
    val haptic = LocalHapticFeedback.current
    LazyVerticalGrid(
        modifier = modifier.padding(24.dp),
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        items((1..9).toList()) { item ->
            NumPadItem(
                onClick = { onAction(NumPadAction.NumChar(item.digitToChar())) }
            ) {
                Text(
                    text = item.toString(),
                    color = LocalContentColor.current,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
        item {
            NumPadAction(
                onClick = { onAction(NumPadAction.Cancel) }
            ) {
                Text(
                    text = stringResource(R.string.cancel_btn),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        item {
            NumPadItem(
                onClick = { onAction(NumPadAction.NumChar('0')) }
            ) {
                Text(
                    text = "0",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
        item {
            NumPadAction(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onAction(NumPadAction.Delete)
                }
            ) {
                Icon(
                    modifier = Modifier.height(24.dp),
                    imageVector = Icons.AutoMirrored.Outlined.Backspace,
                    contentDescription = ""
                )
            }
        }
    }
}

@Composable
private fun NumPadItem(onClick: () -> Unit = {}, content: @Composable BoxScope.() -> Unit) {
    val haptic = LocalHapticFeedback.current
    Card(
        modifier = Modifier
            .aspectRatio(1f),
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        colors = CardDefaults.cardColors(contentColor = LocalContentColor.current),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
private fun NumPadAction(onClick: () -> Unit = {}, content: @Composable BoxScope.() -> Unit) {
    val haptic = LocalHapticFeedback.current
    Card(
        modifier = Modifier.aspectRatio(1f),
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        colors = CardDefaults.cardColors(contentColor = LocalContentColor.current, containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1f)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
fun Modifier.shake(repeats: Int = 8, offset: Float = 15f, duration: Int = 50, enabled: Boolean): Modifier = composed(
    factory = {
        var finished by remember(enabled) {
            mutableStateOf(!enabled)
        }
        val currentOffset by animateFloatAsState(
            targetValue = if (enabled) offset else 0.0f,
            animationSpec = repeatable(
                iterations = repeats,
                animation = tween(durationMillis = duration, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            finishedListener = { finished = true },
            label = ""
        )

        this@shake.then(
            Modifier.graphicsLayer {
                translationX = if (finished) 0f else currentOffset
            }
        )
    }
)

@Composable
private fun PinError.errorMessage() = when (this) {
    is PinError.IncorrectPin -> stringResource(R.string.pin_incorrect_pin_attempts_left, attemptsLeft)
    PinError.UnknownError -> stringResource(R.string.pin_unknown_error)
    PinError.NotMatched -> stringResource(R.string.pin_pins_don_t_match_please_try_again)
}
