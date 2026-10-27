package pr.com.kotlin_nyff.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pr.com.kotlin_nyff.data.local.SavedDraft
import pr.com.kotlin_nyff.data.local.UserPrefs
import pr.com.kotlin_nyff.data.model.DeliveryRoute
import pr.com.kotlin_nyff.data.model.GeoPoint
import pr.com.kotlin_nyff.di.ApplicationScope
import pr.com.kotlin_nyff.domain.Cart
import pr.com.kotlin_nyff.domain.CartLine
import pr.com.kotlin_nyff.domain.OrderMode
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Rasmiylashtirilayotgan buyurtma: tur, filial / dostavka nuqtasi va savat. Bosh sahifa, xarita va rasmiylashtirish
 * ekranlari uchun umumiy. Tur, olib ketish filiali va nuqta telefonda saqlanadi (keyingi ochilishda qayta tanlanmasin);
 * savat — faqat xotirada.
 */
@Singleton
class DraftStore @Inject constructor(
    private val prefs: UserPrefs,
    @param:ApplicationScope private val scope: CoroutineScope,
) {

    private val _mode = MutableStateFlow(OrderMode.DELIVERY)
    val mode: StateFlow<OrderMode> = _mode.asStateFlow()

    private val _takeawayBranchId = MutableStateFlow<String?>(null)
    val takeawayBranchId: StateFlow<String?> = _takeawayBranchId.asStateFlow()

    private val _deliveryPoint = MutableStateFlow<GeoPoint?>(null)
    val deliveryPoint: StateFlow<GeoPoint?> = _deliveryPoint.asStateFlow()

    /** [deliveryPoint] uchun server javobi; `null` — hali so'ralmagan yoki so'rov muvaffaqiyatsiz. */
    private val _route = MutableStateFlow<DeliveryRoute?>(null)
    val route: StateFlow<DeliveryRoute?> = _route.asStateFlow()

    private val _lines = MutableStateFlow<List<CartLine>>(emptyList())
    val lines: StateFlow<List<CartLine>> = _lines.asStateFlow()

    /** Saqlangan tanlov yuklanguncha foydalanuvchi biror narsani o'zgartirsa — saqlangani uni bosib ketmasin. */
    @Volatile
    private var touched = false

    init {
        scope.launch {
            val saved = prefs.draft.first()
            if (touched) return@launch
            _mode.value = saved.mode
            _takeawayBranchId.value = saved.takeawayBranchId
            _deliveryPoint.value = saved.deliveryPoint
        }
    }

    fun setMode(mode: OrderMode) {
        _mode.value = mode
        persist()
    }

    fun setTakeawayBranch(branchId: String) {
        _takeawayBranchId.value = branchId
        persist()
    }

    fun setDelivery(point: GeoPoint, route: DeliveryRoute?) {
        // avval javob, keyin nuqta: nuqtani kuzatayotgan ekran yangi nuqtani eski javob bilan ko'rmasin
        _route.value = route
        _deliveryPoint.value = point
        persist()
    }

    /** Shu nuqta uchun yangilangan javob (filiallar holati o'zgargan bo'lishi mumkin). */
    fun updateRoute(point: GeoPoint, route: DeliveryRoute?) {
        if (_deliveryPoint.value == point) _route.value = route
    }

    fun add(productId: String, qty: Int = 1, note: String = "") = _lines.update { Cart.add(it, productId, qty, note) }

    fun changeQty(key: String, delta: Int) = _lines.update { Cart.changeQty(it, key, delta) }

    fun remove(key: String) = _lines.update { Cart.remove(it, key) }

    fun clearCart() {
        _lines.value = emptyList()
    }

    private fun persist() {
        touched = true
        val draft = SavedDraft(_mode.value, _takeawayBranchId.value, _deliveryPoint.value)
        scope.launch { prefs.saveDraft(draft) }
    }
}
