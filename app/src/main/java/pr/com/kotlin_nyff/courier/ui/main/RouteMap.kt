package pr.com.kotlin_nyff.courier.ui.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import pr.com.kotlin_nyff.courier.data.CourierOrder
import pr.com.kotlin_nyff.data.model.GeoPoint
import pr.com.kotlin_nyff.ui.theme.Coal
import pr.com.kotlin_nyff.ui.theme.Select
import pr.com.kotlin_nyff.ui.theme.Taxi

private val TASHKENT = LatLng(41.311, 69.279)

private fun GeoPoint.toLatLng() = LatLng(lat, lng)

fun hasLocationPermission(context: Context): Boolean =
    listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION).any {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

/**
 * Yo'l xaritasi (Maps SDK for Android — bepul): boshlanish (filial yoki oxirgi yetkazilgan joy), manzillar server
 * taklif qilgan tartibda raqamlangan va to'g'ri chiziq bilan ulangan. Ko'chalar bo'yicha yo'lni Google / Yandex ilovasi
 * chizadi (pullik Routes API ishlatilmaydi).
 */
@Composable
fun RouteMap(start: GeoPoint?, startIsBranch: Boolean, stops: List<CourierOrder>, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val located = stops.mapNotNull { order -> order.location?.let { order to it } }
    val path = listOfNotNull(start) + located.map { it.second }
    val camera = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(path.firstOrNull()?.toLatLng() ?: TASHKENT, 13f)
    }
    var loaded by remember { mutableStateOf(false) }

    // hamma nuqta ko'rinsin
    LaunchedEffect(loaded, path) {
        if (!loaded || path.isEmpty()) return@LaunchedEffect
        if (path.size == 1) {
            camera.move(CameraUpdateFactory.newLatLngZoom(path.first().toLatLng(), 15f))
        } else {
            val bounds = LatLngBounds.builder().apply { path.forEach { include(it.toLatLng()) } }.build()
            camera.move(CameraUpdateFactory.newLatLngBounds(bounds, 120))
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = camera,
        properties = MapProperties(isMyLocationEnabled = hasLocationPermission(context)),
        uiSettings = MapUiSettings(zoomControlsEnabled = false, mapToolbarEnabled = false, myLocationButtonEnabled = false),
        mapColorScheme = ComposeMapColorScheme.FOLLOW_SYSTEM,
        onMapLoaded = { loaded = true },
    ) {
        if (path.size >= 2) {
            Polyline(
                points = path.map { it.toLatLng() },
                color = Select,
                width = 9f,
                pattern = listOf(Dash(28f), Gap(16f)),
            )
        }
        if (start != null) {
            val state = remember(start) { MarkerState(start.toLatLng()) }
            MarkerComposable(startIsBranch, state = state, anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f)) {
                StartPin(startIsBranch)
            }
        }
        located.forEachIndexed { index, (order, point) ->
            val state = remember(order.id, point) { MarkerState(point.toLatLng()) }
            val number = order.courierSeq ?: (index + 1)
            MarkerComposable(order.id, number, index == 0, state = state, anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f)) {
                NumberPin(number, next = index == 0)
            }
        }
    }
}

@Composable
private fun StartPin(isBranch: Boolean) {
    Box(
        Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(Coal)
            .border(2.dp, Taxi, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(if (isBranch) Icons.Outlined.Storefront else Icons.Filled.Check, contentDescription = null, tint = Taxi, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun NumberPin(number: Int, next: Boolean) {
    Box(
        Modifier
            .size(if (next) 38.dp else 32.dp)
            .clip(CircleShape)
            .background(Taxi)
            .border(if (next) 3.dp else 2.dp, Coal, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(number.toString(), color = Coal, fontWeight = FontWeight.Black, fontSize = if (next) 16.sp else 14.sp)
    }
}
