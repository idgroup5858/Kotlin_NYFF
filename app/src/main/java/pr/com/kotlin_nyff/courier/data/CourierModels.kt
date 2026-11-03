package pr.com.kotlin_nyff.courier.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pr.com.kotlin_nyff.data.model.GeoPoint
import pr.com.kotlin_nyff.data.model.OnlineInfo
import pr.com.kotlin_nyff.data.model.OnlinePayment
import pr.com.kotlin_nyff.data.model.OrderItem
import pr.com.kotlin_nyff.data.model.OrderStatus

/*
 * Kuryer qismi turlari — NestJS_NYFF/API.md 12-bo'lim. Umumiylari (filial, taom, mijoz, holat) — mijoz modellaridan
 * (`data/model/Models.kt`). Noma'lum enum qiymati standart qiymatga tushadi (Json `coerceInputValues`).
 */

// ---------------------------------------------------------------- kirish

/** Kirgan xodim (kuryer). `surname` eski xodimlarda bo'sh bo'lishi mumkin; `email` — kirish emaili (kichik harfda). */
@Serializable
data class StaffMember(
    val id: String,
    val name: String,
    val role: String = "",
    val branchId: String? = null,
    val surname: String = "",
    val email: String? = null,
) {
    /** Ekranda — "Ism Familiya"; familiya bo'sh bo'lsa faqat ism. */
    val fullName: String get() = "$name $surname".trim()
}

/** `POST /auth/courier-login` — admin ochib bergan email va parol (email katta-kichik harfni farqlamaydi). */
@Serializable
data class CourierLoginRequest(val email: String, val password: String)

@Serializable
data class LoginResponse(val token: String, val user: StaffMember)

@Serializable
data class CourierState(
    val user: StaffMember,
    val onDuty: Boolean = false,
    /** qo'ldagi buyurtmalar filiali; qo'l bo'sh — `null` */
    val branchId: String? = null,
    val departed: Boolean = false,
)

@Serializable
data class DutyRequest(val onDuty: Boolean)

// ---------------------------------------------------------------- buyurtmalar

/** "Yetkazib bo'lmadi" sababi. */
@Serializable
enum class ReturnReason {
    @SerialName("no_answer") NO_ANSWER,
    @SerialName("wrong_address") WRONG_ADDRESS,
    @SerialName("refused") REFUSED,
    @SerialName("other") OTHER,
}

/** Nega olib bo'lmaydi; `null` — olsa bo'ladi. */
@Serializable
enum class CourierBlock {
    @SerialName("off_duty") OFF_DUTY,
    @SerialName("courier_busy") COURIER_BUSY,
    @SerialName("other_branch") OTHER_BRANCH,
}

/** "Yangi" ro'yxati — mijoz ismi va telefonisiz. */
@Serializable
data class AvailableOrder(
    val id: String,
    val branchId: String,
    val number: Int,
    val status: OrderStatus = OrderStatus.UNKNOWN,
    val createdAt: String,
    val preparingAt: String? = null,
    val readyAt: String? = null,
    val items: List<OrderItem> = emptyList(),
    val total: Long = 0,
    val payment: OnlinePayment? = null,
    /** hudud narxi; `null` — narxni kuryer aytadi */
    val deliveryFee: Long? = null,
    val address: String = "",
    val location: GeoPoint? = null,
    /** filialdan mijozgacha, km (to'g'ri chiziq) */
    val distanceKm: Double? = null,
    /** qo'ldagilar yo'liga qo'shilsa yo'l qancha uzayadi, km; qo'l bo'sh — `null` */
    val extraKm: Double? = null,
    val blocked: CourierBlock? = null,
)

/** Kuryer olgan buyurtma. */
@Serializable
data class CourierOrder(
    val id: String,
    val branchId: String,
    val number: Int,
    val items: List<OrderItem> = emptyList(),
    val note: String = "",
    val total: Long = 0,
    val online: OnlineInfo? = null,
    val status: OrderStatus = OrderStatus.UNKNOWN,
    val createdAt: String,
    val preparingAt: String? = null,
    val readyAt: String? = null,
    val deliveryFee: Long? = null,
    val takenAt: String? = null,
    val departedAt: String? = null,
    val deliveredAt: String? = null,
    val returnedAt: String? = null,
    val returnReason: ReturnReason? = null,
    val returnNote: String = "",
    /** yo'ldagi navbati (1, 2, …) */
    val courierSeq: Int? = null,
    /** oldingi nuqtadan shu manzilgacha, km */
    val courierLegKm: Double? = null,
) {
    val payment: OnlinePayment get() = online?.payment ?: OnlinePayment.CASH
    val location: GeoPoint? get() = online?.location
}

/** Qo'ldagilar (yo'lda — navbat bo'yicha) va bugun tugatilganlari. */
@Serializable
data class CourierOrders(
    val active: List<CourierOrder> = emptyList(),
    val done: List<CourierOrder> = emptyList(),
)

@Serializable
data class ReturnRequest(val reason: ReturnReason, val note: String = "")
