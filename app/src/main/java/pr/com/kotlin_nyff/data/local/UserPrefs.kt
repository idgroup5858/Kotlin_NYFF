package pr.com.kotlin_nyff.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pr.com.kotlin_nyff.data.model.GeoPoint
import pr.com.kotlin_nyff.data.model.OnlinePayment
import pr.com.kotlin_nyff.domain.OrderMode
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Rasmiylashtirish formasi. `phone` — `+998` dan keyingi 9 ta raqam. Ism va telefon [Profile] dan qo'yiladi (shu
 * buyurtma uchun o'zgartirilsa profil o'zgarmaydi); manzil saqlanmaydi; to'lov turi eslab qolinadi.
 */
data class Contact(
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val payment: OnlinePayment = OnlinePayment.CASH,
)

/**
 * Profil ("⋮" → "Profil"): ism va telefon (`+998` dan keyingi 9 raqam yoki bo'sh) — rasmiylashtirishda maydonlarga
 * o'zi qo'yiladi. Faqat telefonda (DataStore), serverga alohida yuborilmaydi.
 */
data class Profile(
    val name: String = "",
    val phone: String = "",
)

/** Oxirgi tanlov: buyurtma turi, olib ketish filiali, dostavka nuqtasi. */
data class SavedDraft(
    val mode: OrderMode = OrderMode.DELIVERY,
    val takeawayBranchId: String? = null,
    val deliveryPoint: GeoPoint? = null,
)

@Singleton
class UserPrefs @Inject constructor(private val store: DataStore<Preferences>) {

    val payment: Flow<OnlinePayment> = store.data.map { prefs ->
        OnlinePayment.entries.firstOrNull { it.name == prefs[PAYMENT] } ?: OnlinePayment.CASH
    }

    val profile: Flow<Profile> = store.data.map { prefs ->
        Profile(name = prefs[PROFILE_NAME].orEmpty(), phone = prefs[PROFILE_PHONE].orEmpty())
    }

    val draft: Flow<SavedDraft> = store.data.map { prefs ->
        val lat = prefs[DELIVERY_LAT]
        val lng = prefs[DELIVERY_LNG]
        SavedDraft(
            mode = OrderMode.entries.firstOrNull { it.name == prefs[MODE] } ?: OrderMode.DELIVERY,
            takeawayBranchId = prefs[TAKEAWAY_BRANCH],
            deliveryPoint = if (lat != null && lng != null) GeoPoint(lat, lng) else null,
        )
    }

    suspend fun savePayment(payment: OnlinePayment) {
        store.edit {
            it[PAYMENT] = payment.name
            // oldingi versiyalar formadan saqlagan ism / telefon / manzil — endi kerak emas (profil — alohida kalitlarda)
            it.remove(OLD_NAME)
            it.remove(OLD_PHONE)
            it.remove(OLD_ADDRESS)
        }
    }

    /** Bo'sh maydon — kalit o'chiriladi. */
    suspend fun saveProfile(profile: Profile) {
        store.edit {
            if (profile.name.isNotBlank()) it[PROFILE_NAME] = profile.name else it.remove(PROFILE_NAME)
            if (profile.phone.isNotEmpty()) it[PROFILE_PHONE] = profile.phone else it.remove(PROFILE_PHONE)
        }
    }

    suspend fun saveDraft(draft: SavedDraft) {
        store.edit {
            it[MODE] = draft.mode.name
            if (draft.takeawayBranchId != null) it[TAKEAWAY_BRANCH] = draft.takeawayBranchId else it.remove(TAKEAWAY_BRANCH)
            if (draft.deliveryPoint != null) {
                it[DELIVERY_LAT] = draft.deliveryPoint.lat
                it[DELIVERY_LNG] = draft.deliveryPoint.lng
            } else {
                it.remove(DELIVERY_LAT)
                it.remove(DELIVERY_LNG)
            }
        }
    }

    private companion object {
        val OLD_NAME = stringPreferencesKey("contact_name")
        val OLD_PHONE = stringPreferencesKey("contact_phone")
        val OLD_ADDRESS = stringPreferencesKey("contact_address")
        val PAYMENT = stringPreferencesKey("contact_payment")
        val PROFILE_NAME = stringPreferencesKey("profile_name")
        val PROFILE_PHONE = stringPreferencesKey("profile_phone")
        val MODE = stringPreferencesKey("draft_mode")
        val TAKEAWAY_BRANCH = stringPreferencesKey("draft_takeaway_branch")
        val DELIVERY_LAT = doublePreferencesKey("draft_delivery_lat")
        val DELIVERY_LNG = doublePreferencesKey("draft_delivery_lng")
    }
}
