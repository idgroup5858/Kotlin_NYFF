package pr.com.kotlin_nyff.domain

import java.time.Instant

/**
 * Server vaqti: telefon soati noto'g'ri bo'lsa ham qabul muddati sanog'i (`routedAt` + 5 daqiqa) server bo'yicha
 * bo'lsin. Farq har HTTP javobidagi `Date` sarlavhasidan olinadi (soniya aniqligida).
 */
object ServerClock {

    @Volatile
    private var offsetMs = 0L

    fun now(): Instant = Instant.ofEpochMilli(System.currentTimeMillis() + offsetMs)

    fun update(serverTimeMs: Long, deviceTimeMs: Long = System.currentTimeMillis()) {
        offsetMs = serverTimeMs - deviceTimeMs
    }
}
