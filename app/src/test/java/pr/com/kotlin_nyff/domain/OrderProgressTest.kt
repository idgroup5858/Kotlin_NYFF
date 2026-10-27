package pr.com.kotlin_nyff.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pr.com.kotlin_nyff.data.model.CancelReason
import pr.com.kotlin_nyff.data.model.CustomerOrder
import pr.com.kotlin_nyff.data.model.OrderStatus
import pr.com.kotlin_nyff.data.model.OrderType
import java.time.Instant

/** API.md 5.6 jadvali: holat + tur + sabab + kuryer bosqichi → matn va bosqich. */
class OrderProgressTest {

    private fun order(
        status: OrderStatus,
        type: OrderType = OrderType.DELIVERY,
        reason: CancelReason? = null,
        routedAt: String? = "2026-10-27T05:00:00.000Z",
        readyAt: String? = null,
        createdAt: String = "2026-10-27T04:59:00.000Z",
        departedAt: String? = null,
        deliveredAt: String? = null,
        returnedAt: String? = null,
    ) = CustomerOrder(
        id = "ord_1", branchId = "br_chilonzor", number = 17, type = type, total = 32_000, status = status,
        createdAt = createdAt, cancelReason = reason, routedAt = routedAt, readyAt = readyAt,
        departedAt = departedAt, deliveredAt = deliveredAt, returnedAt = returnedAt,
    )

    @Test
    fun `asosiy holatlar`() {
        assertEquals(OrderHeadline.WAITING, OrderProgress.headline(order(OrderStatus.PENDING)))
        assertEquals(OrderHeadline.COOKING, OrderProgress.headline(order(OrderStatus.PREPARING)))
        assertEquals(OrderHeadline.READY_DELIVERY, OrderProgress.headline(order(OrderStatus.READY)))
        assertEquals(OrderHeadline.READY_PICKUP, OrderProgress.headline(order(OrderStatus.READY, OrderType.TAKEAWAY)))
    }

    @Test
    fun `kuryer bosqichlari — status ready da qoladi`() {
        val t = "2026-10-27T05:20:00.000Z"
        assertEquals(OrderHeadline.ON_THE_WAY, OrderProgress.headline(order(OrderStatus.READY, departedAt = t)))
        assertEquals(OrderHeadline.DELIVERED, OrderProgress.headline(order(OrderStatus.READY, departedAt = t, deliveredAt = t)))
        assertEquals(OrderHeadline.RETURNED, OrderProgress.headline(order(OrderStatus.READY, departedAt = t, returnedAt = t)))
    }

    @Test
    fun `bekor qilinish sabablari`() {
        val delivery = OrderType.DELIVERY
        val pickup = OrderType.TAKEAWAY
        assertEquals(OrderHeadline.CANCELLED, OrderProgress.headline(order(OrderStatus.CANCELLED, delivery, null)))
        assertEquals(OrderHeadline.CANCELLED_TIMEOUT, OrderProgress.headline(order(OrderStatus.CANCELLED, delivery, CancelReason.TIMEOUT)))
        assertEquals(OrderHeadline.CANCELLED_TIMEOUT_PICKUP, OrderProgress.headline(order(OrderStatus.CANCELLED, pickup, CancelReason.TIMEOUT)))
        assertEquals(OrderHeadline.CANCELLED_BRANCH_CLOSED, OrderProgress.headline(order(OrderStatus.CANCELLED, delivery, CancelReason.BRANCH_CLOSED)))
        assertEquals(
            OrderHeadline.CANCELLED_BRANCH_CLOSED_PICKUP,
            OrderProgress.headline(order(OrderStatus.CANCELLED, pickup, CancelReason.BRANCH_CLOSED)),
        )
        assertEquals(OrderHeadline.CANCELLED_NO_BRANCH, OrderProgress.headline(order(OrderStatus.CANCELLED, delivery, CancelReason.NO_BRANCH)))
    }

    @Test
    fun `bosqichlar — dostavkada 4 ta, olib ketishda 3 ta`() {
        val t = "2026-10-27T05:20:00.000Z"
        assertEquals(4, OrderProgress.steps(OrderType.DELIVERY).size)
        assertEquals(3, OrderProgress.steps(OrderType.TAKEAWAY).size)
        assertEquals(0, OrderProgress.step(order(OrderStatus.PENDING)))
        assertEquals(1, OrderProgress.step(order(OrderStatus.PREPARING)))
        // dostavka tayyor, kuryer yo'lga chiqmagan — hali 2-bosqichda ("Tayyor · kuryer olib ketadi")
        assertEquals(1, OrderProgress.step(order(OrderStatus.READY)))
        assertEquals(2, OrderProgress.step(order(OrderStatus.READY, departedAt = t)))
        assertEquals(3, OrderProgress.step(order(OrderStatus.READY, departedAt = t, deliveredAt = t)))
        assertEquals(2, OrderProgress.step(order(OrderStatus.READY, OrderType.TAKEAWAY)))

        assertFalse(OrderProgress.completed(order(OrderStatus.READY, departedAt = t)))
        assertTrue(OrderProgress.completed(order(OrderStatus.READY, departedAt = t, deliveredAt = t)))
        assertTrue(OrderProgress.completed(order(OrderStatus.READY, OrderType.TAKEAWAY)))
    }

    @Test
    fun `qabul muddati — routedAt dan 5 daqiqa, faqat pending da`() {
        assertEquals(Instant.parse("2026-10-27T05:05:00Z"), OrderProgress.acceptDeadline(order(OrderStatus.PENDING)))
        assertNull(OrderProgress.acceptDeadline(order(OrderStatus.PREPARING)))
        // routedAt bo'lmasa — createdAt dan
        assertEquals(Instant.parse("2026-10-27T05:04:00Z"), OrderProgress.acceptDeadline(order(OrderStatus.PENDING, routedAt = null)))
    }

    @Test
    fun `bosh sahifa banneri — ishdagi, yetkazilmagan dostavka yoki 1 soat ichida tugagan`() {
        val now = Instant.parse("2026-10-27T06:00:00Z")
        assertTrue(OrderProgress.isCurrent(order(OrderStatus.PENDING), now))
        // olib ketish — tayyor bo'lganidan 1 soat
        assertTrue(OrderProgress.isCurrent(order(OrderStatus.READY, OrderType.TAKEAWAY, readyAt = "2026-10-27T05:30:00Z"), now))
        assertFalse(OrderProgress.isCurrent(order(OrderStatus.READY, OrderType.TAKEAWAY, readyAt = "2026-10-27T04:30:00Z"), now))
        // dostavka yetkazilguncha (kuryer kutilmoqda / yo'lda / qaytdi), lekin 12 soatdan eski emas
        assertTrue(OrderProgress.isCurrent(order(OrderStatus.READY, readyAt = "2026-10-27T04:30:00Z"), now))
        assertFalse(OrderProgress.isCurrent(order(OrderStatus.READY, createdAt = "2026-10-26T10:00:00Z"), now))
        // yetkazilgandan 1 soat
        assertTrue(OrderProgress.isCurrent(order(OrderStatus.READY, deliveredAt = "2026-10-27T05:30:00Z"), now))
        assertFalse(OrderProgress.isCurrent(order(OrderStatus.READY, deliveredAt = "2026-10-27T04:30:00Z"), now))
        assertFalse(OrderProgress.isCurrent(order(OrderStatus.CANCELLED), now))

        assertFalse(OrderProgress.isLive(order(OrderStatus.CANCELLED)))
        assertFalse(OrderProgress.isLive(order(OrderStatus.READY, deliveredAt = "2026-10-27T05:30:00Z")))
        assertTrue(OrderProgress.isLive(order(OrderStatus.READY, departedAt = "2026-10-27T05:30:00Z")))
    }
}
