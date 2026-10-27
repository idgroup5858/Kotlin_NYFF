package pr.com.kotlin_nyff.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

class FormatTest {

    @Test
    fun `summa uzilmaydigan bo'shliq bilan guruhlanadi`() {
        assertEquals("32${NBSP}000${NBSP}so'm", Format.money(32_000, "so'm"))
        assertEquals("1${NBSP}234${NBSP}567", Format.number(1_234_567))
        assertEquals("0", Format.number(0))
        assertEquals("999", Format.number(999))
        assertEquals("−5${NBSP}000", Format.number(-5_000))
    }

    @Test
    fun `buyurtma raqami uch xonali`() {
        assertEquals("#007", Format.orderNumber(7))
        assertEquals("#017", Format.orderNumber(17))
        assertEquals("#1234", Format.orderNumber(1234))
    }

    @Test
    fun `vaqt Toshkent zonasida`() {
        val tashkent = ZoneId.of("Asia/Tashkent")
        assertEquals("10:12", Format.time("2026-10-27T05:12:33.123Z", tashkent))
        assertEquals("27.10.2026 10:12", Format.dateTime("2026-10-27T05:12:33.123Z", tashkent))
        assertEquals("", Format.time(null, tashkent))
        assertEquals("", Format.time("buzuq", tashkent))
    }

    @Test
    fun `teskari sanoq`() {
        assertEquals("5:00", Format.countdown(300))
        assertEquals("3:07", Format.countdown(187))
        assertEquals("0:00", Format.countdown(-4))
    }

    @Test
    fun `telefon — faqat 9 ta raqam`() {
        assertEquals("901234567", Phone.clean("90 123-45-67"))
        assertEquals("901234567", Phone.clean("+998 90 123 45 67"))
        assertEquals("9012345", Phone.clean("9012345"))
        assertEquals("901234567", Phone.clean("9012345678"))
        assertTrue(Phone.isComplete("901234567"))
        assertFalse(Phone.isComplete("90123456"))
    }

    @Test
    fun `telefon ko'rinishi`() {
        assertEquals("90 123 45 67", Phone.group("901234567"))
        assertEquals("90 12", Phone.group("9012"))
        assertEquals("90", Phone.group("90"))
        assertEquals("", Phone.group(""))
        assertEquals("+998 90 123 45 67", Phone.full("901234567"))
    }
}
