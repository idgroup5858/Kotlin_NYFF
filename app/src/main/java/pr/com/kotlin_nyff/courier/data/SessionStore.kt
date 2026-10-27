package pr.com.kotlin_nyff.courier.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pr.com.kotlin_nyff.courier.di.CourierStore
import pr.com.kotlin_nyff.di.ApplicationScope
import javax.inject.Inject
import javax.inject.Singleton

/** Kirgan kuryer: JWT (24 soat) va ismi. */
data class Session(val token: String, val userId: String, val name: String)

/**
 * Kuryer sessiyasi telefonda (alohida DataStore) saqlanadi — kuryer kirgan bo'lsa ilova har safar kuryer ekranida
 * ochiladi, token eskirguncha parol so'ralmaydi. Token eskirsa yoki kuryer bloklansa (server `401`) — [expire]: kirish
 * oynasi ochiladi. "Chiqish" — [clear]: ilova mijoz qismiga qaytadi.
 */
@Singleton
class SessionStore @Inject constructor(
    @param:CourierStore private val store: DataStore<Preferences>,
    @param:ApplicationScope private val scope: CoroutineScope,
) {

    private val _session = MutableStateFlow<Session?>(null)
    val session: StateFlow<Session?> = _session.asStateFlow()

    /** DataStore o'qilguncha `false` — ilova mijoz qismini bir lahza ko'rsatib yubormasin. */
    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    /** Kuryer kirish oynasi ochiq (mijoz "NY" belgisiga 10 marta bosdi yoki sessiya eskirdi). */
    private val _entry = MutableStateFlow(false)
    val entry: StateFlow<Boolean> = _entry.asStateFlow()

    /** OkHttp interceptor uchun (har so'rovda). */
    val token: String? get() = _session.value?.token

    init {
        scope.launch {
            val prefs = store.data.first()
            val token = prefs[TOKEN]
            if (token != null && _session.value == null) {
                _session.value = Session(token, prefs[USER_ID].orEmpty(), prefs[NAME].orEmpty())
            }
            _loaded.value = true
        }
    }

    fun openEntry() {
        _entry.value = true
    }

    /** Kirish oynasidan orqaga — mijoz qismiga. */
    fun closeEntry() {
        _entry.value = false
    }

    suspend fun save(session: Session) {
        _session.value = session
        store.edit {
            it[TOKEN] = session.token
            it[USER_ID] = session.userId
            it[NAME] = session.name
        }
    }

    /** "Chiqish" — sessiya o'chadi, ilova mijoz qismiga qaytadi. */
    suspend fun clear() {
        _entry.value = false
        forget()
    }

    /** Server tokenni rad etdi — tarmoq oqimidan chaqiriladi; kuryer qaytadan kiradi. */
    fun expire() {
        _entry.value = true
        scope.launch { forget() }
    }

    private suspend fun forget() {
        _session.value = null
        store.edit {
            it.remove(TOKEN)
            it.remove(USER_ID)
            it.remove(NAME)
        }
    }

    private companion object {
        val TOKEN = stringPreferencesKey("token")
        val USER_ID = stringPreferencesKey("user_id")
        val NAME = stringPreferencesKey("user_name")
    }
}
