package pr.com.kotlin_nyff.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
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
import pr.com.kotlin_nyff.ui.common.BrandBadge
import pr.com.kotlin_nyff.ui.common.BranchSheet
import pr.com.kotlin_nyff.ui.common.ErrorBlock
import pr.com.kotlin_nyff.ui.common.FoodImage
import pr.com.kotlin_nyff.ui.common.Notice
import pr.com.kotlin_nyff.ui.common.NyffCard
import pr.com.kotlin_nyff.ui.common.OrderModeSwitch
import pr.com.kotlin_nyff.ui.common.PillButton
import pr.com.kotlin_nyff.ui.common.RoundIconButton
import pr.com.kotlin_nyff.ui.common.SystemBarIcons
import pr.com.kotlin_nyff.ui.common.Tone
import pr.com.kotlin_nyff.ui.common.branchStateText
import pr.com.kotlin_nyff.ui.common.errorRes
import pr.com.kotlin_nyff.ui.common.feeText
import pr.com.kotlin_nyff.ui.common.headlineRes
import pr.com.kotlin_nyff.ui.common.imageUrl
import pr.com.kotlin_nyff.ui.common.money
import pr.com.kotlin_nyff.ui.common.text
import pr.com.kotlin_nyff.ui.theme.NyffShapes
import pr.com.kotlin_nyff.ui.theme.NyffTheme

/** Bosh sahifa (web `MobileHome`): qora sarlavha, buyurtma turi, filial / manzil, kategoriyalar, taomlar, savat paneli. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenLocation: () -> Unit,
    onOpenCheckout: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenOrder: (String) -> Unit,
    vm: HomeViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val colors = NyffTheme.colors
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
                        onMode = vm::setMode,
                        onPlace = { if (state.mode == OrderMode.DELIVERY) onOpenLocation() else branchSheet = true },
                        onOrders = onOpenOrders,
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
            itemsIndexed(state.products, key = { _, product -> product.id }) { index, product ->
                ProductCard(
                    product = product,
                    count = state.cartCounts[product.id],
                    onOpen = { onOpen(product) },
                    onAdd = { onAdd(product) },
                    modifier = Modifier.padding(
                        start = if (index % 2 == 0) 16.dp else 6.dp,
                        end = if (index % 2 == 0) 6.dp else 16.dp,
                    ),
                )
            }
        }
    }
}

@Composable
private fun HomeHeader(
    state: HomeUiState,
    onMode: (OrderMode) -> Unit,
    onPlace: () -> Unit,
    onOrders: () -> Unit,
) {
    val colors = NyffTheme.colors
    Column(
        Modifier
            .fillMaxWidth()
            .clip(NyffShapes.header)
            .background(colors.header)
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BrandBadge()
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
            LanguageButton()
        }
        Column {
            Text(stringResource(R.string.greeting), style = MaterialTheme.typography.bodyMedium, color = colors.onHeaderMuted)
            Text(stringResource(R.string.home_title), style = MaterialTheme.typography.headlineSmall, color = colors.onHeader)
        }
        OrderModeSwitch(state.mode, onMode, onDark = true)
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

/** Til: UZ / RU / EN — tanlansa ilova shu tilda qayta ochiladi (holat saqlanadi). */
@Composable
private fun LanguageButton() {
    val colors = NyffTheme.colors
    var open by remember { mutableStateOf(false) }
    val current = AppLanguage.current()
    Box {
        Row(
            Modifier
                .height(38.dp)
                .clip(CircleShape)
                .background(colors.headerChip)
                .clickable(role = Role.Button) { open = true }
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.Translate, contentDescription = stringResource(R.string.language), tint = colors.onHeader, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(current.tag.uppercase(), style = MaterialTheme.typography.labelMedium, color = colors.onHeader)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            AppLanguage.entries.forEach { language ->
                DropdownMenuItem(
                    text = {
                        Text(
                            language.label,
                            fontWeight = if (language == current) FontWeight.ExtraBold else FontWeight.Normal,
                        )
                    },
                    onClick = {
                        open = false
                        if (language != current) AppLanguage.apply(language)
                    },
                )
            }
        }
    }
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
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
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

/** Dostavkada — manzil va server tanlagan filial; olib ketishda — tanlangan filial yopiq bo'lsa ogohlantirish. */
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
        route == null -> Notice(stringResource(R.string.checking), Tone.INFO, modifier)
        route.branchId == null -> Notice(stringResource(R.string.no_branch_open), Tone.DANGER, modifier, Icons.Outlined.ErrorOutline)
        else -> Notice(
            stringResource(R.string.route_to, state.routeBranch?.name.orEmpty()),
            Tone.INFO,
            modifier,
            Icons.Filled.LocationOn,
            supporting = buildString {
                append(stringResource(R.string.delivery_fee)).append(": ").append(feeText(route.fee))
                if (route.redirected) append("\n").append(stringResource(R.string.route_redirected))
            },
        )
    }
}

@Composable
private fun CategoryRow(state: HomeUiState, onCategory: (String) -> Unit) {
    val colors = NyffTheme.colors
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(state.categories, key = { it.id }) { category ->
            val active = category.id == state.selectedCategoryId
            Row(
                Modifier
                    .height(42.dp)
                    .clip(CircleShape)
                    .background(if (active) colors.accent else colors.surface)
                    .clickable(role = Role.Tab) { onCategory(category.id) }
                    .padding(start = if (category.icon.isNotBlank()) 6.dp else 14.dp, end = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val icon = imageUrl(category.icon)
                if (icon != null) {
                    AsyncImage(model = icon, contentDescription = null, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    category.name.text(),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (active) colors.onAccent else colors.text,
                    maxLines = 1,
                )
            }
        }
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
    NyffCard(modifier.fillMaxWidth(), onClick = onOpen) {
        Box {
            FoodImage(
                product.image,
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f),
            )
            if (count != null) {
                Text(
                    count.toString(),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(colors.accent)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                    color = colors.onAccent,
                )
            }
        }
        Column(Modifier.padding(10.dp)) {
            Text(
                product.name.text(),
                modifier = Modifier.heightIn(min = 38.dp),
                style = MaterialTheme.typography.titleSmall,
                color = colors.text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    money(product.price ?: 0),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = colors.text,
                    maxLines = 1,
                )
                RoundIconButton(Icons.Filled.Add, stringResource(R.string.add), onAdd, colors.accent, colors.onAccent)
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
