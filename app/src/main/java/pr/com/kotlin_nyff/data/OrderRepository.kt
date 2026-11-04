package pr.com.kotlin_nyff.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pr.com.kotlin_nyff.data.local.SavedOrderDao
import pr.com.kotlin_nyff.data.local.SavedOrderEntity
import pr.com.kotlin_nyff.data.model.CustomerOrder
import pr.com.kotlin_nyff.data.model.MobileOrderRequest
import pr.com.kotlin_nyff.data.remote.AppError
import pr.com.kotlin_nyff.data.remote.NyffApi
import pr.com.kotlin_nyff.data.remote.NyffJson
import pr.com.kotlin_nyff.data.remote.OrderSocket
import pr.com.kotlin_nyff.data.remote.SocketEvent
import pr.com.kotlin_nyff.data.remote.apiCall
import pr.com.kotlin_nyff.di.ApplicationScope
import pr.com.kotlin_nyff.domain.Format
import javax.inject.Inject
import javax.inject.Singleton

/** Telefonda saqlangan buyurtma: oxirgi holat + kuzatish tokeni. */
data class SavedOrder(
    val order: CustomerOrder,
    val token: String,
    val deliveryFee: Long?,
    val createdAt: Long,
)

@Singleton
class OrderRepository @Inject constructor(
    private val api: NyffApi,
    private val dao: SavedOrderDao,
    private val socket: OrderSocket,
    @param:ApplicationScope private val appScope: CoroutineScope,
) {

    /** Eng yangi [limit] ta buyurtma, yangisi birinchi (tarix o'chirilmaydi — "Buyurtmalarim" sahifalab o'qiydi). */
    fun recent(limit: Int): Flow<List<SavedOrder>> = dao.observeRecent(limit).map { list -> list.mapNotNull { it.toSavedOrder() } }

    /** Telefondagi hamma buyurtmalar soni — yana sahifa bormi. */
    val count: Flow<Int> = dao.observeCount()

    fun observe(id: String): Flow<SavedOrder?> = dao.observe(id).map { it?.toSavedOrder() }

    /**
     * Buyurtma berish. So'rov va saqlash ilova doirasida bajariladi: foydalanuvchi ekrandan chiqib ketsa ham javob
     * kelsa token yo'qolmaydi. Avtomatik qayta yuborilmaydi (idempotent emas — ikki buyurtma bo'lishi mumkin).
     */
    suspend fun place(request: MobileOrderRequest, deliveryFee: Long?): SavedOrder = appScope.async {
        val response = apiCall { api.placeOrder(request) }
        val now = System.currentTimeMillis()
        val entity = SavedOrderEntity(
            id = response.order.id,
            token = response.trackingToken,
            createdAt = Format.parse(response.order.createdAt)?.toEpochMilli() ?: now,
            deliveryFee = deliveryFee,
            snapshot = NyffJson.encodeToString(CustomerOrder.serializer(), response.order),
            updatedAt = now,
        )
        dao.insert(entity)
        SavedOrder(response.order, entity.token, deliveryFee, entity.createdAt)
    }.await()

    /** Serverdan joriy holat (REST) va telefonga yozish. */
    suspend fun refresh(id: String): CustomerOrder {
        val saved = dao.get(id) ?: throw AppError.Server(status = 404, code = "not_found", detail = "local")
        val order = apiCall { api.order(id, saved.token) }
        save(order)
        return order
    }

    /** Socket'dan kelgan yangi holatni yozish. */
    suspend fun save(order: CustomerOrder) {
        dao.updateSnapshot(order.id, NyffJson.encodeToString(CustomerOrder.serializer(), order), System.currentTimeMillis())
    }

    fun events(saved: SavedOrder): Flow<SocketEvent> = socket.events(saved.order.id, saved.token)

    private fun SavedOrderEntity.toSavedOrder(): SavedOrder? = runCatching {
        SavedOrder(NyffJson.decodeFromString(CustomerOrder.serializer(), snapshot), token, deliveryFee, createdAt)
    }.getOrNull()
}
