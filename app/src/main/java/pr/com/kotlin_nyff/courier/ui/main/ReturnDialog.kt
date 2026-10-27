package pr.com.kotlin_nyff.courier.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import pr.com.kotlin_nyff.R
import pr.com.kotlin_nyff.data.model.Branch
import pr.com.kotlin_nyff.courier.data.CourierOrder
import pr.com.kotlin_nyff.courier.data.ReturnReason
import pr.com.kotlin_nyff.domain.Format
import pr.com.kotlin_nyff.ui.common.NyffTextField
import pr.com.kotlin_nyff.ui.common.PillButton
import pr.com.kotlin_nyff.courier.ui.reasonRes
import pr.com.kotlin_nyff.ui.theme.NyffTheme

/** "Yetkazib bo'lmadi": sabab, izoh; buyurtma filialga qaytadi — kuryer filial / admin bilan gaplashadi. */
@Composable
fun ReturnDialog(
    order: CourierOrder,
    branch: Branch?,
    onDismiss: () -> Unit,
    onConfirm: (ReturnReason, String) -> Unit,
) {
    val colors = NyffTheme.colors
    val context = LocalContext.current
    var reason by rememberSaveable { mutableStateOf(ReturnReason.NO_ANSWER) }
    var note by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = { Text("${stringResource(R.string.courier_return_title)} · ${Format.orderNumber(order.number)}", color = colors.text) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.courier_return_hint), style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                Column(Modifier.selectableGroup()) {
                    ReturnReason.entries.forEach { value ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(selected = value == reason, role = Role.RadioButton, onClick = { reason = value }),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = value == reason,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = colors.select),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(reasonRes(value)), style = MaterialTheme.typography.bodyMedium, color = colors.text)
                        }
                    }
                }
                NyffTextField(value = note, onValueChange = { note = it.take(300) }, placeholder = stringResource(R.string.courier_return_note), singleLine = false)
                branch.phoneOrNull()?.let { phone ->
                    PillButton(
                        stringResource(R.string.courier_call_branch),
                        { context.dial(phone) },
                        icon = Icons.Outlined.Call,
                        background = colors.surfaceMuted,
                        content = colors.text,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(reason, note) }) {
                Text(stringResource(R.string.courier_return_send), color = colors.danger)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.courier_cancel), color = colors.textMuted)
            }
        },
    )
}
