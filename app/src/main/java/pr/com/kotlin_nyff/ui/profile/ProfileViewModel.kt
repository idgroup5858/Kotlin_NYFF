package pr.com.kotlin_nyff.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pr.com.kotlin_nyff.data.local.Profile
import pr.com.kotlin_nyff.data.local.UserPrefs
import pr.com.kotlin_nyff.domain.Phone
import javax.inject.Inject

/** `phone` — `+998` dan keyingi raqamlar (rasmiylashtirishdagidek). */
data class ProfileUiState(
    val name: String = "",
    val phone: String = "",
    val loaded: Boolean = false,
    val showProblems: Boolean = false,
    val saving: Boolean = false,
) {
    /** Telefon ixtiyoriy, lekin yozilgan bo'lsa — to'liq 9 raqam. */
    val phoneIncomplete: Boolean get() = phone.isNotEmpty() && !Phone.isComplete(phone)
}

/** Profil: ism va telefon — rasmiylashtirishda maydonlarga o'zi qo'yiladi (`UserPrefs.profile`, DataStore). */
@HiltViewModel
class ProfileViewModel @Inject constructor(private val prefs: UserPrefs) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    private val done = Channel<Unit>(Channel.BUFFERED)

    /** Saqlandi — ekran yopiladi. */
    val saved: Flow<Unit> = done.receiveAsFlow()

    init {
        viewModelScope.launch {
            val profile = prefs.profile.first()
            // yuklanguncha yozila boshlangan maydon bosib ketilmasin
            _state.update {
                it.copy(name = it.name.ifEmpty { profile.name }, phone = it.phone.ifEmpty { profile.phone }, loaded = true)
            }
        }
    }

    fun setName(value: String) = _state.update { it.copy(name = value.take(100)) }

    fun setPhone(value: String) = _state.update { it.copy(phone = Phone.clean(value)) }

    fun save() {
        val current = _state.value
        // yuklanmasdan saqlansa — saqlangan profil bo'sh maydonlar bilan almashib qoladi
        if (current.saving || !current.loaded) return
        if (current.phoneIncomplete) {
            _state.update { it.copy(showProblems = true) }
            return
        }
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            prefs.saveProfile(Profile(name = current.name.trim(), phone = current.phone))
            _state.update { it.copy(saving = false) }
            done.send(Unit)
        }
    }
}
