package pr.com.kotlin_nyff.courier.data.remote

import pr.com.kotlin_nyff.courier.data.AvailableOrder
import pr.com.kotlin_nyff.courier.data.CourierLoginRequest
import pr.com.kotlin_nyff.courier.data.CourierOrders
import pr.com.kotlin_nyff.courier.data.CourierState
import pr.com.kotlin_nyff.courier.data.DutyRequest
import pr.com.kotlin_nyff.courier.data.LoginResponse
import pr.com.kotlin_nyff.courier.data.ReturnRequest
import pr.com.kotlin_nyff.data.model.Branch
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** Kuryer qismi endpointlari (NestJS_NYFF/API.md 4.3 va 12). Token — [AuthInterceptor] qo'shadi. */
interface CourierApi {

    @POST("api/auth/courier-login")
    suspend fun login(@Body body: CourierLoginRequest): LoginResponse

    @GET("api/branches")
    suspend fun branches(): List<Branch>

    @GET("api/courier/me")
    suspend fun me(): CourierState

    @PUT("api/courier/duty")
    suspend fun setDuty(@Body body: DutyRequest): CourierState

    @GET("api/courier/orders/available")
    suspend fun available(): List<AvailableOrder>

    @GET("api/courier/orders")
    suspend fun orders(): CourierOrders

    @POST("api/courier/orders/{id}/take")
    suspend fun take(@Path("id") id: String): CourierOrders

    @POST("api/courier/orders/{id}/release")
    suspend fun release(@Path("id") id: String): CourierOrders

    @POST("api/courier/depart")
    suspend fun depart(): CourierOrders

    @POST("api/courier/orders/{id}/deliver")
    suspend fun deliver(@Path("id") id: String): CourierOrders

    @POST("api/courier/orders/{id}/return")
    suspend fun returnOrder(@Path("id") id: String, @Body body: ReturnRequest): CourierOrders
}
