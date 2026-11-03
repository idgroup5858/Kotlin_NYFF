package pr.com.kotlin_nyff.courier.data.remote

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pr.com.kotlin_nyff.courier.data.CourierBlock
import pr.com.kotlin_nyff.courier.data.CourierLoginRequest
import pr.com.kotlin_nyff.courier.data.ReturnReason
import pr.com.kotlin_nyff.courier.data.ReturnRequest
import pr.com.kotlin_nyff.data.model.OnlinePayment
import pr.com.kotlin_nyff.data.model.OrderStatus
import pr.com.kotlin_nyff.data.remote.AppError
import pr.com.kotlin_nyff.data.remote.NyffJson
import pr.com.kotlin_nyff.data.remote.apiCall
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** Backend javoblari (NestJS_NYFF API.md 12, jonli serverdan olingan shakl) va xato kodlari. */
class CourierApiTest {

    private lateinit var server: MockWebServer
    private lateinit var api: CourierApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(NyffJson.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(CourierApi::class.java)
    }

    @After
    fun tearDown() = server.shutdown()

    private fun respond(code: Int, body: String) {
        server.enqueue(MockResponse().setResponseCode(code).setHeader("Content-Type", "application/json").setBody(body))
    }

    @Test
    fun `kirish — email va parol yuboriladi, noto'g'ri bo'lsa wrong_login`() = runTest {
        respond(
            200,
            """{"token":"jwt","user":{"id":"usr_1","name":"Bobur","surname":"Aliyev","role":"courier","branchId":null,
                "email":"bobur@gmail.com"},"shift":null}""",
        )
        val result = api.login(CourierLoginRequest("bobur@gmail.com", "bobur2222"))
        val request = server.takeRequest()
        assertEquals("/api/auth/courier-login", request.path)
        val body = NyffJson.parseToJsonElement(request.body.readUtf8()).jsonObject
        assertEquals(setOf("email", "password"), body.keys)
        assertEquals("bobur@gmail.com", body["email"]!!.jsonPrimitive.content)
        assertEquals("bobur2222", body["password"]!!.jsonPrimitive.content)
        assertEquals("courier", result.user.role)
        assertEquals("bobur@gmail.com", result.user.email)
        assertEquals("Bobur Aliyev", result.user.fullName)

        respond(401, """{"statusCode":401,"code":"wrong_login","message":"Email yoki parol noto'g'ri"}""")
        val error = runCatching { apiCall { api.login(CourierLoginRequest("bobur@gmail.com", "x")) } }.exceptionOrNull() as AppError.Server
        assertEquals("wrong_login", error.code)
    }

    @Test
    fun `kuryer holati — familiyasi bo'sh eski xodim faqat ism bilan`() = runTest {
        respond(
            200,
            """{"user":{"id":"usr_1","name":"Bobur","surname":"","role":"courier","branchId":null,"email":"bobur@gmail.com"},
                "onDuty":true,"branchId":null,"departed":false}""",
        )
        val me = api.me()
        assertEquals("/api/courier/me", server.takeRequest().path)
        assertEquals("Bobur", me.user.fullName)
        assertTrue(me.onDuty)
    }

    @Test
    fun `Yangi ro'yxati — masofa, yo'lga qo'shimchasi, nega olib bo'lmaydi`() = runTest {
        respond(
            200,
            """[{"id":"ord_1","branchId":"br_chilonzor","number":15,"status":"preparing","createdAt":"2026-10-27T08:00:00.000Z",
                "preparingAt":"2026-10-27T08:01:00.000Z","readyAt":null,
                "items":[{"productId":"burger-classic","name":{"uz":"Klassik burger","ru":"","en":""},"price":32000,"qty":2,"note":""}],
                "total":64000,"payment":"cash","deliveryFee":10000,"address":"Bunyodkor 5","location":{"lat":41.285,"lng":69.205},
                "distanceKm":0.23,"extraKm":1.4,"blocked":null},
               {"id":"ord_2","branchId":"br_yunusobod","number":6,"status":"ready","createdAt":"2026-10-27T08:00:00.000Z",
                "preparingAt":null,"readyAt":null,"items":[],"total":36000,"payment":"click","deliveryFee":null,"address":"",
                "location":null,"distanceKm":null,"extraKm":null,"blocked":"other_branch"}]""",
        )
        val list = api.available()
        assertEquals("/api/courier/orders/available", server.takeRequest().path)
        assertEquals(OrderStatus.PREPARING, list[0].status)
        assertEquals(OnlinePayment.CASH, list[0].payment)
        assertEquals(1.4, list[0].extraKm!!, 0.0)
        assertNull(list[0].blocked)
        assertEquals(CourierBlock.OTHER_BRANCH, list[1].blocked)
        assertNull(list[1].deliveryFee)
    }

    @Test
    fun `mening buyurtmalarim — navbat, mijoz, qaytarish sababi`() = runTest {
        respond(
            200,
            """{"active":[{"id":"ord_1","branchId":"br_chilonzor","number":15,"items":[],"note":"","total":64000,
                 "online":{"platform":"android","name":"Aziza","phone":"+998 90 123 45 67","address":"Bunyodkor 5","payment":"cash",
                 "location":{"lat":41.285,"lng":69.205}},"status":"ready","createdAt":"2026-10-27T08:00:00.000Z","preparingAt":null,
                 "readyAt":"2026-10-27T08:10:00.000Z","cancelledAt":null,"deliveryFee":10000,"courierId":"usr_1",
                 "takenAt":"2026-10-27T08:05:00.000Z","departedAt":"2026-10-27T08:12:00.000Z","deliveredAt":null,"returnedAt":null,
                 "returnReason":null,"returnNote":"","courierSeq":1,"courierLegKm":0.35}],
               "done":[{"id":"ord_0","branchId":"br_chilonzor","number":14,"total":32000,"status":"ready",
                 "createdAt":"2026-10-27T07:00:00.000Z","returnedAt":"2026-10-27T07:40:00.000Z","returnReason":"no_answer",
                 "returnNote":"3 marta","courierSeq":2}]}""",
        )
        val orders = api.orders()
        val first = orders.active.single()
        assertEquals(1, first.courierSeq)
        assertEquals("Aziza", first.online?.name)
        assertEquals(41.285, first.location!!.lat, 0.0)
        assertEquals(ReturnReason.NO_ANSWER, orders.done.single().returnReason)
    }

    @Test
    fun `qaytarish — sabab va izoh yuboriladi`() = runTest {
        respond(200, """{"active":[],"done":[]}""")
        api.returnOrder("ord_1", ReturnRequest(ReturnReason.WRONG_ADDRESS, "uy topilmadi"))
        val request = server.takeRequest()
        assertEquals("/api/courier/orders/ord_1/return", request.path)
        val body = NyffJson.parseToJsonElement(request.body.readUtf8()).jsonObject
        assertEquals("wrong_address", body["reason"]!!.jsonPrimitive.content)
        assertEquals("uy topilmadi", body["note"]!!.jsonPrimitive.content)
    }

    @Test
    fun `kuryer xato kodlari`() = runTest {
        respond(409, """{"statusCode":409,"code":"other_branch","message":"Qo'lingizda boshqa filial buyurtmalari bor","detail":"br_chilonzor"}""")
        val error = runCatching { apiCall { api.take("ord_2") } }.exceptionOrNull()
        assertTrue(error is AppError.Server)
        assertEquals("other_branch", (error as AppError.Server).code)
        assertEquals(409, error.status)

        respond(409, """{"statusCode":409,"code":"not_ready","message":"Hamma buyurtma hali tayyor emas","detail":"15,16"}""")
        val notReady = runCatching { apiCall { api.depart() } }.exceptionOrNull() as AppError.Server
        assertEquals("not_ready", notReady.code)
        assertEquals("15,16", notReady.detail)
    }
}
