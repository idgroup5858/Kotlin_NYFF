package pr.com.kotlin_nyff.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pr.com.kotlin_nyff.data.CatalogRepository
import pr.com.kotlin_nyff.data.OrderRepository
import pr.com.kotlin_nyff.data.SavedOrder
import pr.com.kotlin_nyff.data.model.Branch
import pr.com.kotlin_nyff.data.model.OrderStatus
import pr.com.kotlin_nyff.data.remote.AppError
import java.time.Duration
import javax.inject.Inject

data class OrdersUiState(
    /** `null` — telefondagi baza hali o'qilmagan. */
    val orders: List<SavedOrder>? = null,
    val branches: List<Branch> = emptyList(),
    val refreshing: Boolean = false,
) {
    fun branchName(id: String): String? = branches.firstOrNull { it.id == id }?.name
}

/** "Buyurtmalarim": telefonda saqlangan hamma buyurtmalar (o'chirilmaydi), yangisi birinchi. */
@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val orders: OrderRepository,
    private val catalog: CatalogRepository,
) : ViewModel() {

    private val refreshing = MutableStateFlow(false)

    val state: StateFlow<OrdersUiState> = combine(orders.history, catalog.branches, refreshing) { history, branches, busy ->
        OrdersUiState(history, branches.orEmpty(), busy)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OrdersUiState())

    init {
        refresh()
    }

    /** Holati hali o'zgarishi mumkin bo'lgan (oxirgi 2 kundagi, bekor qilinmagan) buyurtmalar serverdan. */
    fun refresh() {
        if (refreshing.value) return
        viewModelScope.launch {
            refreshing.value = true
            if (catalog.branches.value == null) {
                try {
                    catalog.refreshBranches()
                } catch (_: AppError) {
                    // nomlarsiz ham ro'yxat ko'rinadi
                }
            }
            val since = System.currentTimeMillis() - RECENT.toMillis()
            orders.history.first()
                .filter { it.createdAt >= since && it.order.status != OrderStatus.CANCELLED }
                .take(MAX_REFRESH)
                .map { saved ->
                    async {
                        try {
                            orders.refresh(saved.order.id)
                        } catch (_: AppError) {
                            // oxirgi saqlangan holat ko'rinadi
                        }
                    }
                }
                .awaitAll()
            refreshing.value = false
        }
    }

    private companion object {
        val RECENT: Duration = Duration.ofDays(2)
        const val MAX_REFRESH = 10
    }
}
