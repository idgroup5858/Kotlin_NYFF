package pr.com.kotlin_nyff.courier.ui.main

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pr.com.kotlin_nyff.courier.data.AvailableOrder
import pr.com.kotlin_nyff.data.model.Branch
import pr.com.kotlin_nyff.courier.data.CourierBlock
import pr.com.kotlin_nyff.courier.data.CourierState
import pr.com.kotlin_nyff.data.model.OrderStatus
import pr.com.kotlin_nyff.courier.data.StaffMember

/** "Yangi" ro'yxati filial bo'yicha: kuryer tanlaydi, qo'lda buyurtma bo'lsa — o'sha filialga qulf. */
class BranchPickTest {

    private val branches = listOf(
        Branch("a", "Luchvoy"),
        Branch("b", "Gurlenskiy"),
        Branch("c", "Yopiq", active = false),
    ).associateBy { it.id }

    private fun order(id: String, branchId: String, blocked: CourierBlock? = null, status: OrderStatus = OrderStatus.PREPARING) =
        AvailableOrder(id = id, branchId = branchId, number = 1, status = status, createdAt = "2026-10-27T05:00:00Z", blocked = blocked)

    private val available = listOf(
        order("a1", "a"),
        order("a2", "a", status = OrderStatus.READY),
        order("b1", "b"),
        order("b2", "b", blocked = CourierBlock.OFF_DUTY),
    )

    private fun state(chosen: String?, holding: String? = null) = MainUiState(
        me = CourierState(StaffMember("u", "Bobur", "courier"), onDuty = true, branchId = holding),
        branches = branches,
        available = available,
        chosenBranchId = chosen,
    )

    @Test
    fun `tanlanmagan — ro'yxat bo'sh, tab sonida hammasi`() {
        val s = state(chosen = null)
        assertNull(s.pickBranchId)
        assertTrue(s.shownAvailable.isEmpty())
        assertEquals(3, s.newCount)
        assertFalse(s.branchLocked)
    }

    @Test
    fun `tanlangan filial — faqat uning buyurtmalari, tayyorlanayotgani ham`() {
        val s = state(chosen = "a")
        assertEquals(listOf("a1", "a2"), s.shownAvailable.map { it.id })
        assertEquals(2, s.newCount)
        assertEquals(2, s.availableCount("b"))
        assertEquals(1, state(chosen = "b").newCount)
    }

    @Test
    fun `qo'lda buyurtma — o'sha filialga qulf, tanlov e'tiborga olinmaydi`() {
        val s = state(chosen = "a", holding = "b")
        assertEquals("b", s.pickBranchId)
        assertTrue(s.branchLocked)
        assertEquals(listOf("b1", "b2"), s.shownAvailable.map { it.id })
    }

    @Test
    fun `faol bo'lmagan yoki noma'lum filial tanlanmagan hisoblanadi, tugmalarda faqat faollari`() {
        assertNull(state(chosen = "c").pickBranchId)
        assertNull(state(chosen = "x").pickBranchId)
        assertEquals(listOf("a", "b"), state(chosen = null).pickBranches.map { it.id })
        // qo'ldagilar filiali yopilgan bo'lsa ham ko'rinadi
        assertEquals(listOf("a", "b", "c"), state(chosen = null, holding = "c").pickBranches.map { it.id })
    }
}
