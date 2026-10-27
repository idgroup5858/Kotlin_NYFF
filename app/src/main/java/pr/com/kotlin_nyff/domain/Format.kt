package pr.com.kotlin_nyff.domain

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToLong

/** Uzilmaydigan bo'shliq: "16 866 / 000 so'm" bo'lib ikki qatorga bo'linmasin (web `lib/format.ts` dagidek). */
const val NBSP = ' '

object Format {

    /** 1234567 → "1 234 567" (til sozlamasidan qat'i nazar bir xil). */
    fun number(value: Long): String {
        val digits = kotlin.math.abs(value).toString()
        val grouped = digits.reversed().chunked(3).joinToString(NBSP.toString()).reversed()
        return if (value < 0) "−$grouped" else grouped
    }

    /** "32 000 so'm" — `unit` tarjimadan (`so'm` / `сум` / `UZS`). */
    fun money(value: Long, unit: String): String = "${number(value)}$NBSP$unit"

    /** Buyurtma raqami: 7 → "#007" — TV, kassa va chekda bir xil. */
    fun orderNumber(number: Int): String = "#" + number.toString().padStart(3, '0')

    /** Masofa (kuryer): 3.456 → "3,5" (web'dagidek vergul bilan, bir xona). */
    fun km(value: Double): String {
        val tenths = (value * 10).roundToLong()
        return "${tenths / 10},${tenths % 10}"
    }

    private val time = DateTimeFormatter.ofPattern("HH:mm")
    private val dateTime = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

    /** ISO vaqt → "14:05" (telefon vaqt zonasida). */
    fun time(iso: String?, zone: ZoneId = ZoneId.systemDefault()): String =
        parse(iso)?.atZone(zone)?.format(time).orEmpty()

    /** ISO vaqt → "23.10.2026 14:05". */
    fun dateTime(iso: String?, zone: ZoneId = ZoneId.systemDefault()): String =
        parse(iso)?.atZone(zone)?.format(dateTime).orEmpty()

    fun parse(iso: String?): Instant? = iso?.let { runCatching { Instant.parse(it) }.getOrNull() }

    /** Soniyalar → "3:07". */
    fun countdown(seconds: Long): String {
        val safe = seconds.coerceAtLeast(0)
        return "${safe / 60}:${(safe % 60).toString().padStart(2, '0')}"
    }
}

/** Telefon: `+998` dan keyingi 9 ta raqam saqlanadi, serverga "+998 90 123 45 67" ko'rinishida yuboriladi. */
object Phone {
    const val PREFIX = "+998"
    const val LENGTH = 9

    /** Kiritilgan matndan faqat raqamlar; to'liq raqam (`+998 90 …`) qo'yilsa — `998` tashlanadi. */
    fun clean(input: String): String {
        val digits = input.filter(Char::isDigit)
        val local = if (digits.length > LENGTH && digits.startsWith("998")) digits.drop(3) else digits
        return local.take(LENGTH)
    }

    fun isComplete(local: String): Boolean = local.length == LENGTH && local.all(Char::isDigit)

    /** "901234567" → "90 123 45 67" (to'liq bo'lmasa — bor qismi). */
    fun group(local: String): String = listOf(
        local.take(2),
        local.drop(2).take(3),
        local.drop(5).take(2),
        local.drop(7).take(2),
    ).filter { it.isNotEmpty() }.joinToString(" ")

    fun full(local: String): String = "$PREFIX ${group(local)}"
}
