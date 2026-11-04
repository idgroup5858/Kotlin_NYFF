package pr.com.kotlin_nyff.courier.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pr.com.kotlin_nyff.R
import pr.com.kotlin_nyff.courier.data.CourierOrder
import pr.com.kotlin_nyff.domain.Format
import pr.com.kotlin_nyff.ui.theme.NyffTheme

/** "Yetkazildi" tasdig'i — qo'l tegib ketsa buyurtma yopilib qolmasin; mijozdan olinadigan pul yana bir bor ko'rsatiladi. */
@Composable
fun DeliverDialog(order: CourierOrder, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val colors = NyffTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = { Text("${stringResource(R.string.courier_deliver_title)} · ${Format.orderNumber(order.number)}", color = colors.text) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.courier_deliver_hint), style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                Text(
                    collectText(order),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = colors.text,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.courier_deliver_confirm), color = colors.select, fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.courier_cancel), color = colors.textMuted)
            }
        },
    )
}
