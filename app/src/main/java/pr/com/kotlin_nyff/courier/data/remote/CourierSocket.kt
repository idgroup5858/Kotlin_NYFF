package pr.com.kotlin_nyff.courier.data.remote

import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.Serializable
import pr.com.kotlin_nyff.BuildConfig
import pr.com.kotlin_nyff.data.remote.NyffJson
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

/** `couriers` xonasidagi belgi — ro'yxatlarni REST'dan qayta olish kerak. */
@Serializable
data class CourierSignal(val id: String, val branchId: String = "", val number: Int = 0, val courierId: String? = null)

sealed interface CourierEvent {
    /** Ulandi (yoki qayta ulandi) — uzilish paytidagi xabarlar yo'qolgan bo'lishi mumkin. */
    data object Connected : CourierEvent

    /** Server tokenni tekshirdi, xonalarga obuna bo'lindi. */
    data object Ready : CourierEvent

    data object Disconnected : CourierEvent

    /** Token eskirgan / kuryer bloklangan — server uzadi, qayta ulanilmaydi. */
    data object AuthError : CourierEvent

    data class Changed(val signal: CourierSignal) : CourierEvent
}

/**
 * Socket.IO (API.md 8, 12): `auth: { token }` → `couriers` (har mobil dostavka o'zgarganda belgi) va `courier:<id>`
 * (o'z buyurtmasi). Buyruqlar faqat REST orqali.
 */
@Singleton
class CourierSocket @Inject constructor() {

    fun events(token: String): Flow<CourierEvent> = callbackFlow {
        val options = IO.Options.builder().setAuth(mapOf("token" to token)).build()
        // socket — server ildizida (`/api` prefiksi socket'ga tegishli emas)
        val socket = IO.socket(URI.create(BuildConfig.API_URL), options)
        socket.on(Socket.EVENT_CONNECT) { trySend(CourierEvent.Connected) }
        socket.on(Socket.EVENT_DISCONNECT) { trySend(CourierEvent.Disconnected) }
        socket.on("ready") { trySend(CourierEvent.Ready) }
        socket.on("auth_error") { trySend(CourierEvent.AuthError) }
        socket.on("order") { args ->
            runCatching { NyffJson.decodeFromString<CourierSignal>(args.firstOrNull().toString()) }
                .getOrNull()
                ?.let { trySend(CourierEvent.Changed(it)) }
        }
        socket.connect()
        awaitClose {
            socket.off()
            socket.disconnect()
        }
    }
}
