package pr.com.kotlin_nyff.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import pr.com.kotlin_nyff.data.model.Branch
import pr.com.kotlin_nyff.data.model.DeliveryRoute
import pr.com.kotlin_nyff.data.model.GeoPoint
import pr.com.kotlin_nyff.data.model.Menu
import pr.com.kotlin_nyff.data.remote.NyffApi
import pr.com.kotlin_nyff.data.remote.apiCall
import javax.inject.Inject
import javax.inject.Singleton

/** Filiallar, yetkazib berish yo'nalishi va menyu. Hamma xato — [pr.com.kotlin_nyff.data.remote.AppError]. */
@Singleton
class CatalogRepository @Inject constructor(private val api: NyffApi) {

    private val _branches = MutableStateFlow<List<Branch>?>(null)

    /** Oxirgi olingan filiallar (`onlineState` — o'sha paytdagi surat); hali olinmagan bo'lsa `null`. */
    val branches: StateFlow<List<Branch>?> = _branches.asStateFlow()

    private val _menus = MutableStateFlow<Map<String, Menu>>(emptyMap())

    /** Filial bo'yicha olingan menyular (narxlar filialga bog'liq). */
    val menus: StateFlow<Map<String, Menu>> = _menus.asStateFlow()

    suspend fun refreshBranches(): List<Branch> =
        apiCall { api.branches() }.also { _branches.value = it }

    suspend fun route(point: GeoPoint): DeliveryRoute =
        apiCall { api.deliveryRoute(point.lat, point.lng) }

    /** Keshdan; `force` — serverdan qayta (narx / "sotuvda bor" o'zgargan bo'lishi mumkin). */
    suspend fun menu(branchId: String, force: Boolean = false): Menu {
        if (!force) _menus.value[branchId]?.let { return it }
        val menu = apiCall { api.menu(branchId) }
        _menus.update { it + (branchId to menu) }
        return menu
    }

    fun branch(id: String?): Branch? = _branches.value?.firstOrNull { it.id == id }
}
