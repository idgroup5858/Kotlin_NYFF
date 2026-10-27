package pr.com.kotlin_nyff.ui.checkout

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pr.com.kotlin_nyff.R
import pr.com.kotlin_nyff.data.CatalogRepository
import pr.com.kotlin_nyff.data.DraftStore
import pr.com.kotlin_nyff.data.OrderRepository
import pr.com.kotlin_nyff.data.local.Contact
import pr.com.kotlin_nyff.data.local.UserPrefs
import pr.com.kotlin_nyff.data.model.Branch
import pr.com.kotlin_nyff.data.model.DeliveryRoute
import pr.com.kotlin_nyff.data.model.GeoPoint
import pr.com.kotlin_nyff.data.model.MobileOrderRequest
import pr.com.kotlin_nyff.data.model.OnlinePayment
import pr.com.kotlin_nyff.data.model.OnlineRequest
import pr.com.kotlin_nyff.data.model.OrderLineRequest
import pr.com.kotlin_nyff.data.remote.AppError
import pr.com.kotlin_nyff.domain.OrderMode
import pr.com.kotlin_nyff.domain.Phone
import pr.com.kotlin_nyff.domain.PricedLine
import pr.com.kotlin_nyff.domain.Shop
import pr.com.kotlin_nyff.ui.common.errorRes
import javax.inject.Inject

/** Formadagi kamchiliklar — tugma bosilgandan keyin maydon ostida ko'rsatiladi. Ism ixtiyoriy. */
data class FormProblems(
    val phone: Boolean = false,
    val address: Boolean = false,
) {
    val any: Boolean get() = phone || address
}

data class CheckoutUiState(
    val mode: OrderMode = OrderMode.DELIVERY,
    val lines: List<PricedLine> = emptyList(),
    val itemsTotal: Long = 0,
    val hasUnavailable: Boolean = false,
    val branches: List<Branch> = emptyList(),
    val takeawayBranch: Branch? = null,
    val deliveryPoint: GeoPoint? = null,
    val route: DeliveryRoute? = null,
    val routeBranch: Branch? = null,
    val contact: Contact = Contact(),
    val note: String = "",
    val showProblems: Boolean = false,
    val problems: FormProblems = FormProblems(),
    val placing: Boolean = false,
    @param:StringRes val error: Int? = null,
) {
    val availableLines: List<PricedLine> get() = lines.filter { it.available }

    /** Buyurtma beriladigan joy bor: dostavkada — server tanlagan ochiq filial, olib ketishda — ochiq filial. */
    val placeReady: Boolean
        get() = when (mode) {
            OrderMode.DELIVERY -> deliveryPoint != null && route?.branchId != null
            OrderMode.TAKEAWAY -> takeawayBranch?.isOpen == true
        }
}

/** Rasmiylashtirish (web `MobileCheckout`): savat, tur, joy, mijoz ma'lumotlari, to'lov → `POST /mobile/orders`. */
@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val catalog: CatalogRepository,
    private val draft: DraftStore,
    private val orders: OrderRepository,
    private val prefs: UserPrefs,
) : ViewModel() {

    private data class Form(
        val contact: Contact = Contact(),
        val note: String = "",
        val showProblems: Boolean = false,
        val placing: Boolean = false,
        val error: Int? = null,
    )

    private val form = MutableStateFlow(Form())

    private val placed = Channel<String>(Channel.BUFFERED)

    /** Buyurtma berildi — holat ekraniga o'tish (id). */
    val placedOrders: Flow<String> = placed.receiveAsFlow()

    private val menu = combine(draft.mode, catalog.branches, draft.takeawayBranchId, draft.route, catalog.menus) { mode, branches, takeaway, route, menus ->
        branches?.let { Shop.menuBranchId(mode, it, takeaway, route) }?.let { menus[it] }
    }

    val state: StateFlow<CheckoutUiState> = combine(
        combine(draft.mode, draft.takeawayBranchId, draft.deliveryPoint, draft.route, ::DraftParts),
        draft.lines,
        catalog.branches,
        menu,
        form,
    ) { d, lines, allBranches, menu, f ->
        val priced = Shop.price(lines, menu)
        val delivery = d.mode == OrderMode.DELIVERY
        CheckoutUiState(
            mode = d.mode,
            lines = priced,
            itemsTotal = priced.filter { it.available }.sumOf { it.sum },
            hasUnavailable = menu != null && priced.any { !it.available },
            branches = allBranches.orEmpty().let(Shop::visibleBranches),
            takeawayBranch = allBranches?.let { Shop.takeawayBranch(it, d.takeawayBranchId) },
            deliveryPoint = d.point,
            route = d.route,
            routeBranch = allBranches?.firstOrNull { it.id == d.route?.branchId },
            contact = f.contact,
            note = f.note,
            showProblems = f.showProblems,
            problems = FormProblems(
                phone = !Phone.isComplete(f.contact.phone),
                address = delivery && f.contact.address.isBlank(),
            ),
            placing = f.placing,
            error = f.error,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CheckoutUiState())

    private data class DraftParts(val mode: OrderMode, val takeawayBranchId: String?, val point: GeoPoint?, val route: DeliveryRoute?)

    init {
        viewModelScope.launch {
            val saved = prefs.contact.first()
            form.update { it.copy(contact = saved) }
        }
        viewModelScope.launch { refreshPlace() }
    }

    fun setMode(mode: OrderMode) {
        draft.setMode(mode)
        form.update { it.copy(error = null) }
        viewModelScope.launch { refreshPlace() }
    }

    fun selectTakeawayBranch(id: String) {
        draft.setTakeawayBranch(id)
        form.update { it.copy(error = null) }
    }

    fun changeQty(key: String, delta: Int) = draft.changeQty(key, delta)

    fun remove(key: String) = draft.remove(key)

    fun updateContact(change: (Contact) -> Contact) = form.update { it.copy(contact = change(it.contact), error = null) }

    fun updateNote(note: String) = form.update { it.copy(note = note.take(500)) }

    fun place() {
        val current = state.value
        if (current.placing) return
        if (current.problems.any || current.availableLines.isEmpty() || !current.placeReady) {
            form.update { it.copy(showProblems = true) }
            return
        }
        val delivery = current.mode == OrderMode.DELIVERY
        val contact = current.contact
        val request = MobileOrderRequest(
            type = current.mode.orderType,
            branchId = if (delivery) null else current.takeawayBranch?.id,
            items = current.availableLines.map { OrderLineRequest(it.line.productId, it.line.qty, it.line.note) },
            note = current.note.trim(),
            online = OnlineRequest(
                name = contact.name.trim(),
                phone = Phone.full(contact.phone),
                address = if (delivery) contact.address.trim() else "",
                payment = contact.payment,
                location = if (delivery) current.deliveryPoint else null,
            ),
        )
        form.update { it.copy(placing = true, error = null, showProblems = true) }
        viewModelScope.launch {
            try {
                val saved = orders.place(request, deliveryFee = if (delivery) current.route?.fee else null)
                prefs.saveContact(contact)
                draft.clearCart()
                form.update { it.copy(placing = false) }
                placed.send(saved.order.id)
            } catch (e: AppError) {
                form.update { it.copy(placing = false, error = placeErrorRes(e)) }
                when (e.serverCode) {
                    // holat o'zgargan — ekrandagi filial / narxlar yangilansin
                    "branch_closed", "not_found" -> refreshPlace()
                    "product_unavailable" -> refreshMenu()
                }
            }
        }
    }

    fun setPayment(payment: OnlinePayment) = updateContact { it.copy(payment = payment) }

    /** Filiallar holati va dostavka yo'nalishi — rasmiylashtirish ochilganda yangidan (eskirgan bo'lishi mumkin). */
    private suspend fun refreshPlace() {
        try {
            catalog.refreshBranches()
            draft.deliveryPoint.value?.let { point -> draft.updateRoute(point, catalog.route(point)) }
        } catch (_: AppError) {
            // ekranda oxirgi ma'lum holat qoladi; buyurtma berishda server yana tekshiradi
        }
        refreshMenu()
    }

    private suspend fun refreshMenu() {
        val branches = catalog.branches.value ?: return
        val id = Shop.menuBranchId(draft.mode.value, branches, draft.takeawayBranchId.value, draft.route.value) ?: return
        try {
            catalog.menu(id, force = true)
        } catch (_: AppError) {
            // keshdagi menyu qoladi
        }
    }

    @StringRes
    private fun placeErrorRes(error: AppError): Int =
        if (error is AppError.Network) R.string.network_place else errorRes(error)
}
