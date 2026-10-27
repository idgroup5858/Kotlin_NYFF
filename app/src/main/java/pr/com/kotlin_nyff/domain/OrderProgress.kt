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
    /** Dostavka tayyor — kuryer olib ketadi. */
    READY_DELIVERY,
    READY_PICKUP,
    ON_THE_WAY,
    DELIVERED,
    /** Kuryer yetkaza olmadi — filial bog'lanadi. */
    RETURNED,
    /** Filial (kassir / admin) bekor qildi. */
    CANCELLED,
    CANCELLED_TIMEOUT,
    CANCELLED_TIMEOUT_PICKUP,
    CANCELLED_BRANCH_CLOSED,
    CANCELLED_BRANCH_CLOSED_PICKUP,
    CANCELLED_NO_BRANCH,
}

/** Kuzatish ekranidagi bosqich. Dostavkada 4 ta (… → kuryer yo'lda → yetkazildi), olib ketishda 3 ta. */
enum class TrackingStep { PLACED, COOKING, READY_PICKUP, ON_THE_WAY, DELIVERED }

object OrderProgress {

    /** Mobil buyurtma filialga tushganidan keyin qabul qilinishi kerak bo'lgan muddat (server fon ishi). */
    val ACCEPT_WINDOW: Duration = Duration.ofMinutes(5)

    /** Tugagandan (tayyor / yetkazildi) keyin bosh sahifadagi "Buyurtmam" bannerida qancha turadi. */
    private val FINISHED_VISIBLE: Duration = Duration.ofHours(1)

    fun headline(order: CustomerOrder): OrderHeadline {
        val pickup = order.type != OrderType.DELIVERY
        return when (order.status) {
            OrderStatus.PENDING, OrderStatus.UNKNOWN -> OrderHeadline.WAITING
            OrderStatus.PREPARING -> OrderHeadline.COOKING
            OrderStatus.READY -> when {
                pickup -> OrderHeadline.READY_PICKUP
                order.deliveredAt != null -> OrderHeadline.DELIVERED
                order.returnedAt != null -> OrderHeadline.RETURNED
                order.departedAt != null -> OrderHeadline.ON_THE_WAY
                else -> OrderHeadline.READY_DELIVERY
            }
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

    fun steps(type: OrderType): List<TrackingStep> = if (type == OrderType.DELIVERY) {
        listOf(TrackingStep.PLACED, TrackingStep.COOKING, TrackingStep.ON_THE_WAY, TrackingStep.DELIVERED)
    } else {
        listOf(TrackingStep.PLACED, TrackingStep.COOKING, TrackingStep.READY_PICKUP)
    }

    /**
     * Joriy bosqich indeksi [steps] ro'yxatida. Dostavka tayyor, lekin kuryer hali yo'lga chiqmagan — "tayyorlanmoqda"
     * bosqichida (sarlavhasi "Tayyor"); yetkazildi / olib ketishga tayyor — oxirgi bosqich bajarilgan.
     */
    fun step(order: CustomerOrder): Int = when (order.status) {
        OrderStatus.PREPARING -> 1
        OrderStatus.READY -> when {
            order.type != OrderType.DELIVERY -> 2
            order.deliveredAt != null -> 3
            order.departedAt != null -> 2
            else -> 1
        }
        else -> 0
    }

    /** Oxirgi bosqich ham bajarildi (olib ketishga tayyor / yetkazildi). */
    fun completed(order: CustomerOrder): Boolean =
        order.status == OrderStatus.READY && (order.type != OrderType.DELIVERY || order.deliveredAt != null)

    /** Qabul muddati tugaydigan vaqt (`routedAt` + 5 daqiqa); faqat `pending` da. */
    fun acceptDeadline(order: CustomerOrder): Instant? {
        if (order.status != OrderStatus.PENDING) return null
        val routed = Format.parse(order.routedAt) ?: Format.parse(order.createdAt) ?: return null
        return routed.plus(ACCEPT_WINDOW)
    }

    /** Server holatni yana o'zgartirishi mumkinmi (bekor qilingan va yetkazilgan — yakuniy). */
    fun isLive(order: CustomerOrder): Boolean =
        order.status != OrderStatus.CANCELLED && order.deliveredAt == null

    /**
     * Yetkazilmagan dostavka (kuryer kutilmoqda / yo'lda / qaytdi) shuncha vaqt "joriy" — server kuryerlarga ham faqat
     * 12 soat ichidagilarni ko'rsatadi (kuryer tizimidan oldingi eski buyurtmalar bannerda qolib ketmasin).
     */
    private val OPEN_DELIVERY_VISIBLE: Duration = Duration.ofHours(12)

    /** Bosh sahifadagi "Buyurtmam" banneri uchun: ishdagi, yo'ldagi, qaytgan yoki yaqinda tugagan. */
    fun isCurrent(order: CustomerOrder, now: Instant = Instant.now()): Boolean {
        fun within(iso: String?, window: Duration) = Format.parse(iso)?.let { Duration.between(it, now) < window } ?: false
        return when (order.status) {
            OrderStatus.PENDING, OrderStatus.PREPARING -> true
            OrderStatus.READY -> when {
                order.type == OrderType.DELIVERY && order.deliveredAt == null -> within(order.createdAt, OPEN_DELIVERY_VISIBLE)
                else -> within(order.deliveredAt ?: order.readyAt, FINISHED_VISIBLE)
            }
            else -> false
        }
    }
}
