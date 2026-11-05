package pr.com.kotlin_nyff.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pr.com.kotlin_nyff.R
import pr.com.kotlin_nyff.core.AppLanguage
import pr.com.kotlin_nyff.core.AppTheme
import pr.com.kotlin_nyff.ui.theme.NyffShapes
import pr.com.kotlin_nyff.ui.theme.NyffTheme

/** Qora sarlavhadagi "⋮": profil, yorug' / tungi rejim va til (mijoz bosh sahifasida). */
@Composable
fun SettingsMenuButton(onProfile: () -> Unit, modifier: Modifier = Modifier) {
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
        NyffMenu(expanded = open, onDismiss = { open = false }) {
            MenuAction(
                Icons.Outlined.Person,
                stringResource(R.string.profile),
                onClick = {
                    open = false
                    onProfile()
                },
            )
            MenuDivider()
            SettingsMenuContent(onDone = { open = false })
        }
    }
}

/** Ochiluvchi menyu — ilova kartochkalari uslubida: oq, yumaloq, yengil soya; tungi rejimda chegara. */
@Composable
fun NyffMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    width: Dp = 220.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = NyffTheme.colors
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.width(width),
        shape = NyffShapes.card,
        containerColor = colors.surface,
        tonalElevation = 0.dp,
        shadowElevation = 8.dp,
        border = if (colors.isDark) BorderStroke(1.dp, colors.line) else null,
        content = content,
    )
}

/**
 * Menyu ichi: "Ko'rinish" (Yorug' | Tungi) va "Til" (UZ | RU | EN) — rasmiylashtirishdagi "Yetkazib berish | Olib
 * ketish" kabi sariq pill tanlov. Kuryer qismining "⋮" menyusida ham — "Chiqish" dan oldin. Tanlansa avval menyu
 * yopiladi, keyin ekran yangi mavzu / tilda joyida qayta chiziladi (activity qayta yaratilmaydi, holat saqlanadi).
 */
@Composable
fun SettingsMenuContent(onDone: () -> Unit) {
    val context = LocalContext.current
    val dark = NyffTheme.colors.isDark
    Column(
        Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        SectionLabel(stringResource(R.string.theme))
        Segments(
            options = listOf(false, true),
            selected = dark,
            label = { stringResource(if (it) R.string.theme_dark else R.string.theme_light) },
            icon = { if (it) Icons.Outlined.DarkMode else Icons.Outlined.LightMode },
            onSelect = { value ->
                onDone()
                if (value != dark) AppTheme.apply(context, if (value) AppTheme.DARK else AppTheme.LIGHT)
            },
        )
        Spacer(Modifier.height(2.dp))
        LanguageMenuContent(onDone)
    }
}

/** "Til": UZ | RU | EN. */
@Composable
fun LanguageMenuContent(onDone: () -> Unit) {
    val current = AppLanguage.fromTag(currentLanguage())
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SectionLabel(stringResource(R.string.language))
        Segments(
            options = AppLanguage.entries,
            selected = current,
            label = { it.tag.uppercase() },
            onSelect = { language ->
                onDone()
                if (language != current) AppLanguage.apply(language)
            },
        )
    }
}

/** Menyudagi oddiy amal ("Chiqish") — ixcham qator. */
@Composable
fun MenuAction(icon: ImageVector, text: String, onClick: () -> Unit, tint: Color = NyffTheme.colors.text) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = tint)
    }
}

@Composable
fun MenuDivider() {
    HorizontalDivider(Modifier.padding(vertical = 6.dp), color = NyffTheme.colors.line)
}

/** Pill tanlov (`OrderModeSwitch` uslubida): och fon, tanlangani sariq. */
@Composable
private fun <T> Segments(
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    icon: ((T) -> ImageVector)? = null,
) {
    val colors = NyffTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(colors.surfaceMuted)
            .padding(3.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        options.forEach { value ->
            val active = value == selected
            val tint = if (active) colors.onAccent else colors.textMuted
            Row(
                Modifier
                    .weight(1f)
                    .height(30.dp)
                    .clip(CircleShape)
                    .background(if (active) colors.accent else Color.Transparent)
                    .selectable(selected = active, role = Role.RadioButton, onClick = { onSelect(value) }),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (icon != null) {
                    Icon(icon(value), contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                }
                Text(label(value), style = MaterialTheme.typography.labelMedium, color = tint, maxLines = 1)
            }
        }
    }
}
