package pr.com.kotlin_nyff.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pr.com.kotlin_nyff.R
import pr.com.kotlin_nyff.core.AppLanguage
import pr.com.kotlin_nyff.core.AppTheme
import pr.com.kotlin_nyff.ui.theme.NyffTheme

/** Qora sarlavhadagi "⋮": yorug' / tungi rejim va til (mijoz bosh sahifasida). */
@Composable
fun SettingsMenuButton(modifier: Modifier = Modifier) {
    val colors = NyffTheme.colors
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        RoundIconButton(
            Icons.Outlined.MoreVert,
            stringResource(R.string.settings),
            { open = true },
            colors.headerChip,
            colors.onHeader,
            size = 38.dp,
            iconSize = 20.dp,
        )
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            SettingsMenuItems(onDone = { open = false })
        }
    }
}

/**
 * Menyu bandlari: mavzu (yorug' ↔ tungi) va tillar (tanlangani belgilangan). Kuryer qismining "⋮" menyusida ham —
 * "Chiqish" dan oldin. Tanlansa ilova yangi mavzu / tilda qayta chiziladi (holat saqlanadi).
 */
@Composable
fun SettingsMenuItems(onDone: () -> Unit) {
    val context = LocalContext.current
    val dark = NyffTheme.colors.isDark
    val current = AppLanguage.current()
    DropdownMenuItem(
        text = { Text(stringResource(if (dark) R.string.theme_light else R.string.theme_dark)) },
        leadingIcon = { Icon(if (dark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode, contentDescription = null) },
        onClick = {
            onDone()
            AppTheme.apply(context, if (dark) AppTheme.LIGHT else AppTheme.DARK)
        },
    )
    HorizontalDivider()
    AppLanguage.entries.forEach { language ->
        val selected = language == current
        DropdownMenuItem(
            text = { Text(language.label, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal) },
            leadingIcon = { Icon(Icons.Outlined.Translate, contentDescription = null) },
            trailingIcon = if (selected) ({ Icon(Icons.Filled.Check, contentDescription = null) }) else null,
            onClick = {
                onDone()
                if (!selected) AppLanguage.apply(language)
            },
        )
    }
}
