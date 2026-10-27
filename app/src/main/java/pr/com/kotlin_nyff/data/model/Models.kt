package pr.com.kotlin_nyff.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
 * Backend turlari — NestJS_NYFF/API.md 6-bo'lim. Javobda maydon doim bor (`null` bo'lishi mumkin); noma'lum enum
 * qiymati standart qiymatga tushadi (Json `coerceInputValues`) — server yangi holat qo'shsa ilova yiqilmasin.
 */

/** Uch tildagi matn: `uz` majburiy, `ru` / `en` bo'sh bo'lsa `uz` ko'rsatiladi. */
@Serializable
data class LocalizedText(val uz: String = "", val ru: String = "", val en: String = "") {
    fun get(language: String): String = when (language) {
        "ru" -> ru
        "en" -> en
        else -> uz
    }.ifBlank { uz }
}

/** WGS84 koordinata (`LatLng` — Google Maps turi bilan adashmasin). */
@Serializable
data class GeoPoint(val lat: Double, val lng: Double)

/** Online qabul vaqti, "HH:MM" (Asia/Tashkent); yarim tundan o'tishi mumkin. */
@Serializable
data class WorkHours(val open: String, val close: String)

@Serializable
enum class BranchOnlineState {
    @SerialName("open") OPEN,
    @SerialName("inactive") INACTIVE,
    @SerialName("paused") PAUSED,
    @SerialName("off_hours") OFF_HOURS,
    @SerialName("no_shift") NO_SHIFT,
    @SerialName("no_kassa") NO_KASSA,
    UNKNOWN,
}

@Serializable
data class Branch(
    val id: String,
    val name: String,
    val address: String = "",
    val phone: String = "",
    val active: Boolean = true,
    val location: GeoPoint? = null,
    val deliveryZone: List<GeoPoint> = emptyList(),
    val deliveryFee: Long = 0,
    val workHours: WorkHours? = null,
    val onlineState: BranchOnlineState = BranchOnlineState.UNKNOWN,
) {
    val isOpen: Boolean get() = onlineState == BranchOnlineState.OPEN
}

/** `GET /delivery/route`: buyurtmani qaysi filial oladi va yetkazish narxi. */
@Serializable
data class DeliveryRoute(
    /** `null` — hech bir filial ochiq emas. */
    val branchId: String? = null,
    /** Nuqta tushgan hudud egasi; `null` — hech qaysi hududda emas. */
    val zoneBranchId: String? = null,
    /** Hudud narxi (`0` — bepul); `null` — hududdan tashqarida, narxni kuryer aytadi. */
    val fee: Long? = null,
    /** Hudud egasi yopiq — buyurtmani boshqa filial oladi. */
    val redirected: Boolean = false,
)

@Serializable
data class Category(
    val id: String,
    val name: LocalizedText,
    val image: String = "",
    val icon: String = "",
    val sortOrder: Int = 0,
)

@Serializable
data class MenuProduct(
    val id: String,
    val categoryId: String,
    val name: LocalizedText,
    val image: String = "",
    val sortOrder: Int = 0,
    /** Shu filial narxi, so'm. */
    val price: Long? = null,
    val composition: List<LocalizedText> = emptyList(),
)

@Serializable
data class Menu(
    val branchId: String,
    val categories: List<Category> = emptyList(),
    val products: List<MenuProduct> = emptyList(),
)

@Serializable
enum class OrderType {
    @SerialName("dine_in") DINE_IN,
    @SerialName("takeaway") TAKEAWAY,
    @SerialName("delivery") DELIVERY,
}

@Serializable
enum class OrderStatus {
    @SerialName("pending") PENDING,
    @SerialName("preparing") PREPARING,
    @SerialName("ready") READY,
    @SerialName("cancelled") CANCELLED,
    UNKNOWN,
}

/** Server o'zi bekor qilgan sabab; filial (kassir / admin) bekor qilsa — `null` (noma'lum qiymat ham `null`). */
@Serializable
enum class CancelReason {
    @SerialName("timeout") TIMEOUT,
    @SerialName("branch_closed") BRANCH_CLOSED,
    @SerialName("no_branch") NO_BRANCH,
}

@Serializable
enum class OnlinePayment {
    @SerialName("click") CLICK,
    @SerialName("cash") CASH,
}

@Serializable
data class OrderItem(
    val productId: String,
    val name: LocalizedText,
    val price: Long,
    val qty: Int,
    val note: String = "",
)

@Serializable
data class OnlineInfo(
    val platform: String = "android",
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val payment: OnlinePayment = OnlinePayment.CASH,
    val location: GeoPoint? = null,
)

/** Mijoz ko'rinishidagi buyurtma (`CustomerOrder`) — REST va socket `order:<id>` xonasi. */
@Serializable
data class CustomerOrder(
    val id: String,
    val branchId: String,
    /** Filialda har kuni 1 dan; boshqa filialga o'tkazilsa o'zgaradi. */
    val number: Int,
    val type: OrderType,
    val items: List<OrderItem> = emptyList(),
    val note: String = "",
    /** Taomlar summasi (yetkazish narxisiz). */
    val total: Long,
    val payment: String? = null,
    val online: OnlineInfo? = null,
    val status: OrderStatus = OrderStatus.UNKNOWN,
    val createdAt: String,
    val preparingAt: String? = null,
    val readyAt: String? = null,
    val cancelledAt: String? = null,
    val cancelReason: CancelReason? = null,
    /** Shu filialga tushgan vaqt — qabul muddati (5 daqiqa) shundan. */
    val routedAt: String? = null,
    val transferCount: Int = 0,
    val fromBranchId: String? = null,
    /** Buyurtma berilgandagi hudud narxi (dostavka; summaga kirmaydi); `null` — narxni kuryer aytadi. */
    val deliveryFee: Long? = null,
    /** Kuryer bosqichlari (dostavka): `status` `ready` da qoladi. */
    val departedAt: String? = null,
    val deliveredAt: String? = null,
    /** Kuryer yetkaza olmadi — buyurtma filialga qaytdi, filial mijoz bilan bog'lanadi. */
    val returnedAt: String? = null,
)

// ---------------------------------------------------------------- so'rovlar

@Serializable
data class OrderLineRequest(val productId: String, val qty: Int, val note: String = "")

@Serializable
data class OnlineRequest(
    val platform: String = "android",
    val name: String,
    val phone: String,
    val address: String = "",
    val payment: OnlinePayment,
    val location: GeoPoint? = null,
)

/** `POST /mobile/orders`. Dostavkada `branchId` yuborilmaydi — filialni server tanlaydi. */
@Serializable
data class MobileOrderRequest(
    val type: OrderType,
    val branchId: String? = null,
    val items: List<OrderLineRequest>,
    val note: String = "",
    val online: OnlineRequest,
)

@Serializable
data class MobileOrderResponse(val order: CustomerOrder, val trackingToken: String)
