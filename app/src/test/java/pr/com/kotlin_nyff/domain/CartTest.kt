package pr.com.kotlin_nyff.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CartTest {

    private var seq = 0
    private val key = { "k${++seq}" }

    @Test
    fun `bir xil taom va izoh bitta qatorga qo'shiladi`() {
        var lines = Cart.add(emptyList(), "burger", newKey = key)
        lines = Cart.add(lines, "burger", qty = 2, newKey = key)
        assertEquals(1, lines.size)
        assertEquals(3, lines.single().qty)
    }

    @Test
    fun `izohi boshqa taom alohida qator bo'ladi, izoh tozalanadi`() {
        var lines = Cart.add(emptyList(), "burger", newKey = key)
        lines = Cart.add(lines, "burger", note = "  piyozsiz ", newKey = key)
        lines = Cart.add(lines, "burger", note = "piyozsiz", newKey = key)
        assertEquals(2, lines.size)
        assertEquals("piyozsiz", lines[1].note)
        assertEquals(2, lines[1].qty)
    }

    @Test
    fun `soni nolga tushsa qator o'chadi`() {
        val lines = Cart.add(emptyList(), "fries", newKey = key)
        val k = lines.single().key
        assertEquals(2, Cart.changeQty(lines, k, 1).single().qty)
        assertTrue(Cart.changeQty(lines, k, -1).isEmpty())
    }

    @Test
    fun `taom bo'yicha jami soni`() {
        var lines = Cart.add(emptyList(), "burger", qty = 2, newKey = key)
        lines = Cart.add(lines, "burger", note = "achchiq", newKey = key)
        lines = Cart.add(lines, "fries", newKey = key)
        assertEquals(mapOf("burger" to 3, "fries" to 1), Cart.countsByProduct(lines))
        assertEquals(4, Cart.qty(lines))
    }
}
