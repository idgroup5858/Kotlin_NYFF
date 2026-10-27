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

/** Mijoz ma'lumotlari — keyingi buyurtmada qayta yozilmasin. `phone` — `+998` dan keyingi 9 ta raqam. */
data class Contact(
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val payment: OnlinePayment = OnlinePayment.CASH,
)

/** Oxirgi tanlov: buyurtma turi, olib ketish filiali, dostavka nuqtasi. */
data class SavedDraft(
    val mode: OrderMode = OrderMode.DELIVERY,
    val takeawayBranchId: String? = null,
    val deliveryPoint: GeoPoint? = null,
)

@Singleton
class UserPrefs @Inject constructor(private val store: DataStore<Preferences>) {

    val contact: Flow<Contact> = store.data.map { prefs ->
        Contact(
            name = prefs[NAME].orEmpty(),
            phone = prefs[PHONE].orEmpty(),
            address = prefs[ADDRESS].orEmpty(),
            payment = OnlinePayment.entries.firstOrNull { it.name == prefs[PAYMENT] } ?: OnlinePayment.CASH,
        )
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

    suspend fun saveContact(contact: Contact) {
        store.edit {
            it[NAME] = contact.name.trim()
            it[PHONE] = contact.phone
            it[ADDRESS] = contact.address.trim()
            it[PAYMENT] = contact.payment.name
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
        val NAME = stringPreferencesKey("contact_name")
        val PHONE = stringPreferencesKey("contact_phone")
        val ADDRESS = stringPreferencesKey("contact_address")
        val PAYMENT = stringPreferencesKey("contact_payment")
        val MODE = stringPreferencesKey("draft_mode")
        val TAKEAWAY_BRANCH = stringPreferencesKey("draft_takeaway_branch")
        val DELIVERY_LAT = doublePreferencesKey("draft_delivery_lat")
        val DELIVERY_LNG = doublePreferencesKey("draft_delivery_lng")
    }
}
