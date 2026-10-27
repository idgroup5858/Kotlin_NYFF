package pr.com.kotlin_nyff.ui.orders

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pr.com.kotlin_nyff.R
import pr.com.kotlin_nyff.data.SavedOrder
import pr.com.kotlin_nyff.data.model.OrderType
import pr.com.kotlin_nyff.domain.Format
import pr.com.kotlin_nyff.ui.common.NyffCard
import pr.com.kotlin_nyff.ui.common.RoundIconButton
import pr.com.kotlin_nyff.ui.common.StatusChip
import pr.com.kotlin_nyff.ui.common.SystemBarIcons
import pr.com.kotlin_nyff.ui.common.currentLanguage
import pr.com.kotlin_nyff.ui.common.money
import pr.com.kotlin_nyff.ui.theme.NyffTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(onBack: () -> Unit, onOpenOrder: (String) -> Unit, vm: OrdersViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val colors = NyffTheme.colors
    SystemBarIcons(darkIcons = !colors.isDark)

    Column(
        Modifier
            .fillMaxSize()
            .background(colors.page),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .statusBarsPadding()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoundIconButton(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), onBack, Color.Transparent, colors.text, size = 44.dp, iconSize = 22.dp)
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.my_orders), style = MaterialTheme.typography.titleLarge, color = colors.text)
        }
        PullToRefreshBox(
            isRefreshing = state.refreshing,
            onRefresh = vm::refresh,
            modifier = Modifier.weight(1f),
        ) {
            val orders = state.orders
            when {
                orders == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.select)
                }
                orders.isEmpty() -> Column(
                    Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(Icons.AutoMirrored.Outlined.ReceiptLong, contentDescription = null, tint = colors.textFaint, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.no_orders), style = MaterialTheme.typography.titleMedium, color = colors.textMuted)
                }
                else -> LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .navigationBarsPadding(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(orders, key = { it.order.id }) { saved ->
                        OrderRow(saved, state.branchName(saved.order.branchId), onClick = { onOpenOrder(saved.order.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderRow(saved: SavedOrder, branchName: String?, onClick: () -> Unit) {
    val colors = NyffTheme.colors
    val order = saved.order
    NyffCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    Format.orderNumber(order.number),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = colors.text,
                )
                Spacer(Modifier.width(10.dp))
                StatusChip(order)
                Spacer(Modifier.weight(1f))
                Text(money(order.total), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold), color = colors.text)
            }
            val type = stringResource(if (order.type == OrderType.DELIVERY) R.string.delivery else R.string.pickup)
            Text(
                listOfNotNull(Format.dateTime(order.createdAt), branchName, type).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted,
            )
            val language = currentLanguage()
            Text(
                order.items.joinToString(", ") { "${it.qty}× ${it.name.get(language)}" },
                style = MaterialTheme.typography.bodySmall,
                color = colors.textFaint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
