package pr.com.kotlin_nyff.ui.common

import androidx.compose.ui.text.AnnotatedString
import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneVisualTransformationTest {

    @Test
    fun `to'liq raqam va kursor o'rinlari`() {
        val result = PhoneVisualTransformation.filter(AnnotatedString("901234567"))
        assertEquals("90 123 45 67", result.text.text)
        val map = result.offsetMapping
        // asl → ko'rinish: bo'shliqlardan keyin siljiydi
        assertEquals(0, map.originalToTransformed(0))
        assertEquals(2, map.originalToTransformed(2))
        assertEquals(4, map.originalToTransformed(3))
        assertEquals(12, map.originalToTransformed(9))
        // ko'rinish → asl: har bir o'rin uchun aylanma mos
        for (offset in 0..9) {
            assertEquals(offset, map.transformedToOriginal(map.originalToTransformed(offset)))
        }
        assertEquals(9, map.transformedToOriginal(12))
    }

    @Test
    fun `chala raqam`() {
        val result = PhoneVisualTransformation.filter(AnnotatedString("9012"))
        assertEquals("90 12", result.text.text)
        assertEquals(5, result.offsetMapping.originalToTransformed(4))
        assertEquals(4, result.offsetMapping.transformedToOriginal(5))
        assertEquals("", PhoneVisualTransformation.filter(AnnotatedString("")).text.text)
    }
}
