package pr.com.kotlin_nyff.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pr.com.kotlin_nyff.data.model.Branch
import pr.com.kotlin_nyff.data.model.BranchOnlineState
import pr.com.kotlin_nyff.data.model.DeliveryRoute
import pr.com.kotlin_nyff.data.model.LocalizedText
import pr.com.kotlin_nyff.data.model.Menu
import pr.com.kotlin_nyff.data.model.MenuProduct

class ShopTest {

    private fun branch(id: String, state: BranchOnlineState, active: Boolean = true) =
        Branch(id = id, name = id, active = active, onlineState = state)

    private val chilonzor = branch("br_chilonzor", BranchOnlineState.NO_KASSA)
    private val yunusobod = branch("br_yunusobod", BranchOnlineState.OPEN)
    private val old = branch("br_old", BranchOnlineState.INACTIVE, active = false)
    private val branches = listOf(old, chilonzor, yunusobod)

    @Test
    fun `faol emas filial ko'rinmaydi`() {
        assertEquals(listOf(chilonzor, yunusobod), Shop.visibleBranches(branches))
    }

    @Test
    fun `olib ketish — tanlangan filial, yopiq bo'lsa ham (ogohlantirish bilan)`() {
        assertEquals("br_chilonzor", Shop.menuBranchId(OrderMode.TAKEAWAY, branches, "br_chilonzor", null))
        assertEquals(chilonzor, Shop.takeawayBranch(branches, "br_chilonzor"))
    }

    @Test
    fun `olib ketish — tanlanmagan bo'lsa birinchi ochiq`() {
        assertEquals("br_yunusobod", Shop.menuBranchId(OrderMode.TAKEAWAY, branches, null, null))
        assertEquals(yunusobod, Shop.takeawayBranch(branches, "br_old"))
    }

    @Test
    fun `dostavka — server tanlagan filial`() {
        val route = DeliveryRoute(branchId = "br_chilonzor", zoneBranchId = "br_chilonzor", fee = 10_000)
        assertEquals("br_chilonzor", Shop.menuBranchId(OrderMode.DELIVERY, branches, "br_yunusobod", route))
    }

    @Test
    fun `dostavka — manzil yo'q yoki ochiq filial yo'q bo'lsa ko'rib turish uchun birinchi ochiq`() {
        assertEquals("br_yunusobod", Shop.menuBranchId(OrderMode.DELIVERY, branches, null, null))
        assertEquals("br_yunusobod", Shop.menuBranchId(OrderMode.DELIVERY, branches, null, DeliveryRoute(branchId = null)))
    }

    @Test
    fun `hech bir filial ochiq emas — birinchi ko'rinadigan`() {
        val closed = listOf(chilonzor, branch("br_b", BranchOnlineState.OFF_HOURS))
        assertEquals("br_chilonzor", Shop.menuBranchId(OrderMode.DELIVERY, closed, null, null))
        assertNull(Shop.takeawayBranch(closed, null))
    }

    @Test
    fun `savat narxi menyudan, menyuda yo'q taom sotilmaydi`() {
        val name = LocalizedText(uz = "Burger")
        val menu = Menu(
            branchId = "br_chilonzor",
            products = listOf(MenuProduct(id = "burger", categoryId = "c", name = name, price = 32_000)),
        )
        val lines = listOf(CartLine("a", "burger", 2, ""), CartLine("b", "hotdog-kimchi", 1, ""))
        val priced = Shop.price(lines, menu)
        assertTrue(priced[0].available)
        assertEquals(64_000, priced[0].sum)
        assertFalse(priced[1].available)
        assertEquals(0, priced[1].sum)
    }
}
