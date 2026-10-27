package pr.com.kotlin_nyff.courier.ui.main

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pr.com.kotlin_nyff.R
import pr.com.kotlin_nyff.data.model.Branch
import pr.com.kotlin_nyff.courier.data.CourierOrder
import pr.com.kotlin_nyff.data.model.OnlinePayment
import pr.com.kotlin_nyff.data.model.OrderStatus
import pr.com.kotlin_nyff.courier.domain.CourierMoney
import pr.com.kotlin_nyff.domain.Format
import pr.com.kotlin_nyff.courier.domain.MapLinks
import pr.com.kotlin_nyff.ui.common.NyffCard
import pr.com.kotlin_nyff.ui.common.PillButton
import pr.com.kotlin_nyff.ui.common.PrimaryButton
import pr.com.kotlin_nyff.ui.common.RoundIconButton
import pr.com.kotlin_nyff.courier.ui.courierFeeText
import pr.com.kotlin_nyff.ui.common.money
import pr.com.kotlin_nyff.ui.theme.NyffShapes
import pr.com.kotlin_nyff.ui.theme.NyffTheme

/** "Meniki": filialda — olib ketish va kassaga to'lash; yo'lda — xarita va server taklif qilgan tartib. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MineTab(state: MainUiState, vm: MainViewModel) {
    var returning by rememberSaveable { mutableStateOf<String?>(null) }
    PullToRefreshBox(isRefreshing = state.refreshing, onRefresh = vm::refresh, modifier = Modifier.fillMaxSize()) {
        when {
            state.active.isEmpty() -> LazyColumn(Modifier.fillMaxSize()) {
                item { Empty(stringResource(R.string.courier_no_mine)) }
            }
            state.departed -> OnTheWay(state, vm, onReturn = { returning = it })
            else -> AtBranch(state, vm)
        }
    }
    val order = state.active.firstOrNull { it.id == returning }
    if (order != null) {
        ReturnDialog(
            order = order,
            branch = state.branch(order.branchId),
            onDismiss = { returning = null },
            onConfirm = { reason, note ->
                returning = null
                vm.returnOrder(order.id, reason, note)
            },
        )
    }
}

/** Filialda: hamma buyurtma tayyor bo'lgach — "Yo'lga chiqdim". Naqd buyurtmalar puli kassaga kuryerning o'zidan. */
@Composable
private fun AtBranch(state: MainUiState, vm: MainViewModel) {
    val colors = NyffTheme.colors
    val context = LocalContext.current
    val branch = state.branch(state.active.first().branchId)
    val allReady = state.active.all { it.status == OrderStatus.READY }
    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item(key = "branch") {
                NyffCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Storefront, contentDescription = null, tint = colors.select, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(stringResource(R.string.courier_at_branch), style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                                Text(branch?.name.orEmpty(), style = MaterialTheme.typography.titleMedium, color = colors.text, maxLines = 1)
                            }
                            branch.phoneOrNull()?.let { phone ->
                                RoundIconButton(
                                    Icons.Outlined.Call,
                                    stringResource(R.string.courier_call_branch),
                                    { context.dial(phone) },
                                    colors.surfaceMuted,
                                    colors.text,
                                    size = 34.dp,
                                )
                            }
                        }
                        val kassa = CourierMoney.kassa(state.active)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Payments, contentDescription = null, tint = colors.text, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (kassa > 0) stringResource(R.string.courier_pay_kassa, money(kassa)) else stringResource(R.string.courier_pay_kassa_none),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = if (kassa > 0) FontWeight.ExtraBold else FontWeight.Medium),
                                color = colors.text,
                            )
                        }
                    }
                }
            }
            items(state.active, key = { it.id }) { order ->
                NyffCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(Format.orderNumber(order.number), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = colors.text)
                            Spacer(Modifier.width(8.dp))
                            StatusChip(order.status)
                            Spacer(Modifier.weight(1f))
                            // TextButton'ning 48dp balandligisiz — karta ixcham qoladi
                            val canRelease = state.busy == null
                            Text(
                                stringResource(R.string.courier_release),
                                modifier = Modifier
                                    .clip(NyffShapes.control)
                                    .clickable(enabled = canRelease, role = Role.Button) { vm.release(order.id) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                color = if (canRelease) colors.danger else colors.danger.copy(alpha = 0.4f),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                        Column(Modifier.padding(end = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            CustomerLines(order)
                        }
                    }
                }
            }
        }
        Column(
            Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .navigationBarsPadding()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (!allReady) {
                Text(stringResource(R.string.courier_wait_ready), style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
            }
            PrimaryButton(
                text = stringResource(R.string.courier_depart),
                onClick = vm::depart,
                enabled = allReady,
                loading = state.busy == MainViewModel.DEPART,
            )
        }
    }
}

/** Yo'lda: xarita, "Butun yo'l" havolalari va manzillar server taklif qilgan tartibda (kuryer o'zgartirmaydi). */
@Composable
private fun OnTheWay(state: MainUiState, vm: MainViewModel, onReturn: (String) -> Unit) {
    val colors = NyffTheme.colors
    val context = LocalContext.current
    val stops = state.active
    val branch = state.branch(stops.first().branchId)
    // yo'l boshi — shu safardagi oxirgi yetkazilgan / qaytarilgan manzil, bo'lmasa filial
    val batch = stops.first().departedAt
    val lastDone = state.done.filter { it.departedAt == batch }.maxByOrNull { it.courierSeq ?: 0 }
    val start = lastDone?.location ?: branch?.location
    val points = stops.mapNotNull { it.location }

    // ko'k nuqta (kuryer joyi) uchun — ixtiyoriy ruxsat
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }
    LaunchedEffect(Unit) {
        if (!hasLocationPermission(context)) {
            permission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "map") {
            RouteMap(
                start = start,
                startIsBranch = lastDone == null,
                stops = stops,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(MaterialTheme.shapes.medium),
            )
        }
        item(key = "whole") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.Map, contentDescription = null, tint = colors.select, modifier = Modifier.size(20.dp))
                    Text(stringResource(R.string.courier_whole_route), style = MaterialTheme.typography.titleSmall, color = colors.text, modifier = Modifier.weight(1f))
                    MapLinks.googleRoute(points)?.let { url ->
                        PillButton(stringResource(R.string.courier_google), { context.openLink(url) }, icon = Icons.Outlined.Navigation)
                    }
                    MapLinks.yandexRoute(points)?.let { url ->
                        PillButton(stringResource(R.string.courier_yandex), { context.openLink(url) }, background = colors.surfaceMuted, content = colors.text)
                    }
                }
                Text(stringResource(R.string.courier_route_hint), style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
            }
        }
        itemsIndexed(stops, key = { _, order -> order.id }) { index, order ->
            StopCard(
                order = order,
                next = index == 0,
                busy = state.busy == order.id,
                enabled = state.busy == null,
                onDelivered = { vm.deliver(order.id) },
                onReturn = { onReturn(order.id) },
            )
        }
    }
}

@Composable
private fun StopCard(
    order: CourierOrder,
    next: Boolean,
    busy: Boolean,
    enabled: Boolean,
    onDelivered: () -> Unit,
    onReturn: () -> Unit,
) {
    val colors = NyffTheme.colors
    val context = LocalContext.current
    NyffCard(Modifier.fillMaxWidth(), highlight = next) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (next) colors.accent else colors.surfaceMuted),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        (order.courierSeq ?: 0).toString(),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                        color = if (next) colors.onAccent else colors.text,
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(Format.orderNumber(order.number), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = colors.text)
                    val leg = order.courierLegKm
                    if (next || leg != null) {
                        Text(
                            listOfNotNull(
                                if (next) stringResource(R.string.courier_next_stop) else null,
                                leg?.let { stringResource(R.string.courier_distance_km, Format.km(it)) },
                            ).joinToString(" · "),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (next) colors.select else colors.textMuted,
                        )
                    }
                }
                order.online?.phone?.takeIf { it.isNotBlank() }?.let { phone ->
                    RoundIconButton(
                        Icons.Outlined.Call,
                        stringResource(R.string.courier_call),
                        { context.dial(phone) },
                        colors.surfaceMuted,
                        colors.text,
                        size = 34.dp,
                    )
                }
            }
            CustomerLines(order)
            order.location?.let { point ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PillButton(stringResource(R.string.courier_google), { context.openLink(MapLinks.google(point)) }, icon = Icons.Outlined.Navigation)
                    PillButton(stringResource(R.string.courier_yandex), { context.openLink(MapLinks.yandex(point)) }, background = colors.surfaceMuted, content = colors.text)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PillButton(
                    stringResource(R.string.courier_not_delivered),
                    onReturn,
                    background = colors.dangerSoft,
                    content = colors.danger,
                    enabled = enabled,
                )
                PillButton(
                    stringResource(R.string.courier_delivered),
                    onDelivered,
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.CheckCircle,
                    enabled = enabled,
                    loading = busy,
                )
            }
        }
    }
}

/** Mijoz, manzil, izoh, taomlar (bosilsa ochiladi) va mijozdan olinadigan pul — filialda ham, yo'lda ham. */
@Composable
private fun CustomerLines(order: CourierOrder) {
    val colors = NyffTheme.colors
    val online = order.online
    if (online != null && online.name.isNotBlank()) {
        Text(
            "${online.name} · ${online.phone}",
            style = MaterialTheme.typography.labelMedium,
            color = colors.textMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
    AddressLine(online?.address.orEmpty(), maxLines = 2)
    if (order.note.isNotBlank()) {
        Text(
            stringResource(R.string.courier_note, order.note),
            style = MaterialTheme.typography.bodySmall,
            color = colors.select,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
    val collect = money(CourierMoney.collect(order))
    ItemsToggle(
        key = order.id,
        items = order.items,
        trailing = when {
            order.payment == OnlinePayment.CLICK && order.deliveryFee == null -> stringResource(R.string.courier_pay_click)
            order.deliveryFee == null -> stringResource(R.string.courier_collect_and_fee, collect)
            else -> stringResource(R.string.courier_collect, collect)
        },
        details = listOf(
            if (order.payment == OnlinePayment.CLICK) stringResource(R.string.courier_pay_click)
            else stringResource(R.string.courier_pay_cash_amount, money(order.total)),
            courierFeeText(order.deliveryFee),
        ),
    )
}

/** Filial (ro'yxatda bor bo'lsa) — kuryer qaytarish dialogida qo'ng'iroq qiladi. */
internal fun Branch?.phoneOrNull(): String? = this?.phone?.takeIf { it.isNotBlank() }
