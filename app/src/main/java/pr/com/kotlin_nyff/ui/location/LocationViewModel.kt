package pr.com.kotlin_nyff.ui.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pr.com.kotlin_nyff.data.CatalogRepository
import pr.com.kotlin_nyff.data.DraftStore
import pr.com.kotlin_nyff.data.model.DeliveryRoute
import pr.com.kotlin_nyff.data.model.GeoPoint
import pr.com.kotlin_nyff.data.remote.AppError
import javax.inject.Inject
import kotlin.math.roundToLong

data class LocationUiState(
    /** Pin turgan nuqta; xarita surilayotganda `null`. */
    val point: GeoPoint? = null,
    val route: DeliveryRoute? = null,
    val loading: Boolean = false,
    val error: AppError? = null,
)

/** Dostavka nuqtasi: xarita to'xtaganda server yo'nalishini so'raydi (`/delivery/route`), "Shu joyni tanlash" — saqlaydi. */
@HiltViewModel
class LocationViewModel @Inject constructor(
    private val catalog: CatalogRepository,
    private val draft: DraftStore,
) : ViewModel() {

    /** Oldin tanlangan nuqta — xarita shu yerdan ochiladi. */
    val initialPoint: GeoPoint? = draft.deliveryPoint.value

    private val _state = MutableStateFlow(LocationUiState())
    val state: StateFlow<LocationUiState> = _state.asStateFlow()

    private var routeJob: Job? = null

    fun onMoving() {
        routeJob?.cancel()
        _state.value = LocationUiState(loading = true)
    }

    fun onSettled(lat: Double, lng: Double) {
        val point = GeoPoint(round6(lat), round6(lng))
        if (_state.value.point == point && (_state.value.route != null || _state.value.loading)) return
        routeJob?.cancel()
        _state.value = LocationUiState(point = point, loading = true)
        routeJob = viewModelScope.launch {
            delay(250)
            try {
                val route = catalog.route(point)
                _state.update { if (it.point == point) it.copy(route = route, loading = false) else it }
            } catch (e: AppError) {
                _state.update { if (it.point == point) it.copy(error = e, loading = false) else it }
            }
        }
    }

    fun retry() {
        val point = _state.value.point ?: return
        _state.value = LocationUiState(point = point)
        onSettled(point.lat, point.lng)
    }

    /** Nuqta va javob saqlanadi; javob hali kelmagan bo'lsa — `false`. */
    fun confirm(): Boolean {
        val current = _state.value
        val point = current.point ?: return false
        val route = current.route ?: return false
        draft.setDelivery(point, route)
        return true
    }

    /** Server ham 6 xonagacha yaxlitlaydi. */
    private fun round6(value: Double): Double = (value * 1_000_000).roundToLong() / 1_000_000.0
}
