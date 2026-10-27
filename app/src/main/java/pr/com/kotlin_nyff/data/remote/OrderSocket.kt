package pr.com.kotlin_nyff.data.remote

import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import pr.com.kotlin_nyff.BuildConfig
import pr.com.kotlin_nyff.data.model.CustomerOrder
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

sealed interface SocketEvent {
    /** Ulandi (yoki qayta ulandi) — uzilish paytidagi xabarlar yo'qolgan bo'lishi mumkin, REST'dan qayta olinadi. */
    data object Connected : SocketEvent

    /** Server tokenni tekshirdi, `order:<id>` xonasiga obuna bo'lindi. */
    data object Ready : SocketEvent

    data object Disconnected : SocketEvent

    /** Token noto'g'ri — server uzadi, qayta ulanilmaydi. */
    data object AuthError : SocketEvent

    data class Order(val order: CustomerOrder) : SocketEvent
}

/**
 * Socket.IO (API.md 8-bo'lim): `auth: { orderId, orderToken }` → `order:<id>` xonasi. Buyruqlar yuborilmaydi — faqat
 * serverdan `order` xabari keladi. Callback'lar asosiy oqimda emas; [Flow] orqali ViewModel'ga uzatiladi.
 */
@Singleton
class OrderSocket @Inject constructor() {

    fun events(orderId: String, token: String): Flow<SocketEvent> = callbackFlow {
        val options = IO.Options.builder()
            .setAuth(mapOf("orderId" to orderId, "orderToken" to token))
            .build()
        // Socket — server ildizida (`/api` prefiksi socket'ga tegishli emas)
        val socket = IO.socket(URI.create(BuildConfig.API_URL), options)

        socket.on(Socket.EVENT_CONNECT) { trySend(SocketEvent.Connected) }
        socket.on(Socket.EVENT_DISCONNECT) { trySend(SocketEvent.Disconnected) }
        socket.on("ready") { trySend(SocketEvent.Ready) }
        socket.on("auth_error") { trySend(SocketEvent.AuthError) }
        socket.on("order") { args ->
            val order = runCatching {
                NyffJson.decodeFromString<CustomerOrder>(args.firstOrNull().toString())
            }.getOrNull()
            if (order != null && order.id == orderId) trySend(SocketEvent.Order(order))
        }
        socket.connect()

        awaitClose {
            socket.off()
            socket.disconnect()
        }
    }
}
