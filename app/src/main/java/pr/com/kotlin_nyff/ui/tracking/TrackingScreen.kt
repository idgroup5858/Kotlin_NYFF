package pr.com.kotlin_nyff.ui.tracking

import android.content.Intent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.DeliveryDining
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import pr.com.kotlin_nyff.R
import pr.com.kotlin_nyff.data.SavedOrder
import pr.com.kotlin_nyff.data.model.Branch
import pr.com.kotlin_nyff.data.model.CustomerOrder
import pr.com.kotlin_nyff.data.model.OrderStatus
import pr.com.kotlin_nyff.data.model.OrderType
import pr.com.kotlin_nyff.domain.Format
import pr.com.kotlin_nyff.domain.OrderHeadline
import pr.com.kotlin_nyff.domain.OrderProgress
import pr.com.kotlin_nyff.domain.ServerClock
import pr.com.kotlin_nyff.domain.TrackingStep
import pr.com.kotlin_nyff.ui.common.DarkButton
import pr.com.kotlin_nyff.ui.common.Notice
import pr.com.kotlin_nyff.ui.common.NyffCard
import pr.com.kotlin_nyff.ui.common.PillButton
import pr.com.kotlin_nyff.ui.common.RoundIconButton
import pr.com.kotlin_nyff.ui.common.SystemBarIcons
import pr.com.kotlin_nyff.ui.common.Tone
import pr.com.kotlin_nyff.ui.common.feeText
import pr.com.kotlin_nyff.ui.common.headlineRes
import pr.com.kotlin_nyff.ui.common.money
import pr.com.kotlin_nyff.ui.common.paymentRes
import pr.com.kotlin_nyff.ui.common.text
import pr.com.kotlin_nyff.ui.theme.NyffShapes
import pr.com.kotlin_nyff.ui.theme.NyffTheme
import java.time.Duration

/**
 * Buyurtma holati (web `MobileTracking`): katta raqam, 3 bosqich (qabul kutilmoqda → tayyorlanmoqda → tayyor / kuryerda),
 * filialga o'tkazilgani, bekor qilinish sababi, tarkibi va to'lovi. Holat jonli yangilanadi.
 */
@Composable
fun TrackingScreen(
    onBack: () -> Unit,
    onNewOrder: () -> Unit,
    vm: TrackingViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val colors = NyffTheme.colors

    SystemBarIcons(darkIcons = false)
    LifecycleStartEffect(Unit) {
        vm.start()
        onStopOrDispose { vm.stop() }
    }

    val saved = state.saved
    Column(
        Modifier
            .fillMaxSize()
            .background(colors.page),
    ) {
        if (saved == null) {
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = colors.select)
            }
        } else {
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Header(saved.order, state.branch(saved.order.branchId), onBack)
                val inset = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                if (state.notFound) {
                    Notice(stringResource(R.string.order_not_found), Tone.DANGER, inset, Icons.Outlined.ErrorOutline)
                } else if (state.offline && !state.live) {
                    Notice(stringResource(R.string.offline_hint), Tone.WARNING, inset, Icons.Outlined.CloudOff)
                }
                val order = saved.order
                if (order.transferCount > 0 && order.status != OrderStatus.CANCELLED) {
                    // dostavkada qaysi filial olgani ko'rsatilmaydi — faqat raqam yangilangani
                    if (order.type == OrderType.DELIVERY) {
                        Notice(stringResource(R.string.transferred_hint), Tone.WARNING, inset, Icons.Outlined.SwapHoriz)
                    } else {
                        Notice(
                            stringResource(R.string.transferred, state.branch(order.branchId)?.name.orEmpty()),
                            Tone.WARNING,
                            inset,
                            Icons.Outlined.SwapHoriz,
                            supporting = stringResource(R.string.transferred_hint),
                        )
                    }
                }
                if (order.status == OrderStatus.CANCELLED) {
                    CancelledCard(order, state.branch(order.branchId), inset)
                } else {
                    if (order.returnedAt != null) ReturnedCard(state.branch(order.branchId), inset)
                    Steps(order, inset)
                }
                Details(saved, inset)
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .navigationBarsPadding()
                .padding(12.dp),
        ) {
            DarkButton(stringResource(R.string.new_order), onNewOrder)
        }
    }
}

@Composable
private fun Header(order: CustomerOrder, branch: Branch?, onBack: () -> Unit) {
    val colors = NyffTheme.colors
    Box(
        Modifier
            .fillMaxWidth()
            .clip(NyffShapes.header)
            .background(colors.header)
            .statusBarsPadding(),
    ) {
        RoundIconButton(
            Icons.AutoMirrored.Filled.ArrowBack,
            stringResource(R.string.back),
            onBack,
            colors.headerChip,
            colors.onHeader,
            modifier = Modifier.padding(12.dp),
            size = 40.dp,
            iconSize = 20.dp,
        )
        Column(
            Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 18.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                stringResource(R.string.order_no).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = colors.onHeaderMuted,
            )
            Text(Format.orderNumber(order.number), style = MaterialTheme.typography.displayMedium, color = colors.accent)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = colors.accent, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                val delivery = order.type == OrderType.DELIVERY
                val type = stringResource(if (delivery) R.string.delivery else R.string.pickup)
                // dostavkada filial nomi ko'rsatilmaydi; olib ketishda — mijoz o'zi tanlagan filial
                Text(
                    listOfNotNull(branch?.name?.takeUnless { delivery }, type).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onHeaderMuted,
                )
            }
        }
    }
}

private data class Step(val icon: ImageVector, val title: Int, val hint: Int, val at: String?)

/** Bosqich ko'rinishi. Dostavka tayyor, kuryer hali yo'lga chiqmagan — 2-bosqich "Tayyor · kuryer olib ketadi". */
private fun stepOf(kind: TrackingStep, order: CustomerOrder): Step = when (kind) {
    TrackingStep.PLACED -> Step(Icons.Outlined.Schedule, R.string.step_placed, R.string.step_placed_hint, order.createdAt)
    TrackingStep.COOKING ->
        if (order.type == OrderType.DELIVERY && order.status == OrderStatus.READY) {
            Step(Icons.Outlined.Inventory2, R.string.step_ready, R.string.step_ready_hint, order.readyAt)
        } else {
            Step(Icons.Outlined.Restaurant, R.string.step_cooking, R.string.step_cooking_hint, order.preparingAt)
        }
    TrackingStep.READY_PICKUP ->
        Step(Icons.Outlined.Celebration, R.string.step_ready_pickup, R.string.step_ready_pickup_hint, order.readyAt)
    TrackingStep.ON_THE_WAY ->
        Step(Icons.Outlined.DeliveryDining, R.string.step_on_the_way, R.string.step_on_the_way_hint, order.departedAt)
    TrackingStep.DELIVERED ->
        Step(Icons.Outlined.Home, R.string.step_delivered, R.string.step_delivered_hint, order.deliveredAt)
}

@Composable
private fun Steps(order: CustomerOrder, modifier: Modifier) {
    val colors = NyffTheme.colors
    val reached = OrderProgress.step(order)
    val completed = OrderProgress.completed(order)
    val steps = OrderProgress.steps(order.type).map { stepOf(it, order) }
    NyffCard(modifier) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            steps.forEachIndexed { index, step ->
                val done = index < reached || (completed && index == reached)
                val current = index == reached && !done
                Row(Modifier.height(IntrinsicSize.Min)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        StepIcon(if (done) Icons.Filled.Check else step.icon, done, current)
                        if (index < steps.lastIndex) {
                            Box(
                                Modifier
                                    .width(2.dp)
                                    .weight(1f)
                                    .background(if (index < reached) colors.accent else colors.line),
                            )
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(top = 6.dp, bottom = if (index < steps.lastIndex) 14.dp else 0.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(step.title),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleSmall,
                                color = if (done || current) colors.text else colors.textFaint,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (step.at != null && (done || current)) {
                                // vaqt sarlavha ustiga chiqmaydi — orada bo'shliq
                                Spacer(Modifier.width(8.dp))
                                Text(Format.time(step.at), style = MaterialTheme.typography.bodySmall, color = colors.textFaint)
                            }
                        }
                        Text(stringResource(step.hint), style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                        if (index == 0 && current) AcceptCountdown(order)
                    }
                }
            }
        }
    }
}

@Composable
private fun StepIcon(icon: ImageVector, done: Boolean, current: Boolean) {
    val colors = NyffTheme.colors
    val pulse = rememberInfiniteTransition(label = "step")
    val alpha by pulse.animateFloat(1f, 0.35f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "alpha")
    Box(
        Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(
                when {
                    done -> colors.accent
                    current -> colors.header
                    else -> colors.surfaceMuted
                },
            )
            .then(if (current) Modifier.border(2.dp, colors.accent.copy(alpha = 0.45f), CircleShape) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = when {
                done -> colors.onAccent
                current -> colors.accent
                else -> colors.textFaint
            },
            modifier = Modifier
                .size(15.dp)
                .alpha(if (current) alpha else 1f),
        )
    }
}

/** "Filial javobini kutyapmiz · 3:12" — qabul muddati (`routedAt` + 5 daqiqa) tugaguncha. */
@Composable
private fun AcceptCountdown(order: CustomerOrder) {
    val deadline = OrderProgress.acceptDeadline(order) ?: return
    val now by produceState(ServerClock.now(), deadline) {
        while (true) {
            value = ServerClock.now()
            delay(1_000)
        }
    }
    val left = Duration.between(now, deadline).seconds.coerceAtMost(OrderProgress.ACCEPT_WINDOW.seconds)
    Text(
        if (left > 0) stringResource(R.string.waiting_branch, Format.countdown(left)) else stringResource(R.string.waiting_branch_long),
        modifier = Modifier.padding(top = 6.dp),
        style = MaterialTheme.typography.labelMedium,
        color = NyffTheme.colors.select,
    )
}

@Composable
private fun CancelledCard(order: CustomerOrder, branch: Branch?, modifier: Modifier) {
    val headline = OrderProgress.headline(order)
    Notice(
        stringResource(headlineRes(headline)),
        Tone.DANGER,
        modifier,
        Icons.Outlined.ErrorOutline,
        supporting = if (headline == OrderHeadline.CANCELLED) stringResource(R.string.cancelled_hint) else null,
    )
    if (headline == OrderHeadline.CANCELLED) CallBranchButton(branch, modifier)
}

/** Kuryer yetkaza olmadi — buyurtma filialga qaytdi, filial mijoz bilan bog'lanadi (mijoz o'zi ham qo'ng'iroq qila oladi). */
@Composable
private fun ReturnedCard(branch: Branch?, modifier: Modifier) {
    Notice(
        stringResource(R.string.returned),
        Tone.WARNING,
        modifier,
        Icons.Outlined.ErrorOutline,
        supporting = stringResource(R.string.returned_hint),
    )
    CallBranchButton(branch, modifier)
}

@Composable
private fun CallBranchButton(branch: Branch?, modifier: Modifier) {
    val context = LocalContext.current
    val phone = branch?.phone?.filter { it.isDigit() || it == '+' }.orEmpty()
    if (phone.isEmpty()) return
    Box(modifier, contentAlignment = Alignment.CenterStart) {
        PillButton(
            stringResource(R.string.call_branch),
            { context.startActivity(Intent(Intent.ACTION_DIAL, "tel:$phone".toUri())) },
            icon = Icons.Outlined.Call,
        )
    }
}

@Composable
private fun Details(saved: SavedOrder, modifier: Modifier) {
    val colors = NyffTheme.colors
    val order = saved.order
    NyffCard(modifier) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            order.items.forEach { item ->
                Row {
                    Text(
                        "${item.qty}× ${item.name.text()}" + if (item.note.isNotBlank()) " (${item.note})" else "",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted,
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(money(item.price * item.qty), style = MaterialTheme.typography.bodySmall, color = colors.text)
                }
            }
            if (order.type == OrderType.DELIVERY) {
                Row {
                    Text(
                        "${stringResource(R.string.delivery_fee)} · ${stringResource(R.string.fee_separate)}",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted,
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(feeText(order.deliveryFee ?: saved.deliveryFee), style = MaterialTheme.typography.bodySmall, color = colors.textMuted, textAlign = TextAlign.End)
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 4.dp), color = colors.line)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    paymentRes(order)?.let { stringResource(it) }.orEmpty(),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted,
                )
                // jami — qalin, lekin ixcham
                Text(money(order.total), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = colors.text)
            }
            val address = order.online?.address.orEmpty()
            if (address.isNotBlank()) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = colors.textFaint, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(address, style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                }
            }
            if (order.note.isNotBlank()) {
                Text("${stringResource(R.string.note)}: ${order.note}", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
            }
            Text(Format.dateTime(order.createdAt), style = MaterialTheme.typography.bodySmall, color = colors.textFaint)
        }
    }
}
