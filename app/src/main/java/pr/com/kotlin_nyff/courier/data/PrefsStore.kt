package pr.com.kotlin_nyff.courier.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pr.com.kotlin_nyff.courier.di.CourierStore
import javax.inject.Inject
import javax.inject.Singleton

/** Kuryerning telefondagi tanlovlari: "Yangi" ro'yxati qaysi filialniki — ilova qayta ochilganda ham eslab qolinadi. */
@Singleton
class PrefsStore @Inject constructor(@param:CourierStore private val store: DataStore<Preferences>) {

    val branchId: Flow<String?> = store.data.map { it[BRANCH] }

    suspend fun setBranch(id: String) {
        store.edit { it[BRANCH] = id }
    }

    private companion object {
        val BRANCH = stringPreferencesKey("pick_branch")
    }
}
