package pr.com.kotlin_nyff.data.remote

import pr.com.kotlin_nyff.data.model.Branch
import pr.com.kotlin_nyff.data.model.CustomerOrder
import pr.com.kotlin_nyff.data.model.DeliveryRoute
import pr.com.kotlin_nyff.data.model.Menu
import pr.com.kotlin_nyff.data.model.MobileOrderRequest
import pr.com.kotlin_nyff.data.model.MobileOrderResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Mijoz ilovasi endpointlari (NestJS_NYFF/API.md 5-bo'lim) — hammasi ochiq, token yo'q. */
interface NyffApi {

    /** Filiallar `onlineState` bilan (so'rov paytidagi surat). */
    @GET("api/branches")
    suspend fun branches(): List<Branch>

    /** Dostavka nuqtasi → buyurtmani oladigan filial va hudud narxi. */
    @GET("api/delivery/route")
    suspend fun deliveryRoute(@Query("lat") lat: Double, @Query("lng") lng: Double): DeliveryRoute

    /** Filialda sotuvdagi taomlar (narx shu filialniki), `sortOrder` bo'yicha tartiblangan. */
    @GET("api/menu")
    suspend fun menu(@Query("branchId") branchId: String): Menu

    /** Idempotent emas — tarmoq xatosida avtomatik qayta yuborilmaydi. */
    @POST("api/mobile/orders")
    suspend fun placeOrder(@Body body: MobileOrderRequest): MobileOrderResponse

    /** Token noto'g'ri bo'lsa — `404 not_found` (buyurtma umuman yo'qdek). */
    @GET("api/mobile/orders/{id}")
    suspend fun order(@Path("id") id: String, @Header("X-Order-Token") token: String): CustomerOrder
}
