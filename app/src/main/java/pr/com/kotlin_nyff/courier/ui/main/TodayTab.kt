package pr.com.kotlin_nyff.courier.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pr.com.kotlin_nyff.R
import pr.com.kotlin_nyff.courier.data.CourierOrder
import pr.com.kotlin_nyff.courier.domain.CourierMoney
import pr.com.kotlin_nyff.domain.Format
import pr.com.kotlin_nyff.ui.common.Chip
import pr.com.kotlin_nyff.ui.common.NyffCard
import pr.com.kotlin_nyff.ui.common.money
import pr.com.kotlin_nyff.courier.ui.reasonRes
import pr.com.kotlin_nyff.ui.theme.NyffTheme

/** "Bugun": nechta yetkazildi / qaytdi, mijozlardan olingan naqd va yetkazish narxlari (kuryer haqi hisoblanmaydi). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayTab(state: MainUiState, vm: MainViewModel) {
    val today = CourierMoney.today(state.done)
    PullToRefreshBox(isRefreshing = state.refreshing, onRefresh = vm::refresh, modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "stats") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Stat(stringResource(R.string.courier_today_delivered), today.delivered.toString(), Modifier.weight(1f))
                    Stat(stringResource(R.string.courier_today_returned), today.returned.toString(), Modifier.weight(1f))
                }
            }
            item(key = "money") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Stat(stringResource(R.string.courier_today_cash), money(today.cash), Modifier.weight(1f))
                    Stat(stringResource(R.string.courier_today_fees), money(today.fees), Modifier.weight(1f))
                }
            }
            if (state.done.isEmpty()) {
                item(key = "empty") { Empty(stringResource(R.string.courier_no_done)) }
            }
            items(state.done, key = { it.id }) { order -> DoneRow(order, state.branch(order.branchId)?.name.orEmpty()) }
        }
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier = Modifier) {
    val colors = NyffTheme.colors
    NyffCard(modifier) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
            // raqam / summa — Ultra Bold (rasmiylashtirishdagi "Jami" kabi)
            Text(value, style = MaterialTheme.typography.headlineMedium, color = colors.text, maxLines = 1)
        }
    }
}

@Composable
private fun DoneRow(order: CourierOrder, branchName: String) {
    val colors = NyffTheme.colors
    NyffCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(Format.orderNumber(order.number), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = colors.text)
                Spacer(Modifier.width(10.dp))
                if (order.deliveredAt != null) {
                    Chip(stringResource(R.string.courier_done_delivered, Format.time(order.deliveredAt)), colors.successSoft, colors.success)
                } else {
                    Chip(stringResource(R.string.courier_done_returned, Format.time(order.returnedAt)), colors.warningSoft, colors.warning)
                }
                Spacer(Modifier.weight(1f))
                Text(branchName, style = MaterialTheme.typography.labelMedium, color = colors.select)
            }
            Text(
                order.online?.address.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (order.returnedAt != null) {
                Text(
                    stringResource(reasonRes(order.returnReason)) + if (order.returnNote.isNotBlank()) " — ${order.returnNote}" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.warning,
                )
            }
        }
    }
}
