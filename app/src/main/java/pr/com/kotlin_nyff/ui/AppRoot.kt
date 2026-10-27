package pr.com.kotlin_nyff.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pr.com.kotlin_nyff.courier.data.SessionStore
import pr.com.kotlin_nyff.courier.ui.login.LoginScreen
import pr.com.kotlin_nyff.courier.ui.main.MainScreen
import pr.com.kotlin_nyff.ui.theme.NyffTheme

/**
 * Ilova ildizi — mijoz yoki kuryer qismi. Kuryer kirgan bo'lsa (sessiya telefonda saqlanadi) ilova har safar kuryer
 * ekranida ochiladi; mijoz "NY" belgisiga 10 marta bossa — kuryer kirish oynasi (orqaga — mijoz qismiga). Kuryer
 * "Chiqish" bossa — mijoz qismiga qaytadi.
 */
@Composable
fun AppRoot(courier: SessionStore) {
    val loaded by courier.loaded.collectAsStateWithLifecycle()
    val session by courier.session.collectAsStateWithLifecycle()
    val entry by courier.entry.collectAsStateWithLifecycle()
    when {
        // kuryer sessiyasi o'qilguncha — mijoz qismi bir lahza ko'rinib ketmasin
        !loaded -> Box(
            Modifier
                .fillMaxSize()
                .background(NyffTheme.colors.header),
        )
        session != null -> MainScreen()
        entry -> LoginScreen()
        else -> NyffNavHost(onCourierEntry = courier::openEntry)
    }
}
