package pr.com.kotlin_nyff.courier.ui.main

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pr.com.kotlin_nyff.R
import pr.com.kotlin_nyff.core.AppTheme
import pr.com.kotlin_nyff.data.remote.AppError
import pr.com.kotlin_nyff.courier.ui.Notifier
import pr.com.kotlin_nyff.courier.ui.courierErrorRes
import pr.com.kotlin_nyff.ui.common.BrandBadge
import pr.com.kotlin_nyff.ui.common.ErrorBlock
import pr.com.kotlin_nyff.ui.common.LanguageButton
import pr.com.kotlin_nyff.ui.common.Notice
import pr.com.kotlin_nyff.ui.common.RoundIconButton
import pr.com.kotlin_nyff.ui.common.SystemBarIcons
import pr.com.kotlin_nyff.ui.common.Tone
import pr.com.kotlin_nyff.ui.theme.NyffShapes
import pr.com.kotlin_nyff.ui.theme.NyffTheme

private enum class Tab(val title: Int) { NEW(R.string.courier_tab_new), MINE(R.string.courier_tab_mine), TODAY(R.string.courier_tab_today) }

@Composable
fun MainScreen(vm: MainViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val colors = NyffTheme.colors
    val context = LocalContext.current
    var tab by rememberSaveable { mutableStateOf(Tab.NEW) }
    val snackbar = remember { SnackbarHostState() }

    SystemBarIcons(darkIcons = false)

    // Android 13+ — bildirishnoma ruxsati (yangi buyurtma haqida xabar)
    val notifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !Notifier.canNotify(context)) {
            notifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    // qo'lda buyurtma bo'lsa — "Meniki" ochiq tursin
    LaunchedEffect(state.active.isNotEmpty()) {
        if (state.active.isNotEmpty() && tab == Tab.NEW && state.departed) tab = Tab.MINE
    }
    val message = state.message?.let { stringResource(it) }
    LaunchedEffect(message) {
        if (message != null) {
            snackbar.showSnackbar(message)
            vm.consumeMessage()
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(colors.page),
    ) {
        Column(Modifier.fillMaxSize()) {
            Header(state, vm, tab, onTab = { tab = it })
            if (state.loaded && state.loadError is AppError.Network) {
                Notice(
                    stringResource(R.string.courier_offline),
                    Tone.WARNING,
                    Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp),
                    Icons.Outlined.CloudOff,
                )
            }
            Box(Modifier.weight(1f)) {
                when {
                    !state.loaded && state.loadError != null ->
                        ErrorBlock(state.loadError, vm::refresh, message = courierErrorRes(state.loadError))
                    !state.loaded -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.select)
                    }
                    else -> when (tab) {
                        Tab.NEW -> AvailableTab(state, vm)
                        Tab.MINE -> MineTab(state, vm)
                        Tab.TODAY -> TodayTab(state, vm)
                    }
                }
            }
        }
        SnackbarHost(
            snackbar,
            Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
        )
    }
}

@Composable
private fun Header(state: MainUiState, vm: MainViewModel, tab: Tab, onTab: (Tab) -> Unit) {
    val colors = NyffTheme.colors
    val context = LocalContext.current
    var menu by remember { mutableStateOf(false) }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(NyffShapes.header)
            .background(colors.header)
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BrandBadge()
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(state.me?.user?.name.orEmpty(), style = MaterialTheme.typography.titleMedium, color = colors.onHeader, maxLines = 1)
                Text(
                    stringResource(if (state.onDuty) R.string.courier_on_duty else R.string.courier_off_duty),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (state.onDuty) colors.accent else colors.onHeaderMuted,
                )
            }
            Switch(
                checked = state.onDuty,
                onCheckedChange = vm::setDuty,
                enabled = state.me != null && state.busy != MainViewModel.DUTY,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.onAccent,
                    checkedTrackColor = colors.accent,
                    uncheckedThumbColor = colors.onHeaderMuted,
                    uncheckedTrackColor = colors.headerChip,
                    uncheckedBorderColor = colors.onHeaderMuted,
                ),
            )
            Spacer(Modifier.width(8.dp))
            LanguageButton()
            Box {
                RoundIconButton(
                    Icons.Outlined.MoreVert,
                    stringResource(R.string.courier_menu),
                    { menu = true },
                    Color.Transparent,
                    colors.onHeader,
                    size = 38.dp,
                    iconSize = 22.dp,
                )
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    // yorug' / tungi rejim — mijoz qismidagi bilan bitta tanlov
                    val dark = colors.isDark
                    DropdownMenuItem(
                        text = { Text(stringResource(if (dark) R.string.theme_light else R.string.theme_dark)) },
                        leadingIcon = {
                            Icon(if (dark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode, contentDescription = null)
                        },
                        onClick = {
                            menu = false
                            AppTheme.apply(context, if (dark) AppTheme.LIGHT else AppTheme.DARK)
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.courier_logout)) },
                        leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null) },
                        onClick = {
                            menu = false
                            vm.logout()
                        },
                    )
                }
            }
        }
        Tabs(tab, onTab, newCount = state.newCount, mineCount = state.active.size)
    }
}

/** "Yangi · 3", "Meniki · 2", "Bugun" — sariq pill tanlangan (mijoz ilovasidagi tur tanlovi uslubida). */
@Composable
private fun Tabs(tab: Tab, onTab: (Tab) -> Unit, newCount: Int, mineCount: Int) {
    val colors = NyffTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(colors.headerChip)
            .padding(3.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Tab.entries.forEach { value ->
            val active = value == tab
            val count = when (value) {
                Tab.NEW -> newCount
                Tab.MINE -> mineCount
                Tab.TODAY -> 0
            }
            Row(
                Modifier
                    .weight(1f)
                    .height(32.dp)
                    .clip(CircleShape)
                    .background(if (active) colors.accent else Color.Transparent)
                    .selectable(selected = active, role = Role.Tab, onClick = { onTab(value) }),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val tint = if (active) colors.onAccent else colors.onHeaderMuted
                Text(stringResource(value.title), style = MaterialTheme.typography.labelMedium, color = tint)
                if (count > 0) {
                    Spacer(Modifier.width(6.dp))
                    Box(
                        Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(if (active) colors.onAccent else colors.accent),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            count.toString(),
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = MaterialTheme.typography.labelMedium.letterSpacing),
                            color = if (active) colors.accent else colors.onAccent,
                        )
                    }
                }
            }
        }
    }
}
