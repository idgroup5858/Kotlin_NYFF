package pr.com.kotlin_nyff.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pr.com.kotlin_nyff.data.CatalogRepository
import pr.com.kotlin_nyff.data.DraftStore
import pr.com.kotlin_nyff.data.OrderRepository
import pr.com.kotlin_nyff.data.SavedOrder
import pr.com.kotlin_nyff.data.model.Branch
import pr.com.kotlin_nyff.data.model.Category
import pr.com.kotlin_nyff.data.model.DeliveryRoute
import pr.com.kotlin_nyff.data.model.GeoPoint
import pr.com.kotlin_nyff.data.model.MenuProduct
import pr.com.kotlin_nyff.data.remote.AppError
import pr.com.kotlin_nyff.domain.Cart
import pr.com.kotlin_nyff.domain.CartLine
import pr.com.kotlin_nyff.domain.OrderMode
import pr.com.kotlin_nyff.domain.OrderProgress
import pr.com.kotlin_nyff.domain.Shop
import javax.inject.Inject

data class HomeUiState(
    val mode: OrderMode = OrderMode.DELIVERY,
    /** Mijozga ko'rinadigan filiallar; `null` — hali yuklanmagan. */
    val branches: List<Branch>? = null,
    val branchesError: AppError? = null,
    val takeawayBranch: Branch? = null,
    val deliveryPoint: GeoPoint? = null,
    val route: DeliveryRoute? = null,
    val routeBranch: Branch? = null,
    val routeLoading: Boolean = false,
    val routeError: AppError? = null,
    val menuLoading: Boolean = false,
    val menuError: AppError? = null,
    val menuLoaded: Boolean = false,
    val categories: List<Category> = emptyList(),
    /** kategoriya id'si yoki [Shop.ALL_CATEGORIES] ("Barchasi" — standart) */
    val selectedCategoryId: String = Shop.ALL_CATEGORIES,
    /** Tanlangan kategoriyaning (yoki hamma kategoriyalarning) taomlari. */
    val products: List<MenuProduct> = emptyList(),
    val cartCounts: Map<String, Int> = emptyMap(),
    val cartQty: Int = 0,
    val cartTotal: Long = 0,
    /** "Buyurtmam" banneri — ishdagi yoki yaqinda tayyor bo'lgan oxirgi buyurtma. */
    val currentOrder: SavedOrder? = null,
    val refreshing: Boolean = false,
)

/**
 * Bosh sahifa: filiallar, buyurtma turi, dostavka yo'nalishi, menyu (tanlangan / server tanlagan filialniki) va savat.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val catalog: CatalogRepository,
    private val draft: DraftStore,
    private val orders: OrderRepository,
) : ViewModel() {

    private data class Loading(
        val branchesError: AppError? = null,
        val routeLoading: Boolean = false,
        val routeError: AppError? = null,
        val menuLoading: Boolean = false,
        val menuError: AppError? = null,
        val refreshing: Boolean = false,
    )

    private data class DraftSnapshot(
        val mode: OrderMode,
        val takeawayBranchId: String?,
        val point: GeoPoint?,
        val route: DeliveryRoute?,
        val lines: List<CartLine>,
    )

    private val loading = MutableStateFlow(Loading())
    private val selectedCategory = MutableStateFlow<String?>(null)

    private val menuBranchId: StateFlow<String?> =
        combine(draft.mode, catalog.branches, draft.takeawayBranchId, draft.route) { mode, branches, takeaway, route ->
            branches?.let { Shop.menuBranchId(mode, it, takeaway, route) }
        }.distinctUntilChanged().stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val state: StateFlow<HomeUiState> = combine(
        combine(draft.mode, draft.takeawayBranchId, draft.deliveryPoint, draft.route, draft.lines, ::DraftSnapshot),
        catalog.branches,
        combine(catalog.menus, menuBranchId) { menus, id -> id?.let { menus[it] } },
        combine(loading, selectedCategory, ::Pair),
        orders.history,
    ) { d, allBranches, menu, (load, category), history ->
        val branches = allBranches?.let(Shop::visibleBranches)
        val categoryId = Shop.selectedCategory(menu, category)
        val priced = Shop.price(d.lines, menu).filter { it.available }
        HomeUiState(
            mode = d.mode,
            branches = branches,
            branchesError = load.branchesError,
            takeawayBranch = allBranches?.let { Shop.takeawayBranch(it, d.takeawayBranchId) },
            deliveryPoint = d.point,
            route = d.route,
            routeBranch = allBranches?.firstOrNull { it.id == d.route?.branchId },
            routeLoading = load.routeLoading,
            routeError = load.routeError,
            menuLoading = load.menuLoading,
            menuError = load.menuError,
            menuLoaded = menu != null,
            categories = menu?.categories.orEmpty(),
            selectedCategoryId = categoryId,
            products = Shop.products(menu, categoryId),
            cartCounts = Cart.countsByProduct(d.lines),
            cartQty = priced.sumOf { it.line.qty },
            cartTotal = priced.sumOf { it.sum },
            currentOrder = history.firstOrNull { OrderProgress.isCurrent(it.order) },
            refreshing = load.refreshing,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    init {
        viewModelScope.launch { loadBranches() }
        viewModelScope.launch { menuBranchId.filterNotNull().collectLatest { loadMenu(it, force = false) } }
        // Saqlangan nuqta (ilova qayta ochilganda) — server javobini qayta so'rash
        viewModelScope.launch {
            draft.deliveryPoint.filterNotNull().distinctUntilChanged().collect { point ->
                if (draft.route.value == null) loadRoute(point)
            }
        }
    }

    /** Ekran qayta ko'ringanda: filiallar holati va dostavka yo'nalishi o'zgargan bo'lishi mumkin. */
    fun onResume() {
        viewModelScope.launch { loadBranches() }
        draft.deliveryPoint.value?.let { point -> viewModelScope.launch { loadRoute(point) } }
        viewModelScope.launch { refreshCurrentOrders() }
    }

    /** Pastga tortib yangilash: filiallar, yo'nalish va menyu (narx / "sotuvda bor") serverdan. */
    fun refresh() {
        viewModelScope.launch {
            loading.update { it.copy(refreshing = true) }
            loadBranches()
            draft.deliveryPoint.value?.let { loadRoute(it) }
            menuBranchId.value?.let { loadMenu(it, force = true) }
            loading.update { it.copy(refreshing = false) }
        }
    }

    /** "Qayta urinish": aloqa uzilgan bo'lsa filiallar, yo'nalish va menyu birga yuklanmagan bo'ladi. */
    fun retryMenu() {
        viewModelScope.launch {
            loadBranches()
            draft.deliveryPoint.value?.takeIf { draft.route.value == null }?.let { loadRoute(it) }
            menuBranchId.value?.let { id -> if (catalog.menus.value[id] == null) loadMenu(id, force = false) }
        }
    }

    fun retryRoute() {
        draft.deliveryPoint.value?.let { point -> viewModelScope.launch { loadRoute(point) } }
    }

    fun selectTakeawayBranch(id: String) = draft.setTakeawayBranch(id)

    fun selectCategory(id: String) {
        selectedCategory.value = id
    }

    fun add(productId: String, qty: Int = 1) = draft.add(productId, qty)

    private suspend fun loadBranches() {
        try {
            catalog.refreshBranches()
            loading.update { it.copy(branchesError = null) }
        } catch (e: AppError) {
            loading.update { it.copy(branchesError = e) }
        }
    }

    private suspend fun loadRoute(point: GeoPoint) {
        loading.update { it.copy(routeLoading = true) }
        try {
            draft.updateRoute(point, catalog.route(point))
            loading.update { it.copy(routeLoading = false, routeError = null) }
        } catch (e: AppError) {
            loading.update { it.copy(routeLoading = false, routeError = e) }
        }
    }

    private suspend fun loadMenu(branchId: String, force: Boolean) {
        loading.update { it.copy(menuLoading = true) }
        try {
            catalog.menu(branchId, force)
            loading.update { it.copy(menuLoading = false, menuError = null) }
        } catch (e: AppError) {
            loading.update { it.copy(menuLoading = false, menuError = e) }
        }
    }

    /** Banner eskirmasin: ishdagi buyurtmalar holati serverdan (ilova yopiq turgan bo'lishi mumkin). */
    private suspend fun refreshCurrentOrders() {
        orders.history.first()
            .filter { OrderProgress.isCurrent(it.order) }
            .take(3)
            .forEach { saved ->
                try {
                    orders.refresh(saved.order.id)
                } catch (_: AppError) {
                    // keyingi ochilishda yana urinadi
                }
            }
    }
}
