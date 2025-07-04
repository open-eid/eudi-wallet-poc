package ee.cyber.wallet.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ee.cyber.wallet.ui.theme.PreviewThemes
import ee.cyber.wallet.ui.theme.WalletThemePreviewSurface

@Composable
@PreviewThemes
private fun LanguageItemCardPreview() {
    WalletThemePreviewSurface {
        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LanguageItemCard(title = "English", subTitle = "", selected = true)
            LanguageItemCard(title = "Eesti keel", subTitle = "Estonian", selected = false)
        }
    }
}

@Composable
fun LanguageItemCard(title: String, subTitle: String?, selected: Boolean, onClick: () -> Unit = {}) {
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
                if (subTitle?.isNotBlank() == true) {
                    VSpace(4)
                    Text(
                        text = subTitle,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            if (selected) {
                Icon(Icons.Rounded.Check, contentDescription = "", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
