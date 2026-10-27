package pr.com.kotlin_nyff

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import pr.com.kotlin_nyff.core.AppTheme

@HiltAndroidApp
class NyffApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // birinchi activity ochilmasdan — splash va birinchi kadr ham tanlangan mavzuda
        AppTheme.init(this)
    }
}
