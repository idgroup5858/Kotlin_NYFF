package pr.com.kotlin_nyff

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import pr.com.kotlin_nyff.core.AppLanguage
import pr.com.kotlin_nyff.ui.NyffNavHost
import pr.com.kotlin_nyff.ui.theme.NyffTheme

/** AppCompatActivity — ilova tili (AppCompatDelegate) Android 12 va undan eskisida ham qo'llanishi uchun. */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        AppLanguage.ensureChosen()
        setContent {
            NyffTheme {
                NyffNavHost()
            }
        }
    }
}
