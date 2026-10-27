package pr.com.kotlin_nyff.courier.domain

import pr.com.kotlin_nyff.courier.data.CourierOrder
import pr.com.kotlin_nyff.data.model.OnlinePayment

/**
 * Kuryer puli (kelishilgan, 2026-10-27): naqd buyurtmani kuryer filialdan olayotganda **kassaga o'zidan to'laydi**, keyin
 * mijozdan oladi; Click — to'langan. Yetkazish narxi summaga kirmaydi — mijoz kuryerga alohida beradi (hudud narxi yoki,
 * hududdan tashqarida, kuryer aytgan narx).
 */
object CourierMoney {

    /** Kassaga to'lanadigan naqd (taomlar summasi). */
    fun kassa(order: CourierOrder): Long = if (order.payment == OnlinePayment.CASH) order.total else 0

    fun kassa(orders: List<CourierOrder>): Long = orders.sumOf { kassa(it) }

    /** Mijozdan olinadigan: naqd taomlar + ma'lum hudud narxi (noma'lum bo'lsa — kuryer o'zi aytadi). */
    fun collect(order: CourierOrder): Long = kassa(order) + (order.deliveryFee ?: 0)

    /** Bugungi natija: yetkazilgan naqd buyurtmalar summasi va yetkazish narxlari (ma'lumlari). */
    data class Today(val delivered: Int, val returned: Int, val cash: Long, val fees: Long)

    fun today(done: List<CourierOrder>): Today {
        val delivered = done.filter { it.deliveredAt != null }
        return Today(
            delivered = delivered.size,
            returned = done.count { it.returnedAt != null },
            cash = delivered.sumOf { kassa(it) },
            fees = delivered.sumOf { it.deliveryFee ?: 0 },
        )
    }
}
