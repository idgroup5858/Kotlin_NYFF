package pr.com.kotlin_nyff.courier.domain

import pr.com.kotlin_nyff.data.model.GeoPoint

/**
 * Google Maps va Yandex havolalari — bepul, kalitsiz; yo'lni kuryer telefonidagi ilova o'zi chizadi, boshlanish —
 * telefonning joriy joyi (ROADMAP 2, API.md 12).
 */
object MapLinks {

    private fun GeoPoint.text() = "$lat,$lng"

    fun google(point: GeoPoint): String =
        "https://www.google.com/maps/dir/?api=1&destination=${point.text()}&travelmode=driving"

    fun yandex(point: GeoPoint): String = "https://yandex.uz/maps/?rtext=~${point.text()}&rtt=auto"

    /**
     * Hamma manzil tartib bilan: `/maps/dir//A/B/C` (boshida bo'sh — joriy joy). `api=1&waypoints=` formati Android'dagi
     * Google Maps ilovasida oraliq nuqtalarni tashlab yuboradi (emulyatorda tekshirilgan) — yo'l ichidagi format esa
     * hamma to'xtash joyini oladi.
     */
    fun googleRoute(points: List<GeoPoint>): String? {
        if (points.isEmpty()) return null
        return "https://www.google.com/maps/dir//" + points.joinToString("/") { it.text() }
    }

    /** Yandex: `rtext=~A~B~C` (boshida bo'sh — joriy joy). */
    fun yandexRoute(points: List<GeoPoint>): String? {
        if (points.isEmpty()) return null
        return "https://yandex.uz/maps/?rtext=~" + points.joinToString("~") { it.text() } + "&rtt=auto"
    }
}
