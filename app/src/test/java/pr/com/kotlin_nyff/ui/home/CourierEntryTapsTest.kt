package pr.com.kotlin_nyff.ui.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** "NY" belgisiga 10 marta bosish — kuryer kirishi. */
class CourierEntryTapsTest {

    @Test
    fun `ketma-ket 10 marta — ochiladi, keyin hisob boshidan`() {
        val taps = CourierEntryTaps()
        val results = (1..10).map { taps.tap(it * 300L) }
        assertEquals(List(9) { false } + true, results)
        // yana 9 tasi yetmaydi
        assertFalse((11..19).map { taps.tap(it * 300L) }.any { it })
        assertTrue(taps.tap(20 * 300L))
    }

    @Test
    fun `orada uzoq to'xtalsa — hisob boshidan`() {
        val taps = CourierEntryTaps()
        repeat(9) { taps.tap(it * 200L) }
        // 9-dan keyin 2 soniya o'tdi — bu birinchi bosish sanaladi
        assertFalse(taps.tap(8 * 200L + 2_000))
        assertFalse((1..8).map { taps.tap(8 * 200L + 2_000 + it * 200L) }.any { it })
        assertTrue(taps.tap(8 * 200L + 2_000 + 9 * 200L))
    }
}
