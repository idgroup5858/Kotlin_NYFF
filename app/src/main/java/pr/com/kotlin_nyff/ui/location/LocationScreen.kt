package pr.com.kotlin_nyff.ui.location

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import pr.com.kotlin_nyff.R
import pr.com.kotlin_nyff.ui.common.Notice
import pr.com.kotlin_nyff.ui.common.PillButton
import pr.com.kotlin_nyff.ui.common.PrimaryButton
import pr.com.kotlin_nyff.ui.common.RoundIconButton
import pr.com.kotlin_nyff.ui.common.SystemBarIcons
import pr.com.kotlin_nyff.ui.common.Tone
import pr.com.kotlin_nyff.ui.common.errorRes
import pr.com.kotlin_nyff.ui.common.feeText
import pr.com.kotlin_nyff.ui.theme.Coal
import pr.com.kotlin_nyff.ui.theme.NyffShapes
import pr.com.kotlin_nyff.ui.theme.NyffTheme
import pr.com.kotlin_nyff.ui.theme.Taxi

/** Toshkent markazi — joylashuv aniqlanmasa xarita shu yerdan ochiladi. */
private val TASHKENT = LatLng(41.311, 69.279)

/**
 * Yetkazib berish joyi: xarita joriy joylashuvda ochiladi, mijoz xaritani surib pinni aniq joyga keltiradi. Har
 * to'xtashda server qaysi filial olishini va hudud narxini aytadi (ROADMAP 1, API.md 5.2).
 */
@Composable
fun LocationScreen(onBack: () -> Unit, vm: LocationViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val colors = NyffTheme.colors
    val state by vm.state.collectAsStateWithLifecycle()
    val branches by vm.branches.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val start = vm.initialPoint
    val camera = rememberCameraPositionState {
        position = if (start != null) {
            CameraPosition.fromLatLngZoom(LatLng(start.lat, start.lng), 16f)
        } else {
            CameraPosition.fromLatLngZoom(TASHKENT, 12f)
        }
    }
    var hasPermission by remember { mutableStateOf(DeviceLocation.hasPermission(context)) }
    var locating by remember { mutableStateOf(false) }
    var message by rememberSaveable { mutableStateOf<Int?>(null) }

    fun locate() {
        scope.launch {
            locating = true
            message = null
            val point = DeviceLocation.current(context)
            locating = false
            if (point == null) {
                message = R.string.locate_failed
            } else {
                camera.animate(CameraUpdateFactory.newLatLngZoom(LatLng(point.lat, point.lng), 16f))
            }
        }
    }

    val permissions = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        hasPermission = result.values.any { it }
        if (hasPermission) locate() else message = R.string.locate_denied
    }

    fun requestLocation() {
        if (hasPermission) locate() else permissions.launch(DeviceLocation.PERMISSIONS)
    }

    // Nuqta hali tanlanmagan bo'lsa — darhol joriy joylashuv
    LaunchedEffect(Unit) {
        if (start == null) requestLocation()
    }
    // Xarita to'xtaganda — shu nuqta uchun server javobi
    LaunchedEffect(camera.isMoving) {
        if (camera.isMoving) {
            vm.onMoving()
        } else {
            val target = camera.position.target
            vm.onSettled(target.latitude, target.longitude)
        }
    }

    SystemBarIcons(darkIcons = !colors.isDark)

    Box(Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = camera,
            properties = MapProperties(isMyLocationEnabled = hasPermission),
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = false,
                zoomControlsEnabled = false,
                mapToolbarEnabled = false,
                tiltGesturesEnabled = false,
            ),
            mapColorScheme = ComposeMapColorScheme.FOLLOW_SYSTEM,
        ) {
            branches.filter { it.deliveryZone.size >= 3 }.forEach { branch ->
                Polygon(
                    points = branch.deliveryZone.map { LatLng(it.lat, it.lng) },
                    fillColor = Taxi.copy(alpha = 0.12f),
                    strokeColor = Taxi.copy(alpha = 0.9f),
                    strokeWidth = 4f,
                )
            }
        }

        CenterPin(lifted = camera.isMoving, modifier = Modifier.align(Alignment.Center))

        // tepada: orqaga
        Row(
            Modifier
                .statusBarsPadding()
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoundIconButton(
                Icons.AutoMirrored.Filled.ArrowBack,
                stringResource(R.string.back),
                onBack,
                colors.surface,
                colors.text,
                modifier = Modifier.shadow(4.dp, CircleShape),
                size = 44.dp,
                iconSize = 22.dp,
            )
            Spacer(Modifier.width(10.dp))
            Text(
                stringResource(R.string.location),
                modifier = Modifier
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(colors.surface)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                style = MaterialTheme.typography.titleSmall,
                color = colors.text,
            )
        }

        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
        ) {
            Box(
                Modifier
                    .align(Alignment.End)
                    .padding(end = 16.dp, bottom = 12.dp)
                    .shadow(6.dp, CircleShape)
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(colors.surface),
                contentAlignment = Alignment.Center,
            ) {
                if (locating) {
                    CircularProgressIndicator(Modifier.size(22.dp), color = colors.select, strokeWidth = 2.dp)
                } else {
                    RoundIconButton(
                        Icons.Outlined.MyLocation,
                        stringResource(R.string.locate),
                        ::requestLocation,
                        colors.surface,
                        colors.text,
                        size = 52.dp,
                        iconSize = 24.dp,
                    )
                }
            }
            BottomPanel(
                state = state,
                branchName = vm.branchName(state.route?.branchId),
                message = message,
                onRetry = vm::retry,
                onConfirm = { if (vm.confirm()) onBack() },
            )
        }
    }
}

/** Markazdagi pin: xarita surilayotganda biroz ko'tariladi. Uchi aynan ekran markazida. */
@Composable
private fun CenterPin(lifted: Boolean, modifier: Modifier = Modifier) {
    val lift by animateDpAsState(if (lifted) 10.dp else 0.dp, label = "pin")
    Box(modifier, contentAlignment = Alignment.Center) {
        // yerdagi soya-nuqta
        Box(
            Modifier
                .size(width = 12.dp, height = 5.dp)
                .clip(RoundedCornerShape(50))
                .background(Coal.copy(alpha = 0.35f)),
        )
        Column(
            Modifier.offset { IntOffset(0, -(24.dp + lift).roundToPx()) },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier
                    .size(34.dp)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Taxi)
                    .border(3.dp, Coal, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Coal),
                )
            }
            Box(
                Modifier
                    .size(width = 3.dp, height = 14.dp)
                    .background(Coal),
            )
        }
    }
}

@Composable
private fun BottomPanel(
    state: LocationUiState,
    branchName: String?,
    message: Int?,
    onRetry: () -> Unit,
    onConfirm: () -> Unit,
) {
    val colors = NyffTheme.colors
    val route = state.route
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(16.dp, NyffShapes.sheet)
            .clip(NyffShapes.sheet)
            .background(colors.surface)
            .navigationBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.map_hint), style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
        if (message != null) {
            Notice(stringResource(message), Tone.WARNING, icon = Icons.Outlined.ErrorOutline)
        }
        when {
            state.error != null -> Notice(
                stringResource(errorRes(state.error)),
                Tone.DANGER,
                icon = Icons.Outlined.ErrorOutline,
                action = { PillButton(stringResource(R.string.retry), onRetry) },
            )
            route == null -> Row(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(Modifier.size(18.dp), color = colors.select, strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
                Text(stringResource(R.string.checking), style = MaterialTheme.typography.titleSmall, color = colors.textMuted)
            }
            route.branchId == null -> Notice(stringResource(R.string.no_branch_open), Tone.DANGER, icon = Icons.Outlined.ErrorOutline)
            else -> Notice(
                stringResource(R.string.route_to, branchName.orEmpty()),
                Tone.INFO,
                icon = Icons.Filled.LocationOn,
                supporting = buildString {
                    append(stringResource(R.string.delivery_fee)).append(": ").append(feeText(route.fee))
                    if (route.redirected) append("\n").append(stringResource(R.string.route_redirected))
                },
            )
        }
        PrimaryButton(
            text = stringResource(R.string.choose_this_place),
            onClick = onConfirm,
            enabled = route != null,
        )
    }
}
