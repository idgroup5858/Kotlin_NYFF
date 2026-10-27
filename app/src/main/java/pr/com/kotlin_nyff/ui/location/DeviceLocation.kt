package pr.com.kotlin_nyff.ui.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import pr.com.kotlin_nyff.data.model.GeoPoint

/** Telefonning joriy joylashuvi (Google Play xizmatlari, GPS / tarmoq). */
object DeviceLocation {

    val PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

    fun hasPermission(context: Context): Boolean = PERMISSIONS.any {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    /** Shu yoshdan yangi oxirgi joy kutmasdan olinadi. */
    private const val RECENT_MS = 2 * 60_000L

    /**
     * Yaqinda ma'lum bo'lgan joy bo'lsa — darhol o'sha; aks holda yangi aniqlash (birinchi GPS aniqlash xonada sekin —
     * 20 s gacha kutiladi), bo'lmasa — eskiroq oxirgi joy. Ruxsat yo'q yoki GPS o'chiq — `null`.
     */
    @SuppressLint("MissingPermission")
    suspend fun current(context: Context): GeoPoint? {
        if (!hasPermission(context)) return null
        val client = LocationServices.getFusedLocationProviderClient(context)
        return try {
            val last = client.lastLocation.await()
            val location = if (last != null && System.currentTimeMillis() - last.time < RECENT_MS) {
                last
            } else {
                val token = CancellationTokenSource()
                val fresh = withTimeoutOrNull(20_000) {
                    client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, token.token).await()
                }
                token.cancel()
                fresh ?: last
            }
            location?.let { GeoPoint(it.latitude, it.longitude) }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }
}
