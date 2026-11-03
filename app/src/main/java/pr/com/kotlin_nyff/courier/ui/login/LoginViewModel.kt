package pr.com.kotlin_nyff.courier.ui.login

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pr.com.kotlin_nyff.R
import pr.com.kotlin_nyff.courier.data.CourierLoginRequest
import pr.com.kotlin_nyff.courier.data.Session
import pr.com.kotlin_nyff.courier.data.SessionStore
import pr.com.kotlin_nyff.courier.data.remote.CourierApi
import pr.com.kotlin_nyff.courier.ui.courierErrorRes
import pr.com.kotlin_nyff.data.remote.AppError
import pr.com.kotlin_nyff.data.remote.apiCall
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val busy: Boolean = false,
    @param:StringRes val error: Int? = null,
) {
    val canSubmit: Boolean get() = email.isNotBlank() && password.isNotEmpty() && !busy
}

/**
 * Kuryer kirishi (API.md 4.3): admin bergan email va parol → token (ismlar ro'yxati yo'q). Email katta-kichik harfni
 * farqlamaydi (server kichik harfga o'tkazadi). Sessiya telefonda qoladi — ilova keyingi safar to'g'ridan-to'g'ri kuryer
 * ekranida ochiladi.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val api: CourierApi,
    private val session: SessionStore,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun setEmail(value: String) = _state.update { it.copy(email = value.filterNot(Char::isWhitespace).take(100), error = null) }

    fun setPassword(value: String) = _state.update { it.copy(password = value.take(64), error = null) }

    fun login() {
        val current = _state.value
        if (!current.canSubmit) return
        _state.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            try {
                val result = apiCall { api.login(CourierLoginRequest(current.email, current.password)) }
                if (result.user.role != "courier") {
                    _state.update { it.copy(busy = false, error = R.string.courier_error_forbidden) }
                    return@launch
                }
                session.save(Session(result.token, result.user.id, result.user.fullName))
                _state.value = LoginUiState()
            } catch (e: AppError) {
                _state.update { it.copy(busy = false, password = "", error = courierErrorRes(e)) }
            }
        }
    }

    /** Mijoz qismiga qaytish. */
    fun back() = session.closeEntry()
}
