package ee.cyber.wallet.ui.screens.boarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ee.cyber.wallet.R
import ee.cyber.wallet.ui.components.AppContent
import ee.cyber.wallet.ui.components.PrimaryButton
import ee.cyber.wallet.ui.components.SecondaryButton
import ee.cyber.wallet.ui.components.VSpace
import ee.cyber.wallet.ui.components.WSpace
import ee.cyber.wallet.ui.screens.pin.NumPad
import ee.cyber.wallet.ui.screens.pin.NumPadAction
import ee.cyber.wallet.ui.screens.pin.PIN_LENGTH
import ee.cyber.wallet.ui.screens.pin.shake
import ee.cyber.wallet.ui.theme.green_600
import ee.cyber.wallet.ui.util.ContentColor

@Composable
fun AuthenticationMeansScreen(navigateToMobileIdAuth: () -> Unit = {}) {
    AppContent {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = "DEMO",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = stringResource(R.string.mock_auth_auth_mean),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        VSpace(24.dp)
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AuthenticationNavigationItem(
                stringResource(R.string.mock_auth_auth_mean_id_card_pin),
                stringResource(R.string.mock_auth_auth_mean_id_card_pin_details)
            ) { }
            AuthenticationNavigationItem(
                stringResource(R.string.mock_auth_auth_mean_biometry),
                stringResource(R.string.mock_auth_auth_mean_biometry_details)
            ) { }
            AuthenticationNavigationItem(
                stringResource(R.string.mock_auth_mobile_id),
                stringResource(R.string.mock_auth_mobile_id_details)
            ) { navigateToMobileIdAuth() }
            AuthenticationNavigationItem(stringResource(R.string.mock_auth_on_premise), stringResource(R.string.mock_auth_on_premise_details)) { }
        }
    }
}

@Composable
fun MobileIdAuthenticationScreen(onContinue: () -> Unit = {}, onCancel: () -> Unit = {}, viewModel: PidAuthenticationViewModel) {
    var phoneNumber by remember { mutableStateOf("37200000766") }
    var personalCode by remember { mutableStateOf("38001085718") }

    AppContent {
        Text(
            text = stringResource(R.string.mock_auth_mobile_id_auth_info),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        TextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text(stringResource(R.string.mock_auth_mobile_id_mobile_nr)) },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        TextField(
            value = personalCode,
            onValueChange = { personalCode = it },
            label = { Text(stringResource(R.string.mock_auth_mobile_id_pid)) },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        WSpace()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally)
        ) {
            SecondaryButton(text = stringResource(R.string.cancel_btn), onClick = onCancel)
            PrimaryButton(
                text = stringResource(R.string.confirm_btn),
                onClick = {
                    viewModel.sendEvent(Event.SetMockPid(personalCode))
                    onContinue()
                }
            )
        }
    }
}

@Composable
fun MobileIdControlCodeScreen(onContinue: () -> Unit = {}, onCancel: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = {
                VSpace(40.dp)
                Text(
                    text = stringResource(R.string.mock_auth_mobile_id_control_code),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(3.dp),
                    color = Color.White
                )
                Text(
                    text = "1234",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(3.dp),
                    color = Color.White
                )
                Text(
                    text = stringResource(R.string.mock_auth_mobile_id_confirm),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(3.dp),
                    color = Color.White
                )
                WSpace()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally)
                ) {
                    TextButton(onClick = onCancel) {
                        Text(
                            text = stringResource(R.string.cancel_btn),
                            fontWeight = FontWeight.Normal,
                            color = Color.White,
                            modifier = Modifier.padding(20.dp, 0.dp)
                        )
                    }
                    TextButton(onClick = onContinue) {
                        Text(
                            text = stringResource(R.string.confirm_btn),
                            fontWeight = FontWeight.Normal,
                            color = Color.White,
                            modifier = Modifier.padding(20.dp, 0.dp)
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun MobileIdPin1Screen(onContinue: () -> Unit = {}, onCancel: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = {
                Text(
                    text = stringResource(R.string.mock_auth_mobile_id_pin1),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp),
                    color = Color.White
                )
                VSpace(16.dp)
                NumPadView(
                    onContinue = onContinue,
                    onCancel = onCancel
                )
            }
        )
    }
}

@Composable
fun MobileIdSuccessScreen(onContinue: () -> Unit = {}) {
    Box(Modifier.fillMaxSize()) {
        AppContent {
            Text(
                modifier = Modifier.padding(horizontal = 24.dp),
                text = stringResource(R.string.mock_auth_mobile_id_sucess),
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
            PrimaryButton(text = stringResource(R.string.continue_btn), onClick = onContinue)
        }
    }
}

@Composable
fun NumPadView(onContinue: () -> Unit = {}, onCancel: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var pin by remember { mutableStateOf("") }
        fun onNumPadAction(action: NumPadAction) {
            when (action) {
                is NumPadAction.Cancel -> onCancel
                is NumPadAction.Delete -> {
                    pin = pin.dropLast(1)
                }

                is NumPadAction.NumChar -> {
                    if (pin.length < PIN_LENGTH) {
                        pin += action.char
                        if (pin.length == PIN_LENGTH) {
                            onContinue.invoke()
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier.shake(enabled = true),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ContentColor(MaterialTheme.colorScheme.primary) {
                VSpace(4.dp)
                TextField(
                    value = "*".repeat(pin.length),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }
        ContentColor(MaterialTheme.colorScheme.primary) {
            NumPad(
                modifier = Modifier.weight(1f),
                onAction = ::onNumPadAction
            )
        }
    }
}

@Composable
private fun AuthenticationNavigationItem(title: String, subtitle: String? = null, onClick: () -> Unit = {}) {
    Card(onClick = onClick) {
        Row(
            modifier = androidx.compose.ui.Modifier
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
