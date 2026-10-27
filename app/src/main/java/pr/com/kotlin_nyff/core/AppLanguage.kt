package pr.com.kotlin_nyff.core

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * Ilova tili: o'zbekcha (standart, `values/`), ruscha, inglizcha. Tanlov telefon tilidan mustaqil; Android 13+ da
 * sozlamalardagi "Ilova tili" bilan bir xil, eskisida AppCompat o'zi saqlaydi (manifestdagi `autoStoreLocales`).
 */
enum class AppLanguage(val tag: String, val label: String) {
    UZ("uz", "O'zbekcha"),
    RU("ru", "Русский"),
    EN("en", "English"),
    ;

    companion object {
        val DEFAULT = UZ

        fun fromTag(tag: String?): AppLanguage = entries.firstOrNull { it.tag == tag } ?: DEFAULT

        fun current(): AppLanguage = fromTag(AppCompatDelegate.getApplicationLocales()[0]?.language)

        /** Faol activity yangi tilda qayta yaratiladi. */
        fun apply(language: AppLanguage) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
        }

        /** Birinchi ishga tushishda (til hali tanlanmagan) — o'zbekcha. `Activity.onCreate` da chaqiriladi. */
        fun ensureChosen() {
            if (AppCompatDelegate.getApplicationLocales().isEmpty) apply(DEFAULT)
        }
    }
}
