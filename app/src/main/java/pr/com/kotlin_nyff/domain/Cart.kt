package pr.com.kotlin_nyff.domain

import java.util.UUID

/** Savat qatori. Narx saqlanmaydi — har doim menyu filialining joriy narxidan olinadi (web `lib/cart.ts` dagidek). */
data class CartLine(val key: String, val productId: String, val qty: Int, val note: String)

object Cart {

    /** Shu izohli (yoki izohsiz) qator bo'lsa — soni oshadi, aks holda yangi qator. */
    fun add(
        lines: List<CartLine>,
        productId: String,
        qty: Int = 1,
        note: String = "",
        newKey: () -> String = { UUID.randomUUID().toString() },
    ): List<CartLine> {
        val cleanNote = note.trim()
        val same = lines.firstOrNull { it.productId == productId && it.note == cleanNote }
            ?: return lines + CartLine(newKey(), productId, qty, cleanNote)
        return lines.map { if (it.key == same.key) it.copy(qty = it.qty + qty) else it }
    }

    /** Soni 0 ga tushsa qator o'chadi. */
    fun changeQty(lines: List<CartLine>, key: String, delta: Int): List<CartLine> = lines.mapNotNull {
        when {
            it.key != key -> it
            it.qty + delta > 0 -> it.copy(qty = it.qty + delta)
            else -> null
        }
    }

    fun remove(lines: List<CartLine>, key: String): List<CartLine> = lines.filterNot { it.key == key }

    fun qty(lines: List<CartLine>): Int = lines.sumOf { it.qty }

    /** Taom bo'yicha jami soni (menyu kartochkasidagi belgi uchun). */
    fun countsByProduct(lines: List<CartLine>): Map<String, Int> =
        lines.groupingBy { it.productId }.fold(0) { sum, line -> sum + line.qty }
}
