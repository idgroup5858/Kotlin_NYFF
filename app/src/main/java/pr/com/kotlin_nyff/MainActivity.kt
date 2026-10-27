package pr.com.kotlin_nyff

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import pr.com.kotlin_nyff.core.AppLanguage
import pr.com.kotlin_nyff.courier.data.SessionStore
import pr.com.kotlin_nyff.ui.AppRoot
import pr.com.kotlin_nyff.ui.theme.NyffTheme
import javax.inject.Inject

/** AppCompatActivity — ilova tili (AppCompatDelegate) Android 12 va undan eskisida ham qo'llanishi uchun. */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    /** Kuryer qismi sessiyasi — ilova qaysi qismda ochilishini hal qiladi. */
    @Inject
    lateinit var courierSession: SessionStore

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        AppLanguage.ensureChosen()
        setContent {
            NyffTheme {
                AppRoot(courierSession)
            }
        }
    }
}
