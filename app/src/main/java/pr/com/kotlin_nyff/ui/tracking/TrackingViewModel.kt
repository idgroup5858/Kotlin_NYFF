package pr.com.kotlin_nyff.ui.tracking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pr.com.kotlin_nyff.data.CatalogRepository
import pr.com.kotlin_nyff.data.OrderRepository
import pr.com.kotlin_nyff.data.SavedOrder
import pr.com.kotlin_nyff.data.model.Branch
import pr.com.kotlin_nyff.data.remote.AppError
import pr.com.kotlin_nyff.data.remote.SocketEvent
import pr.com.kotlin_nyff.domain.OrderProgress
import pr.com.kotlin_nyff.ui.TrackingRoute
import javax.inject.Inject

data class TrackingUiState(
    val saved: SavedOrder? = null,
    val branches: List<Branch> = emptyList(),
    /** Socket ulangan va xonaga obuna bo'lingan — holat darhol keladi. */
    val live: Boolean = false,
    /** Oxirgi REST so'rovi tarmoq xatosi bilan tugadi. */
    val offline: Boolean = false,
    /** Server buyurtmani tanimadi (token / baza tozalangan). */
    val notFound: Boolean = false,
) {
    fun branch(id: String?): Branch? = branches.firstOrNull { it.id == id }
}

/**
 * Buyurtma holati (API.md 5.5): ekran ko'rinib turganda Socket.IO (`order:<id>`) tinglanadi, har ulanishda REST'dan
 * qayta olinadi, socket ulanmagan paytda har 12 soniyada so'rov (zaxira). Har yangi holat telefonga yoziladi.
 */
@HiltViewModel
class TrackingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val orders: OrderRepository,
    private val catalog: CatalogRepository,
) : ViewModel() {

    private val orderId = savedStateHandle.toRoute<TrackingRoute>().orderId

    private data class Connection(val live: Boolean = false, val offline: Boolean = false, val notFound: Boolean = false)

    private val connection = MutableStateFlow(Connection())

    val state: StateFlow<TrackingUiState> = combine(orders.observe(orderId), catalog.branches, connection) { saved, branches, c ->
        TrackingUiState(saved, branches.orEmpty(), c.live, c.offline, c.notFound)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TrackingUiState())

    private var liveJob: Job? = null

    init {
        if (catalog.branches.value == null) {
            viewModelScope.launch {
                try {
                    catalog.refreshBranches()
                } catch (_: AppError) {
                    // filial nomi bo'lmasa ham holat ko'rinadi
                }
            }
        }
    }

    /** Ekran oldingi planga chiqdi: REST + socket + zaxira so'rovlar. */
    fun start() {
        if (liveJob?.isActive == true) return
        liveJob = viewModelScope.launch {
            refresh()
            val saved = orders.observe(orderId).filterNotNull().first()
            if (!OrderProgress.isLive(saved.order)) return@launch
            launch { listen(saved) }
            while (true) {
                delay(POLL_MS)
                if (!connection.value.live) refresh()
            }
        }
    }

    /** Ekran yopildi / fonga ketdi — socket uziladi (qaytganda [start] yana REST'dan oladi). */
    fun stop() {
        liveJob?.cancel()
        liveJob = null
        connection.update { it.copy(live = false) }
    }

    private suspend fun listen(saved: SavedOrder) {
        orders.events(saved).collect { event ->
            when (event) {
                SocketEvent.Connected -> refresh()
                SocketEvent.Ready -> connection.update { it.copy(live = true, offline = false) }
                SocketEvent.Disconnected, SocketEvent.AuthError -> connection.update { it.copy(live = false) }
                is SocketEvent.Order -> orders.save(event.order)
            }
        }
    }

    private suspend fun refresh() {
        try {
            orders.refresh(orderId)
            connection.update { it.copy(offline = false, notFound = false) }
        } catch (e: AppError) {
            connection.update {
                when {
                    e is AppError.Network -> it.copy(offline = true)
                    e.serverCode == "not_found" -> it.copy(notFound = true, offline = false)
                    else -> it
                }
            }
        }
    }

    private companion object {
        const val POLL_MS = 12_000L
    }
}
