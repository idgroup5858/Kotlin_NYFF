package pr.com.kotlin_nyff.courier.domain

import pr.com.kotlin_nyff.data.model.GeoPoint

/**
 * Google Maps va Yandex Go havolalari — bepul, kalitsiz; yo'lni kuryer telefonidagi ilova o'zi chizadi, boshlanish —
 * telefonning joriy joyi (ROADMAP 2, API.md 12).
 */
object MapLinks {

    /** Yandex Go'ning rasmiy havolalaridagi belgi: ilova o'rnatilmagan bo'lsa — Play Market sahifasiga. */
    private const val YANDEX_GO_TRACKING = "1178268795219780156"

    private fun GeoPoint.text() = "$lat,$lng"

    fun google(point: GeoPoint): String =
        "https://www.google.com/maps/dir/?api=1&destination=${point.text()}&travelmode=driving"

    /** Yandex Go (taksi) — ilova sxemasi: o'rnatilgan bo'lsa brauzersiz to'g'ridan-to'g'ri ochiladi, borish joyi — manzil. */
    fun yandexGo(point: GeoPoint): String =
        "yandextaxi://route?end-lat=${point.lat}&end-lon=${point.lng}&appmetrica_tracking_id=$YANDEX_GO_TRACKING"

    /** Sxema ochilmasa — rasmiy havola (AppMetrica): ilova bo'lsa uni, bo'lmasa Play Market'ni ochadi. */
    fun yandexGoWeb(point: GeoPoint): String =
        "https://3.redirect.appmetrica.yandex.com/route?end-lat=${point.lat}&end-lon=${point.lng}" +
            "&appmetrica_tracking_id=$YANDEX_GO_TRACKING"

    /**
     * Hamma manzil tartib bilan: `/maps/dir//A/B/C` (boshida bo'sh — joriy joy). `api=1&waypoints=` formati Android'dagi
     * Google Maps ilovasida oraliq nuqtalarni tashlab yuboradi (emulyatorda tekshirilgan) — yo'l ichidagi format esa
     * hamma to'xtash joyini oladi. Yandex Go (taksi) bir nechta to'xtash joyini havola orqali olmaydi — butun yo'l faqat
     * Google'da.
     */
    fun googleRoute(points: List<GeoPoint>): String? {
        if (points.isEmpty()) return null
        return "https://www.google.com/maps/dir//" + points.joinToString("/") { it.text() }
    }
}
