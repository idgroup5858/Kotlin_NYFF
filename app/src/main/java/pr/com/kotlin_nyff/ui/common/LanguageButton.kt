package pr.com.kotlin_nyff.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import pr.com.kotlin_nyff.R
import pr.com.kotlin_nyff.core.AppLanguage
import pr.com.kotlin_nyff.ui.theme.NyffTheme

/** Til: UZ / RU / EN (qora sarlavhada; mijoz va kuryer qismida) — tanlansa ilova shu tilda qayta ochiladi (holat saqlanadi). */
@Composable
fun LanguageButton(modifier: Modifier = Modifier) {
    val colors = NyffTheme.colors
    var open by remember { mutableStateOf(false) }
    val current = AppLanguage.current()
    Box(modifier) {
        Row(
            Modifier
                .height(38.dp)
                .clip(CircleShape)
                .background(colors.headerChip)
                .clickable(role = Role.Button) { open = true }
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.Translate, contentDescription = stringResource(R.string.language), tint = colors.onHeader, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(current.tag.uppercase(), style = MaterialTheme.typography.labelMedium, color = colors.onHeader)
        }
        NyffMenu(expanded = open, onDismiss = { open = false }, width = 180.dp) {
            Box(Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                LanguageMenuContent(onDone = { open = false })
            }
        }
    }
}
