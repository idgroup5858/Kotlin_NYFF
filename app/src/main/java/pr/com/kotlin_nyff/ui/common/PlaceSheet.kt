package pr.com.kotlin_nyff.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.DeliveryDining
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import pr.com.kotlin_nyff.R
import pr.com.kotlin_nyff.domain.OrderMode
import pr.com.kotlin_nyff.ui.theme.NyffShapes
import pr.com.kotlin_nyff.ui.theme.NyffTheme

/**
 * Bosh sahifadagi joy tanlovi (sarlavhadagi manzil / filial tugmasidan): yetkazib berish yoki olib ketish. Tanlangan usul
 * belgilangan; usul rasmiylashtirishda ham almashtiriladi. Qaysi oyna ochilishi (xarita / filiallar) — chaqiruvchida.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceSheet(
    mode: OrderMode,
    onDelivery: () -> Unit,
    onPickup: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = NyffTheme.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.page,
        shape = NyffShapes.sheet,
    ) {
        Column(
            Modifier
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(stringResource(R.string.place_title), style = MaterialTheme.typography.titleLarge, color = colors.text)
            Spacer(Modifier.height(2.dp))
            PlaceOption(
                icon = Icons.Outlined.DeliveryDining,
                title = stringResource(R.string.delivery),
                hint = stringResource(R.string.place_delivery_hint),
                selected = mode == OrderMode.DELIVERY,
                onClick = onDelivery,
            )
            PlaceOption(
                icon = Icons.Outlined.ShoppingBag,
                title = stringResource(R.string.pickup),
                hint = stringResource(R.string.place_pickup_hint),
                selected = mode == OrderMode.TAKEAWAY,
                onClick = onPickup,
            )
        }
    }
}

@Composable
private fun PlaceOption(icon: ImageVector, title: String, hint: String, selected: Boolean, onClick: () -> Unit) {
    val colors = NyffTheme.colors
    NyffCard(Modifier.fillMaxWidth(), highlight = selected) {
        Row(
            Modifier
                .clickable(role = Role.RadioButton, onClick = onClick)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.accent),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = colors.onAccent, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = colors.text)
                Text(hint, style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
            }
            if (selected) {
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = colors.select)
            }
        }
    }
}
