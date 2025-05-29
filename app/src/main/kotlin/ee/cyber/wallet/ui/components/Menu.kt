package ee.cyber.wallet.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ee.cyber.wallet.ui.theme.PreviewThemes
import ee.cyber.wallet.ui.theme.WalletThemePreviewSurface

data class MenuItem(
    val title: String,
    val onClick: () -> Unit = {},
    val icon: (@Composable () -> Unit)? = null
)

class MenuState(expanded: Boolean = false) {
    var expanded: Boolean by mutableStateOf(expanded)
}

@Composable
fun rememberMenuState(expanded: Boolean = false): MenuState {
    val state by remember {
        mutableStateOf(MenuState(expanded))
    }
    return state
}

@Composable
fun Menu(
    menuState: MenuState = rememberMenuState(),
    items: List<MenuItem> = listOf()
) {
    Menu(menuState = menuState, items = items) {
        IconButton(onClick = { menuState.expanded = !menuState.expanded }) {
            Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "")
        }
    }
}

@Composable
@PreviewThemes
private fun MenuPreview() {
    WalletThemePreviewSurface {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopEnd) {
            Menu(
                menuState = rememberMenuState(true),
                items = listOf(
                    MenuItem(
                        title = "Test",
                        icon = { Icon(imageVector = Icons.Filled.Delete, contentDescription = "") }
                    )
                )
            )
        }
    }
}

@Composable
fun Menu(
    modifier: Modifier = Modifier,
    menuState: MenuState = rememberMenuState(),
    items: List<MenuItem> = listOf(),
    alignment: Alignment = Alignment.TopEnd,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .wrapContentSize(alignment)
            .then(modifier)
    ) {
        content()
        DropdownMenu(
            expanded = menuState.expanded,
            onDismissRequest = { menuState.expanded = false }
        ) {
            items.forEach {
                DropdownMenuItem(
                    onClick = {
                        menuState.expanded = false
                        it.onClick()
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (it.icon != null) {
                                it.icon.invoke()
                                HSpace(width = 8.dp)
                            }
                            Text(text = it.title)
                        }
                    }
                )
            }
        }
    }
}
