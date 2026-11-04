package pr.com.kotlin_nyff.courier.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pr.com.kotlin_nyff.courier.data.CourierOrder
import pr.com.kotlin_nyff.data.model.GeoPoint
import pr.com.kotlin_nyff.data.model.OnlineInfo
import pr.com.kotlin_nyff.data.model.OnlinePayment
import pr.com.kotlin_nyff.domain.Format
import pr.com.kotlin_nyff.domain.NBSP

class CourierDomainTest {

    private val a = GeoPoint(41.285, 69.205)
    private val b = GeoPoint(41.26, 69.22)
    private val c = GeoPoint(41.295, 69.175)

    @Test
    fun `bitta manzil — Google Maps va Yandex Go havolasi`() {
        assertEquals("https://www.google.com/maps/dir/?api=1&destination=41.285,69.205&travelmode=driving", MapLinks.google(a))
        assertEquals(
            "yandextaxi://route?end-lat=41.285&end-lon=69.205&appmetrica_tracking_id=1178268795219780156",
            MapLinks.yandexGo(a),
        )
        assertEquals(
            "https://3.redirect.appmetrica.yandex.com/route?end-lat=41.285&end-lon=69.205&appmetrica_tracking_id=1178268795219780156",
            MapLinks.yandexGoWeb(a),
        )
    }

    @Test
    fun `butun yo'l — tartib bilan, joriy joydan boshlab`() {
        assertEquals(
            "https://www.google.com/maps/dir//41.285,69.205/41.26,69.22/41.295,69.175",
            MapLinks.googleRoute(listOf(a, b, c)),
        )
        assertEquals("https://www.google.com/maps/dir//41.285,69.205", MapLinks.googleRoute(listOf(a)))
        assertNull(MapLinks.googleRoute(emptyList()))
    }

    private fun order(payment: OnlinePayment, total: Long, fee: Long?, delivered: Boolean = true, returned: Boolean = false) = CourierOrder(
        id = "o$total$payment", branchId = "b", number = 1, total = total, createdAt = "2026-10-27T05:00:00Z",
        online = OnlineInfo(payment = payment), deliveryFee = fee,
        deliveredAt = if (delivered) "2026-10-27T06:00:00Z" else null,
        returnedAt = if (returned) "2026-10-27T06:00:00Z" else null,
    )

    @Test
    fun `pul — naqd kassaga kuryerdan, mijozdan taom + yetkazish, Click — faqat yetkazish`() {
        val cash = order(OnlinePayment.CASH, 64_000, 10_000)
        val click = order(OnlinePayment.CLICK, 36_000, 12_000)
        val outside = order(OnlinePayment.CASH, 32_000, null)
        assertEquals(64_000, CourierMoney.kassa(cash))
        assertEquals(0, CourierMoney.kassa(click))
        assertEquals(96_000, CourierMoney.kassa(listOf(cash, click, outside)))
        assertEquals(74_000, CourierMoney.collect(cash))
        assertEquals(12_000, CourierMoney.collect(click))
        // hududdan tashqarida — narxni kuryer o'zi aytadi, hisobga faqat taom
        assertEquals(32_000, CourierMoney.collect(outside))
    }

    @Test
    fun `bugungi natija — faqat yetkazilganlar puli`() {
        val today = CourierMoney.today(
            listOf(
                order(OnlinePayment.CASH, 64_000, 10_000),
                order(OnlinePayment.CLICK, 36_000, 12_000),
                order(OnlinePayment.CASH, 50_000, 10_000, delivered = false, returned = true),
            ),
        )
        assertEquals(CourierMoney.Today(delivered = 2, returned = 1, cash = 64_000, fees = 22_000), today)
    }

    @Test
    fun `masofa va raqam formati`() {
        assertEquals("3,5", Format.km(3.456))
        assertEquals("0,2", Format.km(0.23))
        assertEquals("12,0", Format.km(11.98))
        assertEquals("#015", Format.orderNumber(15))
        assertEquals("64${NBSP}000${NBSP}so'm", Format.money(64_000, "so'm"))
    }
}
