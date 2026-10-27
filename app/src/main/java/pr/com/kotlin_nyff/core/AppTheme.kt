package pr.com.kotlin_nyff.core

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit

/**
 * Ilova mavzusi: yorug' (standart) yoki tungi — telefon sozlamasidan mustaqil. Tanlov SharedPreferences'da (ilova
 * ochilishida sinxron o'qiladi — birinchi kadr ham to'g'ri rangda); AppCompat tungi rejimi orqali qo'llanadi — Compose
 * (`isSystemInDarkTheme`), oyna foni va `values-night` resurslari birga almashadi.
 */
enum class AppTheme(private val nightMode: Int) {
    LIGHT(AppCompatDelegate.MODE_NIGHT_NO),
    DARK(AppCompatDelegate.MODE_NIGHT_YES),
    ;

    companion object {
        val DEFAULT = LIGHT
        private const val PREFS = "nyff_settings"
        private const val KEY = "theme"

        /** `Application.onCreate` da — saqlangan (birinchi ochilishda yorug') mavzu. */
        fun init(context: Context) {
            AppCompatDelegate.setDefaultNightMode(saved(context).nightMode)
        }

        /** Faol activity yangi mavzuda qayta yaratiladi (ekran holati saqlanadi). */
        fun apply(context: Context, theme: AppTheme) {
            prefs(context).edit { putString(KEY, theme.name) }
            AppCompatDelegate.setDefaultNightMode(theme.nightMode)
        }

        private fun saved(context: Context): AppTheme =
            entries.firstOrNull { it.name == prefs(context).getString(KEY, null) } ?: DEFAULT

        private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }
}
