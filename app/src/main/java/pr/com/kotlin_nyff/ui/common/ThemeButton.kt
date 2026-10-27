package pr.com.kotlin_nyff.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pr.com.kotlin_nyff.R
import pr.com.kotlin_nyff.core.AppTheme
import pr.com.kotlin_nyff.ui.theme.NyffTheme

/** Yorug' / tungi rejim (qora sarlavhada, til tugmasi yonida): oy — tungiga o'tish, quyosh — yorug'ga. */
@Composable
fun ThemeButton(modifier: Modifier = Modifier) {
    val colors = NyffTheme.colors
    val context = LocalContext.current
    val dark = colors.isDark
    RoundIconButton(
        icon = if (dark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
        contentDescription = stringResource(if (dark) R.string.theme_light else R.string.theme_dark),
        onClick = { AppTheme.apply(context, if (dark) AppTheme.LIGHT else AppTheme.DARK) },
        background = colors.headerChip,
        tint = colors.onHeader,
        modifier = modifier,
        size = 38.dp,
        iconSize = 18.dp,
    )
}
