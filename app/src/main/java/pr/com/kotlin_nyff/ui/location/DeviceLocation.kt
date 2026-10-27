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

    /** Aniqlab bo'lmasa (ruxsat yo'q, GPS o'chiq, 10 s ichida javob yo'q) — oxirgi ma'lum joy yoki `null`. */
    @SuppressLint("MissingPermission")
    suspend fun current(context: Context): GeoPoint? {
        if (!hasPermission(context)) return null
        val client = LocationServices.getFusedLocationProviderClient(context)
        return try {
            val token = CancellationTokenSource()
            val fresh = withTimeoutOrNull(10_000) {
                client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, token.token).await()
            }
            token.cancel()
            val location = fresh ?: client.lastLocation.await()
            location?.let { GeoPoint(it.latitude, it.longitude) }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }
}
