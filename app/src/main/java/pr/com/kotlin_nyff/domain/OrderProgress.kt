package pr.com.kotlin_nyff.domain

import pr.com.kotlin_nyff.data.model.CancelReason
import pr.com.kotlin_nyff.data.model.CustomerOrder
import pr.com.kotlin_nyff.data.model.OrderStatus
import pr.com.kotlin_nyff.data.model.OrderType
import java.time.Duration
import java.time.Instant

/** Buyurtma holati → ilovadagi asosiy matn (API.md 5.6). Matnning o'zi — `ui/common/Texts.kt`. */
enum class OrderHeadline {
    WAITING,
    COOKING,
    READY_DELIVERY,
    READY_PICKUP,
    /** Filial (kassir / admin) bekor qildi. */
    CANCELLED,
    CANCELLED_TIMEOUT,
    CANCELLED_TIMEOUT_PICKUP,
    CANCELLED_BRANCH_CLOSED,
    CANCELLED_BRANCH_CLOSED_PICKUP,
    CANCELLED_NO_BRANCH,
}

object OrderProgress {

    /** Mobil buyurtma filialga tushganidan keyin qabul qilinishi kerak bo'lgan muddat (server fon ishi). */
    val ACCEPT_WINDOW: Duration = Duration.ofMinutes(5)

    /** "Tayyor" bo'lgandan keyin bosh sahifadagi "Buyurtmam" bannerida qancha turadi. */
    private val READY_VISIBLE: Duration = Duration.ofHours(1)

    fun headline(order: CustomerOrder): OrderHeadline {
        val pickup = order.type != OrderType.DELIVERY
        return when (order.status) {
            OrderStatus.PENDING, OrderStatus.UNKNOWN -> OrderHeadline.WAITING
            OrderStatus.PREPARING -> OrderHeadline.COOKING
            OrderStatus.READY -> if (pickup) OrderHeadline.READY_PICKUP else OrderHeadline.READY_DELIVERY
            OrderStatus.CANCELLED -> when (order.cancelReason) {
                null -> OrderHeadline.CANCELLED
                CancelReason.TIMEOUT ->
                    if (pickup) OrderHeadline.CANCELLED_TIMEOUT_PICKUP else OrderHeadline.CANCELLED_TIMEOUT
                CancelReason.BRANCH_CLOSED ->
                    if (pickup) OrderHeadline.CANCELLED_BRANCH_CLOSED_PICKUP else OrderHeadline.CANCELLED_BRANCH_CLOSED
                CancelReason.NO_BRANCH -> OrderHeadline.CANCELLED_NO_BRANCH
            }
        }
    }

    /** Bosqichlar: 0 — qabul kutilmoqda, 1 — tayyorlanmoqda, 2 — tayyor. */
    fun step(status: OrderStatus): Int = when (status) {
        OrderStatus.PREPARING -> 1
        OrderStatus.READY -> 2
        else -> 0
    }

    /** Qabul muddati tugaydigan vaqt (`routedAt` + 5 daqiqa); faqat `pending` da. */
    fun acceptDeadline(order: CustomerOrder): Instant? {
        if (order.status != OrderStatus.PENDING) return null
        val routed = Format.parse(order.routedAt) ?: Format.parse(order.createdAt) ?: return null
        return routed.plus(ACCEPT_WINDOW)
    }

    /** Server holatini yana o'zgartirishi mumkinmi (bekor qilingan — yakuniy). */
    fun isLive(order: CustomerOrder): Boolean = order.status != OrderStatus.CANCELLED

    /** Bosh sahifadagi "Buyurtmam" banneri uchun: ishdagi yoki yaqinda tayyor bo'lgan. */
    fun isCurrent(order: CustomerOrder, now: Instant = Instant.now()): Boolean = when (order.status) {
        OrderStatus.PENDING, OrderStatus.PREPARING -> true
        OrderStatus.READY -> Format.parse(order.readyAt)?.let { Duration.between(it, now) < READY_VISIBLE } ?: false
        else -> false
    }
}
