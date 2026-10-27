package pr.com.kotlin_nyff.ui.home

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import pr.com.kotlin_nyff.domain.NBSP
import pr.com.kotlin_nyff.ui.theme.Coal
import pr.com.kotlin_nyff.ui.theme.Gold
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import pr.com.kotlin_nyff.R
import pr.com.kotlin_nyff.core.AppLanguage
import pr.com.kotlin_nyff.data.model.MenuProduct
import pr.com.kotlin_nyff.domain.Format
import pr.com.kotlin_nyff.domain.OrderMode
import pr.com.kotlin_nyff.domain.OrderProgress
import pr.com.kotlin_nyff.domain.Shop
import pr.com.kotlin_nyff.ui.common.BrandBadge
import pr.com.kotlin_nyff.ui.common.BranchSheet
import pr.com.kotlin_nyff.ui.common.ErrorBlock
import pr.com.kotlin_nyff.ui.common.FoodImage
import pr.com.kotlin_nyff.ui.common.Notice
import pr.com.kotlin_nyff.ui.common.NyffCard
import pr.com.kotlin_nyff.ui.common.PillButton
import pr.com.kotlin_nyff.ui.common.PlaceSheet
import pr.com.kotlin_nyff.ui.common.RoundIconButton
import pr.com.kotlin_nyff.ui.common.SystemBarIcons
import pr.com.kotlin_nyff.ui.common.SettingsMenuButton
import pr.com.kotlin_nyff.ui.common.Tone
import pr.com.kotlin_nyff.ui.common.branchStateText
import pr.com.kotlin_nyff.ui.common.errorRes
import pr.com.kotlin_nyff.ui.common.headlineRes
import pr.com.kotlin_nyff.ui.common.imageUrl
import pr.com.kotlin_nyff.ui.common.money
import pr.com.kotlin_nyff.ui.common.text
import pr.com.kotlin_nyff.ui.theme.NyffShapes
import pr.com.kotlin_nyff.ui.theme.NyffTheme

/**
 * Bosh sahifa (web `MobileHome`): qora sarlavha, joy (manzil / filial — bosilsa yetkazib berish yoki olib ketish
 * tanlanadi), kategoriyalar, taomlar, savat paneli. "NY" belgisiga 10 marta bosilsa — kuryer kirishi ([onCourierEntry]).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenLocation: () -> Unit,
    onOpenCheckout: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenOrder: (String) -> Unit,
    onCourierEntry: () -> Unit,
    vm: HomeViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val colors = NyffTheme.colors
    var placeSheet by rememberSaveable { mutableStateOf(false) }
    var branchSheet by rememberSaveable { mutableStateOf(false) }
    var openedProduct by remember { mutableStateOf<MenuProduct?>(null) }

    SystemBarIcons(darkIcons = false)
    LifecycleResumeEffect(Unit) {
        vm.onResume()
        onPauseOrDispose { }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(colors.page),
    ) {
        PullToRefreshBox(
            isRefreshing = state.refreshing,
            onRefresh = vm::refresh,
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = if (state.cartQty > 0) 112.dp else 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                fullWidth("header") {
                    HomeHeader(
                        state = state,
                        onPlace = { placeSheet = true },
                        onOrders = onOpenOrders,
                        onCourierEntry = onCourierEntry,
                    )
                }
                state.currentOrder?.let { current ->
                    fullWidth("current-order") {
                        CurrentOrderBanner(
                            number = current.order.number,
                            text = stringResource(headlineRes(OrderProgress.headline(current.order))),
                            onClick = { onOpenOrder(current.order.id) },
                        )
                    }
                }
                fullWidth("place-notice") {
                    PlaceNotice(state, onOpenLocation = onOpenLocation, onRetryRoute = vm::retryRoute)
                }
                menuContent(
                    state = state,
                    onRetry = vm::retryMenu,
                    onCategory = vm::selectCategory,
                    onOpen = { openedProduct = it },
                    onAdd = { vm.add(it.id) },
                )
            }
        }

        if (state.cartQty > 0) {
            CartBar(
                qty = state.cartQty,
                total = state.cartTotal,
                onClick = onOpenCheckout,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }

    // shu usulni qayta tanlash — joyni o'zgartirish; boshqasiga o'tish — joy hali yo'q bo'lsa tanlash oynasi
    if (placeSheet) {
        PlaceSheet(
            mode = state.mode,
            onDelivery = {
                placeSheet = false
                if (state.mode == OrderMode.DELIVERY || state.deliveryPoint == null) onOpenLocation()
                vm.setMode(OrderMode.DELIVERY)
            },
            onPickup = {
                placeSheet = false
                if (state.mode == OrderMode.TAKEAWAY || state.takeawayBranch == null) branchSheet = true
                vm.setMode(OrderMode.TAKEAWAY)
            },
            onDismiss = { placeSheet = false },
        )
    }
    if (branchSheet) {
        BranchSheet(
            branches = state.branches.orEmpty(),
            selectedId = state.takeawayBranch?.id,
            onSelect = {
                vm.selectTakeawayBranch(it.id)
                branchSheet = false
            },
            onDismiss = { branchSheet = false },
        )
    }
    openedProduct?.let { product ->
        ProductSheet(
            product = product,
            onAdd = { qty, note ->
                vm.add(product.id, qty, note)
                openedProduct = null
            },
            onDismiss = { openedProduct = null },
        )
    }
}

private fun LazyGridScope.fullWidth(key: String, content: @Composable () -> Unit) {
    item(key = key, span = { GridItemSpan(maxLineSpan) }) { content() }
}

private fun LazyGridScope.menuContent(
    state: HomeUiState,
    onRetry: () -> Unit,
    onCategory: (String) -> Unit,
    onOpen: (MenuProduct) -> Unit,
    onAdd: (MenuProduct) -> Unit,
) {
    val error = state.menuError ?: state.branchesError.takeIf { state.branches == null }
    when {
        !state.menuLoaded && error != null -> fullWidth("menu-error") { ErrorBlock(error, onRetry) }
        !state.menuLoaded -> fullWidth("menu-loading") {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = NyffTheme.colors.select)
            }
        }
        state.categories.isEmpty() -> fullWidth("menu-empty") {
            Text(
                stringResource(R.string.menu_empty),
                modifier = Modifier.padding(24.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = NyffTheme.colors.textMuted,
            )
        }
        else -> {
            fullWidth("categories") {
                CategoryRow(state, onCategory)
            }
            // juft-juft qator: ikkala karta bir xil balandlikda (nomi 2 qatorli bo'lsa — yonidagisi ham cho'ziladi)
            val rows = state.products.chunked(2)
            items(rows.size, key = { rows[it].first().id }, span = { GridItemSpan(maxLineSpan) }) { index ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    rows[index].forEach { product ->
                        ProductCard(
                            product = product,
                            count = state.cartCounts[product.id],
                            onOpen = { onOpen(product) },
                            onAdd = { onAdd(product) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )
                    }
                    if (rows[index].size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    state: HomeUiState,
    onPlace: () -> Unit,
    onOrders: () -> Unit,
    onCourierEntry: () -> Unit,
) {
    val colors = NyffTheme.colors
    Column(
        Modifier
            .fillMaxWidth()
            .clip(NyffShapes.header)
            .background(colors.header)
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val entry by rememberUpdatedState(onCourierEntry)
            val taps = remember { CourierEntryTaps() }
            BrandBadge(
                Modifier.pointerInput(taps) {
                    detectTapGestures { if (taps.tap(SystemClock.uptimeMillis())) entry() }
                },
            )
            Spacer(Modifier.width(10.dp))
            val delivery = state.mode == OrderMode.DELIVERY
            Box(Modifier.weight(1f)) {
                HeaderChip(
                    icon = if (delivery) Icons.Filled.LocationOn else Icons.Outlined.Storefront,
                    text = when {
                        delivery && state.deliveryPoint == null -> stringResource(R.string.set_address)
                        delivery -> stringResource(R.string.location)
                        else -> state.takeawayBranch?.name ?: stringResource(R.string.choose_branch)
                    },
                    onClick = onPlace,
                )
            }
            Spacer(Modifier.width(8.dp))
            HeaderIconButton(Icons.AutoMirrored.Outlined.ReceiptLong, stringResource(R.string.my_orders), onOrders)
            Spacer(Modifier.width(8.dp))
            // til va yorug' / tungi rejim — bitta "⋮" menyusida
            SettingsMenuButton()
        }
        Column {
            Text(stringResource(R.string.greeting), style = MaterialTheme.typography.bodyMedium, color = colors.onHeaderMuted)
            Text(stringResource(R.string.home_title), style = MaterialTheme.typography.headlineLarge, color = colors.onHeader)
        }
    }
}

@Composable
private fun HeaderChip(icon: ImageVector, text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = NyffTheme.colors
    Row(
        modifier
            .height(38.dp)
            .clip(CircleShape)
            .background(colors.headerChip)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 10.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            text,
            modifier = Modifier.weight(1f, fill = false),
            style = MaterialTheme.typography.labelMedium,
            color = colors.onHeader,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.width(2.dp))
        Icon(Icons.Filled.ExpandMore, contentDescription = null, tint = colors.onHeaderMuted, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun HeaderIconButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    val colors = NyffTheme.colors
    RoundIconButton(icon, description, onClick, colors.headerChip, colors.onHeader, size = 38.dp, iconSize = 20.dp)
}

@Composable
private fun CurrentOrderBanner(number: Int, text: String, onClick: () -> Unit) {
    val colors = NyffTheme.colors
    NyffCard(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        onClick = onClick,
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(Format.orderNumber(number), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = colors.text)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.my_order), style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                Text(text, style = MaterialTheme.typography.titleSmall, color = colors.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = colors.textFaint)
        }
    }
}

/** Dostavkada — manzil belgilanmagan yoki filial topilmagan bo'lsa; olib ketishda — tanlangan filial yopiq bo'lsa ogohlantirish. */
@Composable
private fun PlaceNotice(state: HomeUiState, onOpenLocation: () -> Unit, onRetryRoute: () -> Unit) {
    val colors = NyffTheme.colors
    val modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
    if (state.branches == null) return
    if (state.mode == OrderMode.TAKEAWAY) {
        val branch = state.takeawayBranch
        when {
            branch == null -> Notice(stringResource(R.string.no_branch_open), Tone.DANGER, modifier, Icons.Outlined.ErrorOutline)
            !branch.isOpen -> Notice(
                stringResource(R.string.takeaway_closed),
                Tone.WARNING,
                modifier,
                Icons.Outlined.ErrorOutline,
                supporting = "${branch.name} · ${branchStateText(branch)}",
            )
        }
        return
    }
    val route = state.route
    when {
        state.deliveryPoint == null -> NyffCard(modifier) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Map, contentDescription = null, tint = colors.select, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.branch_by_location), style = MaterialTheme.typography.titleSmall, color = colors.text)
                    Text(stringResource(R.string.menu_preview_hint), style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                }
            }
            PillButton(
                stringResource(R.string.pick_on_map),
                onOpenLocation,
                Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                icon = Icons.Filled.LocationOn,
            )
        }
        route == null && state.routeError != null -> Notice(
            stringResource(errorRes(state.routeError)),
            Tone.DANGER,
            modifier,
            Icons.Outlined.ErrorOutline,
            action = { PillButton(stringResource(R.string.retry), onRetryRoute) },
        )
        // qaysi filial olishi va yetkazish narxi — rasmiylashtirishda ko'rinadi; bu yerda faqat muammo
        route != null && route.branchId == null ->
            Notice(stringResource(R.string.no_branch_open), Tone.DANGER, modifier, Icons.Outlined.ErrorOutline)
    }
}

/** "Barchasi" ikonkasi — kategoriya ikonkalari to'plamidan (web `public/images/icons`, server beradi). */
private const val ALL_CATEGORIES_ICON = "/images/icons/bento-box.png"

/** Kategoriyalar: boshida "Barchasi" (hamma taomlar — standart), keyin menyudagilar. */
@Composable
private fun CategoryRow(state: HomeUiState, onCategory: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = Shop.ALL_CATEGORIES) {
            CategoryChip(
                text = stringResource(R.string.all_categories),
                active = state.selectedCategoryId == Shop.ALL_CATEGORIES,
                onClick = { onCategory(Shop.ALL_CATEGORIES) },
            ) {
                // boshqa kategoriyalardagidek 3D ikonka (o'sha to'plamdan — bir qutida turli taomlar)
                AsyncImage(model = imageUrl(ALL_CATEGORIES_ICON), contentDescription = null, modifier = Modifier.size(22.dp))
            }
        }
        items(state.categories, key = { it.id }) { category ->
            val icon = imageUrl(category.icon)
            CategoryChip(
                text = category.name.text(),
                active = category.id == state.selectedCategoryId,
                onClick = { onCategory(category.id) },
                leading = icon?.let { url -> { AsyncImage(model = url, contentDescription = null, modifier = Modifier.size(22.dp)) } },
            )
        }
    }
}

@Composable
private fun CategoryChip(text: String, active: Boolean, onClick: () -> Unit, leading: (@Composable () -> Unit)? = null) {
    val colors = NyffTheme.colors
    Row(
        Modifier
            .height(34.dp)
            .clip(CircleShape)
            .background(if (active) colors.accent else colors.surface)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(start = if (leading != null) 6.dp else 12.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(5.dp))
        }
        Text(text, style = MaterialTheme.typography.labelMedium, color = if (active) colors.onAccent else colors.text, maxLines = 1)
    }
}

@Composable
private fun ProductCard(
    product: MenuProduct,
    count: Int?,
    onOpen: () -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NyffTheme.colors
    val shape = RoundedCornerShape(16.dp)
    val soum = stringResource(R.string.soum)
    // web "GoMeal" kartasi: oq, soyasiz (tungida — chegara), 16 yumaloq, ichida 8 bo'shliq
    Column(
        modifier
            .fillMaxWidth()
            .then(if (colors.isDark) Modifier.border(1.dp, colors.line, shape) else Modifier)
            .clip(shape)
            .background(colors.surface)
            .clickable(onClick = onOpen)
            .padding(8.dp),
    ) {
        Box {
            // rasm butunligicha (shaffof PNG) karta fonida
            FoodImage(
                product.image,
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f),
                shape = RoundedCornerShape(12.dp),
                background = colors.surface,
                contentScale = ContentScale.Fit,
            )
            if (count != null) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .defaultMinSize(minWidth = 28.dp, minHeight = 28.dp)
                        .clip(CircleShape)
                        .background(Gold)
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(count.toString(), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                }
            }
        }
        // nom tepada, narx qatori doim pastda — qatordagi kartalar narxi bir chiziqda
        Column(
            Modifier
                .weight(1f)
                .padding(8.dp),
        ) {
            Text(
                product.name.text(),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.weight(1f).heightIn(min = 8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                // joy yetmasa (katta shrift sozlamasi) — narx o'zi kichrayadi, "so'm" kesilmaydi
                BasicText(
                    buildAnnotatedString {
                        append(Format.number(product.price ?: 0))
                        append(NBSP)
                        withStyle(SpanStyle(color = Gold, fontSize = 0.8.em)) { append(soum) }
                    },
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium.copy(color = colors.text),
                    maxLines = 1,
                    softWrap = false,
                    autoSize = TextAutoSize.StepBased(minFontSize = 9.sp, maxFontSize = 15.sp, stepSize = 0.5.sp),
                )
                Spacer(Modifier.width(6.dp))
                Box(
                    Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Gold)
                        .clickable(role = Role.Button, onClick = onAdd),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add), tint = Coal, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun CartBar(qty: Int, total: Long, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = NyffTheme.colors
    Row(
        modifier
            .navigationBarsPadding()
            .padding(12.dp)
            .fillMaxWidth()
            .clip(NyffShapes.card)
            .background(colors.bar)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(colors.accent),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.ShoppingBag, contentDescription = null, tint = colors.onAccent, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.cart), style = MaterialTheme.typography.titleSmall, color = colors.onBar)
            Text(stringResource(R.string.items_count, qty), style = MaterialTheme.typography.bodySmall, color = colors.onBar.copy(alpha = 0.6f))
        }
        Text(money(total), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = colors.onBar)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = colors.onBar)
    }
}
