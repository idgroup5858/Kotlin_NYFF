package pr.com.kotlin_nyff

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.LocaleListCompat
import dagger.hilt.android.AndroidEntryPoint
import pr.com.kotlin_nyff.core.AppLanguage
import pr.com.kotlin_nyff.core.AppTheme
import pr.com.kotlin_nyff.courier.data.SessionStore
import pr.com.kotlin_nyff.ui.AppRoot
import pr.com.kotlin_nyff.ui.theme.NyffTheme
import javax.inject.Inject

/**
 * AppCompatActivity — ilova tili (AppCompatDelegate) Android 12 va undan eskisida ham qo'llanishi uchun. Mavzu va til
 * almashganda qayta yaratilmaydi (manifestda `configChanges="locale|layoutDirection|uiMode"`) — o'zgarish joyida.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    /** Kuryer qismi sessiyasi — ilova qaysi qismda ochilishini hal qiladi. */
    @Inject
    lateinit var courierSession: SessionStore

    override fun onCreate(savedInstanceState: Bundle?) {
        applyEdgeToEdge()
        super.onCreate(savedInstanceState)
        AppLanguage.ensureChosen()
        setContent {
            NyffTheme {
                AppRoot(courierSession)
            }
        }
    }

    /** Tungi rejim almashdi (AppCompat resurslarni yangiladi) — oyna foni va tizim panellari ham shu rejimga. */
    override fun onNightModeChanged(mode: Int) {
        super.onNightModeChanged(mode)
        window?.setBackgroundDrawableResource(R.color.nyff_window)
        applyEdgeToEdge()
    }

    /**
     * Til almashdi. Android 12 va eskisida AppCompat resurslarni yangilab, faqat activity'ni xabardor qiladi — Compose
     * (`LocalConfiguration`, `stringResource`) yangi tilni ko'rishi uchun o'zgarish ekran ko'rinishlariga ham uzatiladi.
     * Android 13+ da buni tizim o'zi qiladi (yana bir marta uzatilishi zarar qilmaydi).
     */
    override fun onLocalesChanged(locales: LocaleListCompat) {
        super.onLocalesChanged(locales)
        window?.decorView?.dispatchConfigurationChanged(resources.configuration)
    }

    /** Panellar shaffof; 3 tugmali navigatsiyada pastki panel foni — ilova mavzusiga qarab (telefon sozlamasiga emas). */
    private fun applyEdgeToEdge() {
        val dark = { _: android.content.res.Resources -> AppTheme.isDark }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT, dark),
            navigationBarStyle = SystemBarStyle.auto(LIGHT_SCRIM, DARK_SCRIM, dark),
        )
    }

    private companion object {
        // androidx.activity dagi standart qiymatlar
        val LIGHT_SCRIM = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
        val DARK_SCRIM = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
    }
}
