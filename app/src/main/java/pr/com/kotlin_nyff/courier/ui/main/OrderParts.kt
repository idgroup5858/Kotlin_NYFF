package pr.com.kotlin_nyff.courier.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pr.com.kotlin_nyff.R
import pr.com.kotlin_nyff.data.model.OrderItem
import pr.com.kotlin_nyff.ui.common.currentLanguage
import pr.com.kotlin_nyff.ui.theme.NyffShapes
import pr.com.kotlin_nyff.ui.theme.NyffTheme

/**
 * Taomlar yig'iq holda — "4 ta taom ˅", o'ngda pul. Bosilsa ochiladi: har taom soni va izohi bilan, ostida [details]
 * (yetkazish narxi, to'lov turi). Karta ixcham qoladi — tarkib kerak bo'lganda (filialda tekshirish) ko'riladi.
 */
@Composable
fun ItemsToggle(
    key: String,
    items: List<OrderItem>,
    trailing: String,
    modifier: Modifier = Modifier,
    details: List<String> = emptyList(),
) {
    val colors = NyffTheme.colors
    val language = currentLanguage()
    var open by rememberSaveable(key) { mutableStateOf(false) }
    val count = items.sumOf { it.qty }
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(NyffShapes.control)
                .clickable(role = Role.Button) { open = !open }
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.ShoppingBag, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(pluralStringResource(R.plurals.courier_items_count, count, count), style = MaterialTheme.typography.labelMedium, color = colors.text)
            Icon(
                if (open) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                trailing,
                modifier = Modifier.weight(1f),
                // pul — Ultra Bold
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = colors.text,
                textAlign = TextAlign.End,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        AnimatedVisibility(open) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(NyffShapes.control)
                    .background(colors.surfaceMuted)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items.forEach { item ->
                    Row {
                        Text(
                            "${item.qty}×",
                            modifier = Modifier.width(30.dp),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                            color = colors.text,
                        )
                        Column(Modifier.weight(1f)) {
                            Text(item.name.get(language), style = MaterialTheme.typography.bodySmall, color = colors.text)
                            if (item.note.isNotBlank()) {
                                Text(item.note, style = MaterialTheme.typography.bodySmall, color = colors.select)
                            }
                        }
                    }
                }
                details.forEach { line ->
                    Text(line, style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                }
            }
        }
    }
}

/** Manzil — bir-ikki qator, joy belgisi bilan. */
@Composable
fun AddressLine(address: String, maxLines: Int = 1) {
    val colors = NyffTheme.colors
    Row(verticalAlignment = Alignment.Top) {
        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = colors.textFaint, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(
            address,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.text,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
