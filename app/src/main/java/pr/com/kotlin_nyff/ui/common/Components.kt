package pr.com.kotlin_nyff.ui.common

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.DeliveryDining
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import coil3.compose.AsyncImage
import pr.com.kotlin_nyff.R
import pr.com.kotlin_nyff.data.model.CustomerOrder
import pr.com.kotlin_nyff.domain.OrderHeadline
import pr.com.kotlin_nyff.domain.OrderMode
import pr.com.kotlin_nyff.domain.OrderProgress
import pr.com.kotlin_nyff.ui.theme.Coal
import pr.com.kotlin_nyff.ui.theme.NyffShapes
import pr.com.kotlin_nyff.ui.theme.NyffTheme

/** Taom / kategoriya rasmi serverdan (`/images/...`); yo'q bo'lsa — kulrang fon va belgi. */
@Composable
fun FoodImage(path: String, modifier: Modifier = Modifier, shape: Shape = RectangleShape, iconSize: Dp = 28.dp) {
    val colors = NyffTheme.colors
    Box(modifier.clip(shape).background(colors.surfaceMuted), contentAlignment = Alignment.Center) {
        val url = imageUrl(path)
        if (url == null) {
            Icon(Icons.Outlined.Fastfood, contentDescription = null, tint = colors.textFaint, modifier = Modifier.size(iconSize))
        } else {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}

/** Oq yumaloq kartochka: yorug' mavzuda yengil soya, tungida chegara (web `surface` + `shadow-card`). */
@Composable
fun NyffCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = NyffShapes.card,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = NyffTheme.colors
    val outline = if (colors.isDark) {
        Modifier.border(1.dp, colors.line, shape)
    } else {
        Modifier.shadow(6.dp, shape, ambientColor = Coal.copy(alpha = 0.10f), spotColor = Coal.copy(alpha = 0.22f))
    }
    Column(
        modifier
            .then(outline)
            .clip(shape)
            .background(colors.surface)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        content = content,
    )
}

@Composable
fun RoundIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    background: Color,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    iconSize: Dp = 18.dp,
) {
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(background)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(iconSize))
    }
}

/** − son + (savat qatori). */
@Composable
fun QtyStepper(qty: Int, onChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    val colors = NyffTheme.colors
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        RoundIconButton(Icons.Filled.Remove, null, { onChange(-1) }, colors.surfaceMuted, colors.text)
        Text(
            qty.toString(),
            modifier = Modifier.width(30.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall,
            color = colors.text,
        )
        RoundIconButton(Icons.Filled.Add, null, { onChange(1) }, colors.accent, colors.onAccent)
    }
}

/** Yetkazib berish / olib ketish tanlovi (bosh sahifa — qora fonda, rasmiylashtirish — och fonda). */
@Composable
fun OrderModeSwitch(mode: OrderMode, onChange: (OrderMode) -> Unit, onDark: Boolean, modifier: Modifier = Modifier) {
    val colors = NyffTheme.colors
    val options = listOf(
        Triple(OrderMode.DELIVERY, Icons.Outlined.DeliveryDining, R.string.delivery),
        Triple(OrderMode.TAKEAWAY, Icons.Outlined.ShoppingBag, R.string.pickup),
    )
    Row(
        modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(if (onDark) colors.headerChip else colors.surfaceMuted)
            .padding(4.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { (value, icon, label) ->
            val active = value == mode
            val tint = when {
                active -> colors.onAccent
                onDark -> colors.onHeaderMuted
                else -> colors.textMuted
            }
            Row(
                Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(CircleShape)
                    .background(if (active) colors.accent else Color.Transparent)
                    .selectable(selected = active, role = Role.RadioButton, onClick = { onChange(value) }),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(label), style = MaterialTheme.typography.labelMedium, color = tint)
            }
        }
    }
}

/** Kichik katta harfli bo'lim nomi ("MA'LUMOTLARINGIZ"). */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall,
        color = NyffTheme.colors.textMuted,
    )
}

enum class Tone { INFO, WARNING, DANGER, SUCCESS }

/** Rangli izoh qatori (ogohlantirish, xato, ma'lumot). */
@Composable
fun Notice(
    text: String,
    tone: Tone,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    supporting: String? = null,
    action: (@Composable () -> Unit)? = null,
) {
    val colors = NyffTheme.colors
    val (background, content) = when (tone) {
        Tone.INFO -> colors.surfaceMuted to colors.text
        Tone.WARNING -> colors.warningSoft to colors.warning
        Tone.DANGER -> colors.dangerSoft to colors.danger
        Tone.SUCCESS -> colors.successSoft to colors.success
    }
    Row(
        modifier
            .fillMaxWidth()
            .clip(NyffShapes.card)
            .background(background)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(text, style = MaterialTheme.typography.titleSmall, color = content)
            if (supporting != null) {
                Text(supporting, style = MaterialTheme.typography.bodySmall, color = content.copy(alpha = 0.85f))
            }
        }
        if (action != null) {
            Spacer(Modifier.width(8.dp))
            action()
        }
    }
}

/** Yuklab bo'lmadi + "Qayta urinish". */
@Composable
fun ErrorBlock(error: Throwable?, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val colors = NyffTheme.colors
    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(Icons.Outlined.CloudOff, contentDescription = null, tint = colors.textFaint, modifier = Modifier.size(40.dp))
        Text(stringResource(R.string.load_failed), style = MaterialTheme.typography.titleMedium, color = colors.text)
        Text(
            stringResource(errorRes(error)),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textMuted,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        PillButton(stringResource(R.string.retry), onRetry)
    }
}

/** Kichik sariq tugma ("Qayta urinish", "Xaritada belgilash"). */
@Composable
fun PillButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    val colors = NyffTheme.colors
    Row(
        modifier
            .height(40.dp)
            .clip(CircleShape)
            .background(colors.accent)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = colors.onAccent, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, style = MaterialTheme.typography.labelMedium, color = colors.onAccent)
    }
}

/** Pastdagi katta sariq tugma: matn, o'ngda summa va strelka (yoki yuklanish belgisi). */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: String? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val colors = NyffTheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(NyffShapes.card)
            .background(if (enabled) colors.accent else colors.accent.copy(alpha = 0.45f))
            .clickable(enabled = enabled && !loading, role = Role.Button, onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            color = colors.onAccent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (trailing != null) {
            Spacer(Modifier.width(10.dp))
            Text(
                trailing,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                color = colors.onAccent,
                maxLines = 1,
            )
            Spacer(Modifier.width(6.dp))
        }
        if (loading) {
            CircularProgressIndicator(Modifier.size(20.dp), color = colors.onAccent, strokeWidth = 2.dp)
        } else {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = colors.onAccent)
        }
    }
}

/** Qora to'liq kenglikdagi tugma ("Yangi buyurtma"). */
@Composable
fun DarkButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = NyffTheme.colors
    Box(
        modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(NyffShapes.card)
            .background(colors.bar)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = colors.onBar)
    }
}

/** "NY" belgisi (web `Brand`). */
@Composable
fun BrandBadge(modifier: Modifier = Modifier, size: Dp = 38.dp) {
    val colors = NyffTheme.colors
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .background(colors.accent),
        contentAlignment = Alignment.Center,
    ) {
        Text("NY", color = colors.onAccent, fontWeight = FontWeight.Black, fontSize = 14.sp)
    }
}

/** Ro'yxatdagi holat belgisi (dostavkada — kuryer bosqichi bilan). */
@Composable
fun StatusChip(order: CustomerOrder, modifier: Modifier = Modifier) {
    val colors = NyffTheme.colors
    val (background, content) = when (OrderProgress.headline(order)) {
        OrderHeadline.WAITING -> colors.warningSoft to colors.warning
        OrderHeadline.COOKING, OrderHeadline.ON_THE_WAY -> colors.accent.copy(alpha = 0.22f) to colors.text
        OrderHeadline.READY_DELIVERY, OrderHeadline.READY_PICKUP, OrderHeadline.DELIVERED -> colors.successSoft to colors.success
        OrderHeadline.RETURNED -> colors.warningSoft to colors.warning
        else -> colors.dangerSoft to colors.danger
    }
    Text(
        stringResource(statusRes(order)),
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(background)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        style = MaterialTheme.typography.labelMedium,
        color = content,
    )
}

/**
 * Holat paneli belgilari: qora sarlavhali ekranlarda oq (`darkIcons = false`), qolganlarida mavzuga qarab. Har ekran
 * ochilganda o'zinikini qo'yadi.
 */
@Composable
fun SystemBarIcons(darkIcons: Boolean) {
    val window = LocalActivity.current?.window ?: return
    val lightTheme = !NyffTheme.colors.isDark
    LaunchedEffect(window, darkIcons, lightTheme) {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = darkIcons
            isAppearanceLightNavigationBars = lightTheme
        }
    }
}
