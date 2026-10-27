package pr.com.kotlin_nyff.courier.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import pr.com.kotlin_nyff.courier.data.SessionStore
import javax.inject.Inject

/** Kuryer so'rovlariga `Authorization: Bearer <token>`; server tokenni rad etsa (`401`) — sessiya tugaydi. */
class AuthInterceptor @Inject constructor(private val session: SessionStore) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = session.token
        val request = if (token != null) {
            chain.request().newBuilder().header("Authorization", "Bearer $token").build()
        } else {
            chain.request()
        }
        val response = chain.proceed(request)
        // kirishdagi 401 — noto'g'ri login yoki parol (sessiya hali yo'q)
        if (response.code == 401 && token != null) session.expire()
        return response
    }
}
