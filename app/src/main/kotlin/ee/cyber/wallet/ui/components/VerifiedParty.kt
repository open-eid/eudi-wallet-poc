package ee.cyber.wallet.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ee.cyber.wallet.R

@Composable
fun VerifiedParty() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = Icons.Outlined.VerifiedUser,
            contentDescription = "",
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(R.string.presentation_verified_party),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
