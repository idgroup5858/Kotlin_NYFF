package pr.com.kotlin_nyff.ui.home

/**
 * Yashirin kuryer kirishi: "NY" belgisiga ketma-ket [count] marta bosilsa (bosishlar oralig'i [gapMs] dan oshmasin) —
 * [tap] `true` qaytaradi va hisob boshidan boshlanadi. Belgi, ripple, xabar yo'q — mijoz sezmaydi.
 */
class CourierEntryTaps(private val count: Int = 10, private val gapMs: Long = 1_500) {

    private var taps = 0
    private var last = Long.MIN_VALUE

    /** [now] — monoton vaqt (ms). Yetarli bosildi — `true`. */
    fun tap(now: Long): Boolean {
        taps = if (last == Long.MIN_VALUE || now - last > gapMs) 1 else taps + 1
        last = now
        if (taps < count) return false
        taps = 0
        last = Long.MIN_VALUE
        return true
    }
}
