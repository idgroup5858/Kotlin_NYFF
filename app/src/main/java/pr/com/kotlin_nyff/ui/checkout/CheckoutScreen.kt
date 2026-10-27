package pr.com.kotlin_nyff.ui.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pr.com.kotlin_nyff.R
import pr.com.kotlin_nyff.data.model.OnlinePayment
import pr.com.kotlin_nyff.domain.OrderMode
import pr.com.kotlin_nyff.domain.Phone
import pr.com.kotlin_nyff.domain.PricedLine
import pr.com.kotlin_nyff.ui.common.BranchSheet
import pr.com.kotlin_nyff.ui.common.FoodImage
import pr.com.kotlin_nyff.ui.common.Notice
import pr.com.kotlin_nyff.ui.common.NyffCard
import pr.com.kotlin_nyff.ui.common.NyffTextField
import pr.com.kotlin_nyff.ui.common.OrderModeSwitch
import pr.com.kotlin_nyff.ui.common.PhoneVisualTransformation
import pr.com.kotlin_nyff.ui.common.PillButton
import pr.com.kotlin_nyff.ui.common.PrimaryButton
import pr.com.kotlin_nyff.ui.common.QtyStepper
import pr.com.kotlin_nyff.ui.common.RoundIconButton
import pr.com.kotlin_nyff.ui.common.SectionLabel
import pr.com.kotlin_nyff.ui.common.SystemBarIcons
import pr.com.kotlin_nyff.ui.common.Tone
import pr.com.kotlin_nyff.ui.common.branchStateText
import pr.com.kotlin_nyff.ui.common.feeText
import pr.com.kotlin_nyff.ui.common.money
import pr.com.kotlin_nyff.ui.common.text
import pr.com.kotlin_nyff.ui.theme.NyffShapes
import pr.com.kotlin_nyff.ui.theme.NyffTheme

@Composable
fun CheckoutScreen(
    onBack: () -> Unit,
    onOpenLocation: () -> Unit,
    onPlaced: (String) -> Unit,
    vm: CheckoutViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val colors = NyffTheme.colors
    var branchSheet by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.placedOrders.collect(onPlaced) }
    SystemBarIcons(darkIcons = !colors.isDark)

    Column(
        Modifier
            .fillMaxSize()
            .background(colors.page)
            .imePadding(),
    ) {
        // sarlavha
        Row(
            Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoundIconButton(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), onBack, Color.Transparent, colors.text, size = 44.dp, iconSize = 22.dp)
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.checkout), style = MaterialTheme.typography.titleLarge, color = colors.text)
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OrderModeSwitch(state.mode, vm::setMode, onDark = false)
            PlaceCard(state, onOpenLocation = onOpenLocation, onOpenBranches = { branchSheet = true })
            CartCard(state, onQty = vm::changeQty, onRemove = vm::remove)
            ContactSection(state, vm)
            PaymentSection(state.mode, state.contact.payment, vm::setPayment)
            Summary(state)
        }

        // pastki panel: xato + "Buyurtma berish"
        Column(
            Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .navigationBarsPadding()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.error?.let {
                Text(
                    stringResource(it),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.danger,
                    textAlign = TextAlign.Center,
                )
            }
            PrimaryButton(
                text = stringResource(R.string.place_order),
                trailing = money(state.itemsTotal),
                onClick = vm::place,
                enabled = state.availableLines.isNotEmpty() && state.placeReady,
                loading = state.placing,
            )
        }
    }

    if (branchSheet) {
        BranchSheet(
            branches = state.branches,
            selectedId = state.takeawayBranch?.id,
            onSelect = {
                vm.selectTakeawayBranch(it.id)
                branchSheet = false
            },
            onDismiss = { branchSheet = false },
        )
    }
}

/**
 * Dostavka — belgilangan joyni oladigan filial va yetkazish narxi; olib ketish — tanlangan filial. Ixcham: bir qator
 * nom + bir qator izoh, butun qator bosiladi ("O'zgartirish").
 */
@Composable
private fun PlaceCard(state: CheckoutUiState, onOpenLocation: () -> Unit, onOpenBranches: () -> Unit) {
    val colors = NyffTheme.colors
    val delivery = state.mode == OrderMode.DELIVERY
    NyffCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClick = if (delivery) onOpenLocation else onOpenBranches)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                if (delivery) Icons.Filled.LocationOn else Icons.Outlined.Storefront,
                contentDescription = null,
                tint = colors.select,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                val route = state.route
                val (title, detail) = when {
                    !delivery -> (state.takeawayBranch?.name ?: stringResource(R.string.choose_branch)) to
                        state.takeawayBranch?.let { "${it.address} · ${branchStateText(it)}" }
                    state.deliveryPoint == null -> stringResource(R.string.set_address) to stringResource(R.string.branch_by_location)
                    route == null -> stringResource(R.string.checking) to null
                    route.branchId == null -> stringResource(R.string.no_branch_open) to null
                    else -> stringResource(R.string.route_branch, state.routeBranch?.name.orEmpty()) to buildString {
                        append(stringResource(R.string.delivery_fee)).append(": ").append(feeText(route.fee))
                        if (route.redirected) append("\n").append(stringResource(R.string.route_redirected))
                    }
                }
                Text(title, style = MaterialTheme.typography.titleSmall, color = colors.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (!detail.isNullOrBlank()) {
                    Text(detail, style = MaterialTheme.typography.bodySmall, color = colors.textMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.change), color = colors.select, style = MaterialTheme.typography.labelMedium)
        }
        val problem = when {
            delivery && state.deliveryPoint != null && state.route != null && state.route.branchId == null -> R.string.no_branch_open
            !delivery && state.takeawayBranch != null && !state.takeawayBranch.isOpen -> R.string.takeaway_closed
            !delivery && state.takeawayBranch == null && state.branches.isNotEmpty() -> R.string.no_branch_open
            else -> null
        }
        if (problem != null) {
            Notice(
                stringResource(problem),
                Tone.DANGER,
                Modifier.padding(start = 12.dp, end = 12.dp, bottom = 10.dp),
                icon = Icons.Outlined.ErrorOutline,
            )
        }
        if (delivery && state.deliveryPoint == null) {
            PillButton(
                stringResource(R.string.pick_on_map),
                onOpenLocation,
                Modifier.padding(start = 12.dp, end = 12.dp, bottom = 10.dp),
                icon = Icons.Filled.LocationOn,
            )
        }
    }
}

@Composable
private fun CartCard(state: CheckoutUiState, onQty: (String, Int) -> Unit, onRemove: (String) -> Unit) {
    val colors = NyffTheme.colors
    NyffCard(Modifier.fillMaxWidth()) {
        if (state.lines.isEmpty()) {
            Text(
                stringResource(R.string.empty_cart),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp, horizontal = 16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textMuted,
                textAlign = TextAlign.Center,
            )
        }
        state.lines.forEach { line -> CartLineRow(line, onQty, onRemove) }
        if (state.hasUnavailable) {
            Text(
                stringResource(R.string.unavailable_excluded),
                modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = colors.warning,
            )
        }
    }
}

@Composable
private fun CartLineRow(line: PricedLine, onQty: (String, Int) -> Unit, onRemove: (String) -> Unit) {
    val colors = NyffTheme.colors
    val product = line.product
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .alpha(if (line.available) 1f else 0.55f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FoodImage(product?.image.orEmpty(), Modifier.size(52.dp), shape = NyffShapes.media, iconSize = 20.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                product?.name?.text() ?: line.line.productId,
                style = MaterialTheme.typography.titleSmall,
                color = colors.text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (line.line.note.isNotBlank()) {
                Text(line.line.note, style = MaterialTheme.typography.bodySmall, color = colors.select, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(
                if (line.available) money(line.sum) else stringResource(R.string.line_unavailable),
                style = MaterialTheme.typography.bodySmall,
                color = if (line.available) colors.textMuted else colors.danger,
            )
        }
        Spacer(Modifier.width(8.dp))
        if (line.available) {
            QtyStepper(line.line.qty, onChange = { onQty(line.line.key, it) })
        } else {
            RoundIconButton(Icons.Outlined.Close, null, { onRemove(line.line.key) }, colors.surfaceMuted, colors.text)
        }
    }
}

@Composable
private fun ContactSection(state: CheckoutUiState, vm: CheckoutViewModel) {
    val problems = state.problems.takeIf { state.showProblems }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SectionLabel(stringResource(R.string.contacts))
        NyffTextField(
            value = state.contact.name,
            onValueChange = { value -> vm.updateContact { it.copy(name = value.take(100)) } },
            placeholder = stringResource(R.string.name),
            error = if (problems?.name == true) stringResource(R.string.name_required) else null,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
        )
        NyffTextField(
            value = state.contact.phone,
            onValueChange = { value -> vm.updateContact { it.copy(phone = Phone.clean(value)) } },
            placeholder = "90 123 45 67",
            prefix = Phone.PREFIX + " ",
            error = if (problems?.phone == true) stringResource(R.string.error_phone_required) else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            visualTransformation = PhoneVisualTransformation,
        )
        if (state.mode == OrderMode.DELIVERY) {
            NyffTextField(
                value = state.contact.address,
                onValueChange = { value -> vm.updateContact { it.copy(address = value.take(300)) } },
                placeholder = "${stringResource(R.string.address)}: ${stringResource(R.string.address_placeholder)}",
                // bir qatordan boshlanadi, yozilgan sari o'sadi
                singleLine = false,
                error = if (problems?.address == true) stringResource(R.string.error_address_required) else null,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            )
        }
        NyffTextField(
            value = state.note,
            onValueChange = vm::updateNote,
            placeholder = "${stringResource(R.string.note)}: ${stringResource(R.string.note_placeholder)}",
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        )
    }
}

@Composable
private fun PaymentSection(mode: OrderMode, payment: OnlinePayment, onChange: (OnlinePayment) -> Unit) {
    val colors = NyffTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel(stringResource(R.string.payment))
        // ikkalasi bir xil balandlikda — izoh matni bir qatorga sig'masa ham
        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PaymentOption(
                icon = Icons.Outlined.Smartphone,
                title = stringResource(R.string.pay_click),
                hint = stringResource(R.string.pay_click_hint),
                selected = payment == OnlinePayment.CLICK,
                onClick = { onChange(OnlinePayment.CLICK) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            PaymentOption(
                icon = Icons.Outlined.Payments,
                title = stringResource(R.string.pay_cash),
                hint = stringResource(if (mode == OrderMode.DELIVERY) R.string.pay_cash_delivery else R.string.pay_cash_pickup),
                selected = payment == OnlinePayment.CASH,
                onClick = { onChange(OnlinePayment.CASH) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
        if (payment == OnlinePayment.CLICK) {
            Text(
                stringResource(R.string.click_not_connected),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textFaint,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PaymentOption(
    icon: ImageVector,
    title: String,
    hint: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NyffTheme.colors
    Row(
        modifier
            .clip(NyffShapes.card)
            .background(colors.surface)
            .border(if (selected) 2.dp else 1.dp, if (selected) colors.select else colors.line, NyffShapes.card)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = if (selected) colors.select else colors.textFaint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = colors.text, maxLines = 1)
            Text(hint, style = MaterialTheme.typography.bodySmall, color = colors.textMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** Taomlar summasi; dostavkada — yetkazish narxi alohida (buyurtma summasiga kirmaydi, API.md 5.2). */
@Composable
private fun Summary(state: CheckoutUiState) {
    val colors = NyffTheme.colors
    NyffCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SummaryRow(stringResource(R.string.items_total), money(state.itemsTotal))
            val route = state.route
            if (state.mode == OrderMode.DELIVERY && route?.branchId != null) {
                SummaryRow(
                    "${stringResource(R.string.delivery_fee)} · ${stringResource(R.string.fee_separate)}",
                    feeText(route.fee),
                    muted = true,
                )
            }
            Spacer(Modifier.size(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.total),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = colors.text,
                )
                // jami summa — Ultra Bold (Inter ExtraBold)
                Text(money(state.itemsTotal), style = MaterialTheme.typography.headlineMedium, color = colors.text, maxLines = 1)
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, muted: Boolean = false) {
    val colors = NyffTheme.colors
    Row(verticalAlignment = Alignment.Top) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = colors.textMuted)
        Spacer(Modifier.width(12.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = if (muted) colors.textMuted else colors.text,
            textAlign = TextAlign.End,
        )
    }
}
