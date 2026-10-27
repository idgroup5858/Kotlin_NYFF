package pr.com.kotlin_nyff.courier.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import pr.com.kotlin_nyff.R
import pr.com.kotlin_nyff.courier.data.ReturnReason
import pr.com.kotlin_nyff.data.remote.AppError
import pr.com.kotlin_nyff.ui.common.money

/** Kuryer qismi: backend xato kodi → matn (API.md 3, 12). `code` bo'lmasa (NestJS'ning o'z xatosi) — umumiy matn. */
@StringRes
fun courierErrorRes(error: Throwable?): Int = when (error) {
    is AppError.Network -> R.string.courier_error_network
    is AppError.Server -> when (error.code) {
        "not_found" -> R.string.courier_error_not_found
        "wrong_login" -> R.string.courier_error_wrong_login
        "user_inactive" -> R.string.courier_error_user_inactive
        "too_many_attempts" -> R.string.courier_error_too_many_attempts
        "invalid_input" -> R.string.courier_error_invalid_input
        "unauthorized" -> R.string.courier_error_unauthorized
        "forbidden" -> R.string.courier_error_forbidden
        "invalid_status" -> R.string.courier_error_invalid_status
        "empty_order" -> R.string.courier_error_empty_order
        "off_duty" -> R.string.courier_error_off_duty
        "already_taken" -> R.string.courier_error_already_taken
        "other_branch" -> R.string.courier_error_other_branch
        "courier_busy" -> R.string.courier_error_courier_busy
        "not_ready" -> R.string.courier_error_not_ready
        else -> R.string.courier_error_unknown
    }
    else -> R.string.courier_error_unknown
}

@StringRes
fun reasonRes(reason: ReturnReason?): Int = when (reason) {
    ReturnReason.NO_ANSWER -> R.string.courier_reason_no_answer
    ReturnReason.WRONG_ADDRESS -> R.string.courier_reason_wrong_address
    ReturnReason.REFUSED -> R.string.courier_reason_refused
    else -> R.string.courier_reason_other
}

/** Yetkazish narxi kuryerga: "Yetkazish: 10 000 so'm", `0` — "Yetkazish: Bepul", `null` — "o'zingiz aytasiz". */
@Composable
@ReadOnlyComposable
fun courierFeeText(fee: Long?): String = when (fee) {
    null -> stringResource(R.string.courier_fee_unknown)
    0L -> stringResource(R.string.courier_fee_line, stringResource(R.string.free))
    else -> stringResource(R.string.courier_fee_line, money(fee))
}
