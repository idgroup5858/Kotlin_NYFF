package pr.com.kotlin_nyff.core

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit

/**
 * Ilova mavzusi: yorug' (standart) yoki tungi — telefon sozlamasidan mustaqil. Tanlov SharedPreferences'da (ilova
 * ochilishida sinxron o'qiladi — birinchi kadr ham to'g'ri rangda).
 *
 * Ekran ranglari [current] (Compose holati) dan olinadi — almashtirilganda ekran joyida qayta chiziladi, activity qayta
 * yaratilmaydi (2026-11-06: qayta yaratish telefonda ~2 s qotirardi, menyu ochiq qolib ketardi). AppCompat tungi
 * rejimi ham birga almashadi (`values-night`, oyna foni) — activity `uiMode` o'zgarishini o'zi qabul qiladi (manifest).
 */
enum class AppTheme(private val nightMode: Int) {
    LIGHT(AppCompatDelegate.MODE_NIGHT_NO),
    DARK(AppCompatDelegate.MODE_NIGHT_YES),
    ;

    companion object {
        val DEFAULT = LIGHT
        private const val PREFS = "nyff_settings"
        private const val KEY = "theme"

        private val state = mutableStateOf(DEFAULT)

        /** Joriy mavzu (Compose holati — o'qigan ekranlar almashganda qayta chiziladi). */
        val current: AppTheme get() = state.value

        val isDark: Boolean get() = current == DARK

        /** `Application.onCreate` da — saqlangan (birinchi ochilishda yorug') mavzu. */
        fun init(context: Context) {
            val saved = saved(context)
            state.value = saved
            AppCompatDelegate.setDefaultNightMode(saved.nightMode)
        }

        fun apply(context: Context, theme: AppTheme) {
            prefs(context).edit { putString(KEY, theme.name) }
            state.value = theme
            AppCompatDelegate.setDefaultNightMode(theme.nightMode)
        }

        private fun saved(context: Context): AppTheme =
            entries.firstOrNull { it.name == prefs(context).getString(KEY, null) } ?: DEFAULT

        private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }
}
