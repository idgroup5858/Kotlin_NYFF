package pr.com.kotlin_nyff.domain

import pr.com.kotlin_nyff.data.model.Branch
import pr.com.kotlin_nyff.data.model.BranchOnlineState
import pr.com.kotlin_nyff.data.model.DeliveryRoute
import pr.com.kotlin_nyff.data.model.Menu
import pr.com.kotlin_nyff.data.model.MenuProduct
import pr.com.kotlin_nyff.data.model.OrderType

/** Mobil ilovadagi buyurtma turi (zalda — yo'q). */
enum class OrderMode(val orderType: OrderType) {
    DELIVERY(OrderType.DELIVERY),
    TAKEAWAY(OrderType.TAKEAWAY),
}

/**
 * Ilova hech narsani o'zi hisoblamaydi (API.md 5): filial, narx, ochiq / yopiq — serverdan. Bu yerda faqat qaysi
 * filialning menyusini ko'rsatish tanlanadi.
 */
object Shop {

    /** Mijozga ko'rsatiladigan filiallar — faol emaslari ro'yxatda chiqmaydi. */
    fun visibleBranches(branches: List<Branch>): List<Branch> =
        branches.filter { it.active && it.onlineState != BranchOnlineState.INACTIVE }

    /**
     * Menyu va narxlar qaysi filialniki: olib ketishda — mijoz tanlagani (tanlamagan bo'lsa birinchi ochiq), dostavkada —
     * server tanlagani (`route.branchId`). Dostavka manzili hali belgilanmagan yoki ochiq filial yo'q bo'lsa — taomlarni
     * ko'rib turish uchun birinchi ochiq (yoki birinchi) filial.
     */
    fun menuBranchId(
        mode: OrderMode,
        branches: List<Branch>,
        takeawayBranchId: String?,
        route: DeliveryRoute?,
    ): String? {
        val visible = visibleBranches(branches)
        val browse = (visible.firstOrNull { it.isOpen } ?: visible.firstOrNull())?.id
        return when (mode) {
            OrderMode.TAKEAWAY -> visible.firstOrNull { it.id == takeawayBranchId }?.id ?: browse
            OrderMode.DELIVERY -> route?.branchId?.takeIf { id -> branches.any { it.id == id } } ?: browse
        }
    }

    /** Olib ketish filiali: tanlangani ro'yxatda bo'lsa o'sha, aks holda birinchi ochiq. */
    fun takeawayBranch(branches: List<Branch>, takeawayBranchId: String?): Branch? {
        val visible = visibleBranches(branches)
        return visible.firstOrNull { it.id == takeawayBranchId } ?: visible.firstOrNull { it.isOpen }
    }

    /** Bosh sahifadagi "Barchasi" — kategoriyalar ro'yxati boshida (haqiqiy kategoriya emas, faqat mobil ilovada). */
    const val ALL_CATEGORIES = "__all__"

    /** Tanlangan kategoriya: menyuda bo'lsa — o'sha, aks holda (birinchi ochilish, filial almashganda) — "Barchasi". */
    fun selectedCategory(menu: Menu?, chosen: String?): String =
        menu?.categories?.firstOrNull { it.id == chosen }?.id ?: ALL_CATEGORIES

    /**
     * Sotuvdagi taomlar: "Barchasi" — hamma kategoriyalardan, kategoriyalar tartibida (kategoriya ichida — menyudagi
     * tartib); aks holda — shu kategoriyaniki.
     */
    fun products(menu: Menu?, categoryId: String): List<MenuProduct> {
        val sold = menu?.products.orEmpty().filter { it.price != null }
        if (categoryId != ALL_CATEGORIES) return sold.filter { it.categoryId == categoryId }
        val order = menu?.categories.orEmpty().withIndex().associate { (index, category) -> category.id to index }
        return sold.filter { it.categoryId in order }.sortedBy { order.getValue(it.categoryId) }
    }

    /** Savat qatorlari menyu narxi bilan; menyuda yo'q (shu filialda sotilmaydigan) taom — `product = null`. */
    fun price(lines: List<CartLine>, menu: Menu?): List<PricedLine> = lines.map { line ->
        PricedLine(line, menu?.products?.firstOrNull { it.id == line.productId && it.price != null })
    }
}

data class PricedLine(val line: CartLine, val product: MenuProduct?) {
    val available: Boolean get() = product?.price != null
    val sum: Long get() = (product?.price ?: 0L) * line.qty
}
