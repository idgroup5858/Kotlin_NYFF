package pr.com.kotlin_nyff.data.remote

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import pr.com.kotlin_nyff.data.model.BranchOnlineState
import pr.com.kotlin_nyff.data.model.CancelReason
import pr.com.kotlin_nyff.data.model.GeoPoint
import pr.com.kotlin_nyff.data.model.MobileOrderRequest
import pr.com.kotlin_nyff.data.model.OnlinePayment
import pr.com.kotlin_nyff.data.model.OnlineRequest
import pr.com.kotlin_nyff.data.model.OrderLineRequest
import pr.com.kotlin_nyff.data.model.OrderStatus
import pr.com.kotlin_nyff.data.model.OrderType
import pr.com.kotlin_nyff.data.model.WorkHours
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** Backend javoblari (NestJS_NYFF, demo bazadan olingan namunalar) to'g'ri o'qilishi va xatolar [AppError] ga aylanishi. */
class NyffApiTest {

    private lateinit var server: MockWebServer
    private lateinit var api: NyffApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(NyffJson.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(NyffApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun respond(code: Int, body: String) {
        server.enqueue(MockResponse().setResponseCode(code).setHeader("Content-Type", "application/json").setBody(body))
    }

    @Test
    fun `filiallar — ish vaqti, holat va noma'lum holat`() = runTest {
        respond(
            200,
            """[
              {"id":"br_chilonzor","name":"Chilonzor","address":"Bunyodkor 12","phone":"+998 71 200 11 22","active":true,
               "createdAt":"2026-09-12T01:44:30.109Z","location":{"lat":41.282,"lng":69.2045},
               "deliveryZone":[{"lat":41.305,"lng":69.17},{"lat":41.306,"lng":69.215},{"lat":41.295,"lng":69.24}],
               "deliveryFee":10000,"fallbackBranchId":"br_yunusobod","onlineEnabled":true,
               "workHours":{"open":"10:00","close":"23:00"},"onlineState":"off_hours"},
              {"id":"br_new","name":"Yangi","address":"","phone":"","active":true,"createdAt":"2026-10-01T00:00:00.000Z",
               "location":null,"deliveryZone":[],"deliveryFee":0,"fallbackBranchId":null,"onlineEnabled":true,
               "workHours":null,"onlineState":"kelajakdagi_holat"}
            ]""",
        )
        val branches = api.branches()
        assertEquals("/api/branches", server.takeRequest().path)
        assertEquals(BranchOnlineState.OFF_HOURS, branches[0].onlineState)
        assertEquals(WorkHours("10:00", "23:00"), branches[0].workHours)
        assertEquals(3, branches[0].deliveryZone.size)
        assertEquals(BranchOnlineState.UNKNOWN, branches[1].onlineState)
        assertNull(branches[1].location)
        assertFalse(branches[1].isOpen)
    }

    @Test
    fun `yo'nalish — hududdan tashqarida narx null`() = runTest {
        respond(200, """{"branchId":"br_yunusobod","zoneBranchId":null,"fee":null,"redirected":false}""")
        val route = api.deliveryRoute(41.311, 69.279)
        assertEquals("/api/delivery/route?lat=41.311&lng=69.279", server.takeRequest().path)
        assertEquals("br_yunusobod", route.branchId)
        assertNull(route.fee)
    }

    @Test
    fun `menyu — uch tildagi nom va tarkib`() = runTest {
        respond(
            200,
            """{"branchId":"br_chilonzor","categories":[{"id":"burgers","name":{"en":"Burgers","ru":"Бургеры","uz":"Burgerlar"},
               "image":"/images/categories/burgers.jpg","icon":"/images/icons/hamburger.png","sortOrder":1,"active":true}],
               "products":[{"id":"burger-classic","categoryId":"burgers","name":{"en":"Classic burger","ru":"Классический бургер",
               "uz":"Klassik burger"},"image":"/images/products/burger-classic.jpg","sortOrder":1,"price":32000,
               "composition":[{"en":"Burger bun","ru":"Булочка для бургера","uz":"Burger noni"},{"en":"","ru":"","uz":"Piyoz"}]}]}""",
        )
        val menu = api.menu("br_chilonzor")
        val product = menu.products.single()
        assertEquals(32_000L, product.price)
        assertEquals("Классический бургер", product.name.get("ru"))
        // bo'sh tarjima — o'zbekchasi
        assertEquals("Piyoz", product.composition[1].get("en"))
    }

    @Test
    fun `buyurtma — so'rov tanasi va kuzatish tokeni`() = runTest {
        respond(
            201,
            """{"order":{"id":"ord_3f2a9c1b7e4d8a05","branchId":"br_chilonzor","number":17,"type":"delivery",
               "items":[{"productId":"burger-classic","name":{"uz":"Klassik burger","ru":"","en":""},"price":32000,"qty":2,"note":"piyozsiz"}],
               "note":"","total":64000,"payment":null,"online":{"platform":"android","name":"Aziza","phone":"+998 90 123 45 67",
               "address":"Bunyodkor 5","payment":"cash","location":{"lat":41.282,"lng":69.2045}},"status":"pending",
               "createdAt":"2026-10-27T05:12:33.123Z","preparingAt":null,"readyAt":null,"cancelledAt":null,"cancelReason":null,
               "routedAt":"2026-10-27T05:12:33.123Z","transferCount":0,"fromBranchId":null},"trackingToken":"q9Vxtoken"}""",
        )
        val response = api.placeOrder(
            MobileOrderRequest(
                type = OrderType.DELIVERY,
                items = listOf(OrderLineRequest("burger-classic", 2, "piyozsiz")),
                online = OnlineRequest(
                    name = "Aziza",
                    phone = "+998 90 123 45 67",
                    address = "Bunyodkor 5",
                    payment = OnlinePayment.CASH,
                    location = GeoPoint(41.282, 69.2045),
                ),
            ),
        )
        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/mobile/orders", request.path)
        val body = NyffJson.parseToJsonElement(request.body.readUtf8()).jsonObject
        assertEquals("delivery", body["type"]!!.jsonPrimitive.content)
        // dostavkada filialni server tanlaydi — branchId yuborilmaydi
        assertFalse(body.containsKey("branchId"))
        val online = body["online"]!!.jsonObject
        assertEquals("android", online["platform"]!!.jsonPrimitive.content)
        assertEquals("cash", online["payment"]!!.jsonPrimitive.content)
        assertTrue(online.containsKey("location"))

        assertEquals("q9Vxtoken", response.trackingToken)
        assertEquals(OrderStatus.PENDING, response.order.status)
        assertNull(response.order.payment)
        assertNull(response.order.cancelReason)
        assertEquals(GeoPoint(41.282, 69.2045), response.order.online?.location)
    }

    @Test
    fun `holat — X-Order-Token sarlavhasi, noma'lum sabab null bo'ladi`() = runTest {
        respond(
            200,
            """{"id":"ord_1","branchId":"br_yunusobod","number":3,"type":"takeaway","items":[],"note":"","total":0,
               "payment":"cash","online":null,"status":"cancelled","createdAt":"2026-10-27T05:12:33.123Z","preparingAt":null,
               "readyAt":null,"cancelledAt":"2026-10-27T05:18:00.000Z","cancelReason":"yangi_sabab",
               "routedAt":"2026-10-27T05:12:33.123Z","transferCount":1,"fromBranchId":"br_chilonzor"}""",
        )
        val order = api.order("ord_1", "secret")
        val request = server.takeRequest()
        assertEquals("/api/mobile/orders/ord_1", request.path)
        assertEquals("secret", request.getHeader("X-Order-Token"))
        assertEquals(OrderStatus.CANCELLED, order.status)
        assertNull(order.cancelReason)
        assertEquals(1, order.transferCount)
    }

    @Test
    fun `sabab o'qiladi`() = runTest {
        respond(
            200,
            """{"id":"ord_1","branchId":"b","number":3,"type":"delivery","total":0,"status":"cancelled",
               "createdAt":"2026-10-27T05:12:33.123Z","cancelReason":"no_branch"}""",
        )
        assertEquals(CancelReason.NO_BRANCH, api.order("ord_1", "t").cancelReason)
    }

    @Test
    fun `biznes xatosi — code va detail`() = runTest {
        respond(409, """{"statusCode":409,"code":"branch_closed","message":"Filial hozir buyurtma qabul qilmayapti","detail":"br_chilonzor"}""")
        val error = expectError { api.branches() }
        assertTrue(error is AppError.Server)
        error as AppError.Server
        assertEquals(409, error.status)
        assertEquals("branch_closed", error.code)
        assertEquals("br_chilonzor", error.detail)
    }

    @Test
    fun `NestJS'ning o'z xatosi — code yo'q`() = runTest {
        respond(404, """{"message":"Cannot GET /api/nope","error":"Not Found","statusCode":404}""")
        val error = expectError { api.branches() } as AppError.Server
        assertEquals(404, error.status)
        assertNull(error.code)
    }

    @Test
    fun `server javob bermadi — Network`() = runTest {
        server.shutdown()
        assertTrue(expectError { api.branches() } is AppError.Network)
    }

    @Test
    fun `javob turi mos emas — Unexpected`() = runTest {
        respond(200, """{"bu":"massiv emas"}""")
        assertTrue(expectError { api.branches() } is AppError.Unexpected)
    }

    private suspend fun expectError(block: suspend () -> Unit): AppError {
        try {
            apiCall { block() }
        } catch (e: AppError) {
            return e
        }
        fail("xato kutilgan edi")
        throw IllegalStateException()
    }
}
