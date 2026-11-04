package pr.com.kotlin_nyff.courier.ui.main

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

/**
 * Google Maps / Yandex Go havolasi — o'rnatilgan ilovada (bo'lmasa brauzerda) ochiladi; [fallback] — [url] ni ochadigan
 * ilova bo'lmasa (masalan `yandextaxi://` sxemasi, Yandex Go o'rnatilmagan).
 */
fun Context.openLink(url: String, fallback: String? = null) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: ActivityNotFoundException) {
        // havolani ochadigan ilova yo'q
        if (fallback != null) openLink(fallback)
    }
}

/** Raqam terish oynasi (qo'ng'iroqni kuryer o'zi bosadi). */
fun Context.dial(phone: String) {
    val digits = phone.filter { it.isDigit() || it == '+' }
    if (digits.isEmpty()) return
    try {
        startActivity(Intent(Intent.ACTION_DIAL, "tel:$digits".toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: ActivityNotFoundException) {
        // telefon ilovasi yo'q (planshet)
    }
}
