package pr.com.kotlin_nyff.ui.common

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import pr.com.kotlin_nyff.BuildConfig
import pr.com.kotlin_nyff.R
import pr.com.kotlin_nyff.data.model.Branch
import pr.com.kotlin_nyff.data.model.BranchOnlineState
import pr.com.kotlin_nyff.data.model.CustomerOrder
import pr.com.kotlin_nyff.data.model.LocalizedText
import pr.com.kotlin_nyff.data.model.OnlinePayment
import pr.com.kotlin_nyff.data.model.OrderType
import pr.com.kotlin_nyff.data.remote.AppError
import pr.com.kotlin_nyff.domain.Format
import pr.com.kotlin_nyff.domain.OrderHeadline
import pr.com.kotlin_nyff.domain.OrderProgress

/** Backend xato kodi → matn (API.md 3). `code` bo'lmasa (NestJS'ning o'z xatosi) — umumiy matn. */
@StringRes
fun errorRes(error: Throwable?): Int = when (error) {
    is AppError.Network -> R.string.error_network
    is AppError.Server -> when (error.code) {
        "not_found" -> R.string.error_not_found
        "empty_order" -> R.string.error_empty_order
        "product_unavailable" -> R.string.error_product_unavailable
        "invalid_qty" -> R.string.error_invalid_qty
        "invalid_status" -> R.string.error_invalid_status
        "branch_required" -> R.string.error_branch_required
        "phone_required" -> R.string.error_phone_required
        "address_required" -> R.string.error_address_required
        "invalid_location" -> R.string.error_invalid_location
        "invalid_input" -> R.string.error_invalid_input
        "too_many_attempts" -> R.string.error_too_many_attempts
        "branch_closed" -> R.string.error_branch_closed
        "location_required" -> R.string.error_location_required
        else -> R.string.error_unknown
    }
    else -> R.string.error_unknown
}

/** Holat → asosiy matn (API.md 5.6). */
@StringRes
fun headlineRes(headline: OrderHeadline): Int = when (headline) {
    OrderHeadline.WAITING -> R.string.step_placed
    OrderHeadline.COOKING -> R.string.step_cooking
    OrderHeadline.READY_DELIVERY -> R.string.ready_wait_courier
    OrderHeadline.READY_PICKUP -> R.string.step_ready_pickup
    OrderHeadline.ON_THE_WAY -> R.string.step_on_the_way
    OrderHeadline.DELIVERED -> R.string.step_delivered
    OrderHeadline.RETURNED -> R.string.returned
    OrderHeadline.CANCELLED -> R.string.cancelled
    OrderHeadline.CANCELLED_TIMEOUT -> R.string.cancel_timeout
    OrderHeadline.CANCELLED_TIMEOUT_PICKUP -> R.string.cancel_timeout_pickup
    OrderHeadline.CANCELLED_BRANCH_CLOSED -> R.string.cancel_branch_closed
    OrderHeadline.CANCELLED_BRANCH_CLOSED_PICKUP -> R.string.cancel_branch_closed_pickup
    OrderHeadline.CANCELLED_NO_BRANCH -> R.string.no_branch_open
}

/** Ro'yxatdagi qisqa holat belgisi (dostavkada kuryer bosqichi bilan). */
@StringRes
fun statusRes(order: CustomerOrder): Int = when (OrderProgress.headline(order)) {
    OrderHeadline.WAITING -> R.string.status_pending
    OrderHeadline.COOKING -> R.string.status_preparing
    OrderHeadline.READY_DELIVERY, OrderHeadline.READY_PICKUP -> R.string.status_ready
    OrderHeadline.ON_THE_WAY -> R.string.status_on_the_way
    OrderHeadline.DELIVERED -> R.string.status_delivered
    OrderHeadline.RETURNED -> R.string.status_returned
    else -> R.string.status_cancelled
}

/** Mijoz tanlagan to'lov: "Click orqali to'langan" / "Naqd — kuryerga" / "Naqd — filialda". */
@StringRes
fun paymentRes(order: CustomerOrder): Int? {
    val online = order.online ?: return null
    return when {
        online.payment == OnlinePayment.CLICK -> R.string.paid_online
        order.type == OrderType.DELIVERY -> R.string.pay_on_delivery
        else -> R.string.pay_on_pickup
    }
}

/** Filial holati (API.md 5.1): "Ochiq", "Yopiq · 10:00 da ochiladi", "Hozir buyurtma qabul qilmayapti", "Hozir yopiq". */
@Composable
@ReadOnlyComposable
fun branchStateText(branch: Branch): String = when (branch.onlineState) {
    BranchOnlineState.OPEN -> stringResource(R.string.state_open)
    BranchOnlineState.OFF_HOURS -> branch.workHours?.let { stringResource(R.string.closed_until, it.open) }
        ?: stringResource(R.string.closed_now)
    BranchOnlineState.PAUSED -> stringResource(R.string.state_paused)
    else -> stringResource(R.string.closed_now)
}

@Composable
@ReadOnlyComposable
fun money(value: Long): String = Format.money(value, stringResource(R.string.soum))

/** Hudud narxi: `0` — "Bepul", `null` — "Yetkazish narxini kuryer aytadi". */
@Composable
@ReadOnlyComposable
fun feeText(fee: Long?): String = when (fee) {
    null -> stringResource(R.string.courier_fee)
    0L -> stringResource(R.string.free)
    else -> money(fee)
}

/** Joriy ilova tili ("uz" / "ru" / "en"). */
@Composable
@ReadOnlyComposable
fun currentLanguage(): String = LocalConfiguration.current.locales[0]?.language ?: "uz"

@Composable
@ReadOnlyComposable
fun LocalizedText.text(): String = get(currentLanguage())

/** Menyudagi `/images/...` yo'li → to'liq URL (rasmlar `/api` prefikssiz). Bo'sh yo'l — rasm yo'q. */
fun imageUrl(path: String): String? = when {
    path.isBlank() -> null
    path.startsWith("http://") || path.startsWith("https://") -> path
    else -> BuildConfig.API_URL + (if (path.startsWith("/")) path else "/$path")
}
