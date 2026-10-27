package pr.com.kotlin_nyff.courier.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeliveryDining
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pr.com.kotlin_nyff.R
import pr.com.kotlin_nyff.courier.data.AvailableOrder
import pr.com.kotlin_nyff.courier.data.CourierBlock
import pr.com.kotlin_nyff.data.model.OnlinePayment
import pr.com.kotlin_nyff.data.model.OrderStatus
import pr.com.kotlin_nyff.domain.Format
import pr.com.kotlin_nyff.ui.common.Chip
import pr.com.kotlin_nyff.ui.common.Notice
import pr.com.kotlin_nyff.ui.common.NyffCard
import pr.com.kotlin_nyff.ui.common.PillButton
import pr.com.kotlin_nyff.ui.common.Tone
import pr.com.kotlin_nyff.courier.ui.courierFeeText
import pr.com.kotlin_nyff.ui.common.money
import pr.com.kotlin_nyff.ui.theme.NyffTheme

/**
 * "Yangi": kuryer avval filialni tanlaydi (tepadagi tugmalar, har birida buyurtmalar soni), keyin o'sha filialning hali
 * hech kim olmagan dostavkalari — tayyorlanayotganlari ham, tayyorlari ham (eskisi birinchi). Qo'lda buyurtma bo'lsa —
 * faqat o'sha filial.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailableTab(state: MainUiState, vm: MainViewModel) {
    Column(Modifier.fillMaxSize()) {
        BranchPicker(state, vm::chooseBranch)
        PullToRefreshBox(
            isRefreshing = state.refreshing,
            onRefresh = vm::refresh,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (!state.onDuty) {
                    item(key = "off-duty") {
                        Notice(stringResource(R.string.courier_off_duty_banner), Tone.WARNING, icon = Icons.Outlined.Info)
                    }
                }
                when {
                    state.pickBranchId == null -> item(key = "choose") { Empty(stringResource(R.string.courier_choose_branch_hint)) }
                    state.shownAvailable.isEmpty() -> item(key = "empty") { Empty(stringResource(R.string.courier_no_available)) }
                }
                items(state.shownAvailable, key = { it.id }) { order ->
                    AvailableCard(order = order, busy = state.busy == order.id, onTake = { vm.take(order.id) })
                }
            }
        }
    }
}

/** Filial tugmalari — header'dagi tab uslubida: tanlangani sariq, yonida olsa bo'ladigan buyurtmalar soni. */
@Composable
private fun BranchPicker(state: MainUiState, onPick: (String) -> Unit) {
    val colors = NyffTheme.colors
    val selected = state.pickBranchId
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (selected == null) {
            Text(
                stringResource(R.string.courier_choose_branch),
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.titleSmall,
                color = colors.text,
            )
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.pickBranches, key = { it.id }) { branch ->
                val active = branch.id == selected
                BranchChip(
                    name = branch.name,
                    count = state.availableCount(branch.id),
                    active = active,
                    enabled = active || !state.branchLocked,
                    onClick = { onPick(branch.id) },
                )
            }
        }
        if (state.branchLocked) {
            Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    stringResource(R.string.courier_branch_locked, state.branch(selected)?.name.orEmpty()),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted,
                )
            }
        }
    }
}

@Composable
private fun BranchChip(name: String, count: Int, active: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val colors = NyffTheme.colors
    Row(
        Modifier
            .height(32.dp)
            .alpha(if (enabled) 1f else 0.4f)
            .then(if (active) Modifier else Modifier.border(1.dp, colors.line, CircleShape))
            .clip(CircleShape)
            .background(if (active) colors.accent else colors.surface)
            .selectable(selected = active, enabled = enabled, role = Role.Tab, onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.Storefront, contentDescription = null, tint = if (active) colors.onAccent else colors.select, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(5.dp))
        Text(name, style = MaterialTheme.typography.labelMedium, color = if (active) colors.onAccent else colors.text, maxLines = 1)
        if (count > 0) {
            Spacer(Modifier.width(5.dp))
            Box(
                Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(if (active) colors.onAccent else colors.accent),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (active) colors.accent else colors.onAccent,
                )
            }
        }
    }
}

/** Ixcham karta: raqam, holat, masofa; manzil; taomlar (bosilsa ochiladi) va pul; yetkazish narxi va "Olaman". */
@Composable
private fun AvailableCard(order: AvailableOrder, busy: Boolean, onTake: () -> Unit) {
    val colors = NyffTheme.colors
    NyffCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    Format.orderNumber(order.number),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = colors.text,
                )
                Spacer(Modifier.width(8.dp))
                StatusChip(order.status)
                Spacer(Modifier.weight(1f))
                order.distanceKm?.let {
                    Text(stringResource(R.string.courier_distance_km, Format.km(it)), style = MaterialTheme.typography.labelMedium, color = colors.textMuted)
                }
            }
            AddressLine(order.address)
            ItemsToggle(
                key = order.id,
                items = order.items,
                trailing = if (order.payment == OnlinePayment.CLICK) stringResource(R.string.courier_pay_click)
                else stringResource(R.string.courier_pay_cash_amount, money(order.total)),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(courierFeeText(order.deliveryFee), style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                    order.extraKm?.let {
                        Text(stringResource(R.string.courier_extra_km, Format.km(it)), style = MaterialTheme.typography.labelMedium, color = colors.success)
                    }
                }
                Spacer(Modifier.width(8.dp))
                when (order.blocked) {
                    null -> PillButton(stringResource(R.string.courier_take), onTake, icon = Icons.Outlined.DeliveryDining, loading = busy)
                    // "Ishdaman" o'chiq — sababi tepadagi ogohlantirishda, kartada takrorlanmaydi
                    CourierBlock.OFF_DUTY -> PillButton(stringResource(R.string.courier_take), onTake, icon = Icons.Outlined.DeliveryDining, enabled = false)
                    else -> Text(
                        stringResource(if (order.blocked == CourierBlock.OTHER_BRANCH) R.string.courier_blocked_other_branch else R.string.courier_blocked_busy),
                        modifier = Modifier.widthIn(max = 180.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.warning,
                        textAlign = TextAlign.End,
                    )
                }
            }
        }
    }
}

/** "Tayyorlanmoqda" / "Tayyor". */
@Composable
fun StatusChip(status: OrderStatus) {
    val colors = NyffTheme.colors
    if (status == OrderStatus.READY) {
        Chip(stringResource(R.string.courier_status_ready), colors.successSoft, colors.success)
    } else {
        Chip(stringResource(R.string.courier_status_preparing), colors.accent.copy(alpha = 0.22f), colors.text)
    }
}

@Composable
fun Empty(text: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = NyffTheme.colors.textMuted, textAlign = TextAlign.Center)
    }
}
