package pr.com.kotlin_nyff.courier.ui.main

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pr.com.kotlin_nyff.R
import pr.com.kotlin_nyff.courier.data.PrefsStore
import pr.com.kotlin_nyff.courier.data.SessionStore
import pr.com.kotlin_nyff.courier.data.AvailableOrder
import pr.com.kotlin_nyff.data.model.Branch
import pr.com.kotlin_nyff.courier.data.CourierOrder
import pr.com.kotlin_nyff.courier.data.CourierOrders
import pr.com.kotlin_nyff.courier.data.CourierState
import pr.com.kotlin_nyff.courier.data.DutyRequest
import pr.com.kotlin_nyff.courier.data.ReturnReason
import pr.com.kotlin_nyff.courier.data.ReturnRequest
import pr.com.kotlin_nyff.data.remote.AppError
import pr.com.kotlin_nyff.courier.data.remote.CourierApi
import pr.com.kotlin_nyff.courier.data.remote.CourierEvent
import pr.com.kotlin_nyff.courier.data.remote.CourierSocket
import pr.com.kotlin_nyff.data.remote.apiCall
import pr.com.kotlin_nyff.domain.Format
import pr.com.kotlin_nyff.courier.ui.Notifier
import pr.com.kotlin_nyff.courier.ui.courierErrorRes
import javax.inject.Inject

data class MainUiState(
    val me: CourierState? = null,
    val branches: Map<String, Branch> = emptyMap(),
    val available: List<AvailableOrder> = emptyList(),
    /** qo'ldagilar: yo'lda — server taklif qilgan tartibda */
    val active: List<CourierOrder> = emptyList(),
    /** bugun yetkazilgan / qaytarilganlar */
    val done: List<CourierOrder> = emptyList(),
    val loaded: Boolean = false,
    val loadError: AppError? = null,
    val refreshing: Boolean = false,
    /** socket ulangan — ro'yxatlar darhol yangilanadi */
    val live: Boolean = false,
    /** bajarilayotgan amal: buyurtma id, [MainViewModel.DEPART] yoki [MainViewModel.DUTY] */
    val busy: String? = null,
    @param:StringRes val message: Int? = null,
    /** kuryer "Yangi" ro'yxati uchun tanlagan filial (telefonda saqlanadi); `null` — hali tanlanmagan */
    val chosenBranchId: String? = null,
) {
    val onDuty: Boolean get() = me?.onDuty == true
    val departed: Boolean get() = active.any { it.departedAt != null }
    fun branch(id: String?): Branch? = branches[id]

    /** Filial tanlovi qulf: qo'lda shu filial buyurtmalari bor — boshqasidan olib bo'lmaydi (`other_branch`). */
    val branchLocked: Boolean get() = me?.branchId != null

    /** "Yangi" ro'yxati filiali: qo'ldagilar filiali, bo'lmasa kuryer tanlagani (faol bo'lsa); `null` — tanlanmagan. */
    val pickBranchId: String? get() = me?.branchId ?: chosenBranchId?.takeIf { branches[it]?.active == true }

    /** Filial tugmalari — faol filiallar, server tartibida (qo'ldagilar filiali faol bo'lmasa ham). */
    val pickBranches: List<Branch> get() = branches.values.filter { it.active || it.id == me?.branchId }

    /** Tanlangan filialning "Yangi" buyurtmalari; filial tanlanmagan — bo'sh. */
    val shownAvailable: List<AvailableOrder>
        get() = pickBranchId?.let { id -> available.filter { it.branchId == id } }.orEmpty()

    fun availableCount(branchId: String): Int = available.count { it.branchId == branchId }

    /** "Yangi" tabidagi son: olsa bo'ladiganlar — tanlangan filialdan (tanlanmagan bo'lsa — hammasidan). */
    val newCount: Int get() = (if (pickBranchId != null) shownAvailable else available).count { it.blocked == null }
}

/**
 * Kuryer ekrani (API.md 12): "Yangi" (kuryer tanlagan filialniki), "Meniki", "Bugun". Socket (`couriers`, `courier:<id>`)
 * har o'zgarishda ro'yxatlarni REST'dan qayta oldiradi; socket ulanmagan paytda har 20 soniyada so'rov. Ilova fonda
 * bo'lsa — tanlangan filialdagi yangi buyurtma haqida bildirishnoma. Sessiya tugasa (chiqish / `401`) — hammasi to'xtaydi.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val api: CourierApi,
    private val session: SessionStore,
    private val prefs: PrefsStore,
    private val socket: CourierSocket,
) : ViewModel() {

    private val _state = MutableStateFlow(MainUiState())
    val state: StateFlow<MainUiState> = _state.asStateFlow()

    private val refreshRequests = Channel<Unit>(Channel.CONFLATED)

    /** "Yangi" ro'yxatida ko'rilgan buyurtmalar — bildirishnoma faqat yangilariga; birinchi yuklashda — hech biriga. */
    private var seenAvailable: Set<String>? = null

    init {
        viewModelScope.launch {
            prefs.branchId.collect { id -> _state.update { it.copy(chosenBranchId = id) } }
        }
        viewModelScope.launch {
            session.session.collectLatest { current ->
                // filial tanlovi — telefonniki, sessiya almashsa ham qoladi
                _state.value = MainUiState(chosenBranchId = _state.value.chosenBranchId)
                seenAvailable = null
                if (current == null) return@collectLatest
                coroutineScope {
                    launch { for (request in refreshRequests) load() }
                    launch { listen(current.token) }
                    launch {
                        while (true) {
                            delay(POLL_MS)
                            if (!_state.value.live) refreshRequests.trySend(Unit)
                        }
                    }
                    refreshRequests.trySend(Unit)
                }
            }
        }
    }

    /** Pastga tortib yangilash. */
    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(refreshing = true) }
            load()
            _state.update { it.copy(refreshing = false) }
        }
    }

    fun setDuty(onDuty: Boolean) = act(DUTY) {
        val me = api.setDuty(DutyRequest(onDuty))
        _state.update { it.copy(me = me) }
    }

    /** "Yangi" ro'yxati filiali; qo'lda buyurtma bo'lsa — o'zgarmaydi (o'sha filialga qulf). */
    fun chooseBranch(id: String) {
        if (_state.value.branchLocked) return
        _state.update { it.copy(chosenBranchId = id) }
        viewModelScope.launch { prefs.setBranch(id) }
    }

    fun take(id: String) = act(id) { apply(api.take(id)) }

    fun release(id: String) = act(id) { apply(api.release(id)) }

    fun depart() = act(DEPART) { apply(api.depart()) }

    fun deliver(id: String) = act(id) { apply(api.deliver(id)) }

    fun returnOrder(id: String, reason: ReturnReason, note: String) =
        act(id) { apply(api.returnOrder(id, ReturnRequest(reason, note.trim()))) }

    fun consumeMessage() = _state.update { it.copy(message = null) }

    fun logout() {
        viewModelScope.launch { session.clear() }
    }

    // ───────────────────────── ichki ─────────────────────────

    private suspend fun listen(token: String) {
        socket.events(token).collect { event ->
            when (event) {
                CourierEvent.Connected, is CourierEvent.Changed -> refreshRequests.trySend(Unit)
                CourierEvent.Ready -> _state.update { it.copy(live = true) }
                CourierEvent.Disconnected -> _state.update { it.copy(live = false) }
                // token eskirgan yoki kuryer bloklangan — kirish ekraniga
                CourierEvent.AuthError -> session.clear()
            }
        }
    }

    private suspend fun load() {
        try {
            coroutineScope {
                val me = async { apiCall { api.me() } }
                val available = async { apiCall { api.available() } }
                val orders = async { apiCall { api.orders() } }
                val branches = if (_state.value.branches.isEmpty()) async { apiCall { api.branches() } } else null
                val loadedMe = me.await()
                val loadedAvailable = available.await()
                val loadedOrders = orders.await()
                val loadedBranches = branches?.await()?.associateBy { it.id }
                _state.update {
                    it.copy(
                        me = loadedMe,
                        available = loadedAvailable,
                        active = loadedOrders.active,
                        done = loadedOrders.done,
                        branches = loadedBranches ?: it.branches,
                        loaded = true,
                        loadError = null,
                    )
                }
                notifyNew(loadedAvailable, loadedMe)
            }
        } catch (e: AppError) {
            _state.update { it.copy(loadError = e) }
        }
    }

    private fun apply(orders: CourierOrders) = _state.update { it.copy(active = orders.active, done = orders.done) }

    /** Buyruq: bitta vaqtda bittasi; xato bo'lsa — matn, holat eskirgan bo'lishi mumkin bo'lsa — ro'yxat yangilanadi. */
    private fun act(key: String, block: suspend () -> Unit) {
        if (_state.value.busy != null) return
        _state.update { it.copy(busy = key, message = null) }
        viewModelScope.launch {
            try {
                apiCall { block() }
            } catch (e: AppError) {
                _state.update { it.copy(message = courierErrorRes(e)) }
            } finally {
                _state.update { it.copy(busy = null) }
                refreshRequests.trySend(Unit)
            }
        }
    }

    /**
     * Ilova fonda (lekin ochiq) bo'lsa, "Ishdaman" yoqilgan va olsa bo'ladigan yangi buyurtma paydo bo'lsa — xabar;
     * filial tanlangan bo'lsa — faqat o'sha filialdan.
     */
    private fun notifyNew(available: List<AvailableOrder>, me: CourierState) {
        val previous = seenAvailable
        seenAvailable = (previous ?: emptySet()) + available.map { it.id }
        if (previous == null || !me.onDuty) return
        if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) return
        val branchId = _state.value.pickBranchId
        available.filter { it.id !in previous && it.blocked == null && (branchId == null || it.branchId == branchId) }.take(3).forEach { order ->
            val branch = _state.value.branch(order.branchId)?.name.orEmpty()
            val distance = order.distanceKm?.let { " · ~${Format.km(it)} km" }.orEmpty()
            Notifier.newOrder(
                context,
                order.id,
                context.getString(R.string.courier_notif_title, Format.orderNumber(order.number)),
                "$branch$distance · ${order.address.ifBlank { context.getString(R.string.courier_address_pin) }}",
            )
        }
    }

    companion object {
        const val DEPART = "depart"
        const val DUTY = "duty"
        private const val POLL_MS = 20_000L
    }
}
