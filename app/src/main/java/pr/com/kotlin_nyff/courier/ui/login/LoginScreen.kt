package pr.com.kotlin_nyff.courier.ui.login

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeliveryDining
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pr.com.kotlin_nyff.R
import pr.com.kotlin_nyff.ui.common.LanguageButton
import pr.com.kotlin_nyff.ui.common.NyffCard
import pr.com.kotlin_nyff.ui.common.NyffTextField
import pr.com.kotlin_nyff.ui.common.PrimaryButton
import pr.com.kotlin_nyff.ui.common.RoundIconButton
import pr.com.kotlin_nyff.ui.common.SystemBarIcons
import pr.com.kotlin_nyff.ui.theme.NyffShapes
import pr.com.kotlin_nyff.ui.theme.NyffTheme

/**
 * Kuryer kirishi — login va parol (admin "Xodimlar" da ochib beradi). Mijoz ilovasidagi "NY" belgisiga 10 marta bosib
 * ochiladi; orqaga — mijoz qismiga.
 */
@Composable
fun LoginScreen(vm: LoginViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val colors = NyffTheme.colors
    SystemBarIcons(darkIcons = false)
    BackHandler(onBack = vm::back)

    Column(
        Modifier
            .fillMaxSize()
            .background(colors.page)
            .imePadding(),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(NyffShapes.header)
                .background(colors.header)
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RoundIconButton(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    stringResource(R.string.courier_back),
                    vm::back,
                    colors.headerChip,
                    colors.onHeader,
                    size = 38.dp,
                    iconSize = 20.dp,
                )
                Spacer(Modifier.weight(1f))
                LanguageButton()
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(colors.accent),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.DeliveryDining, contentDescription = null, tint = colors.onAccent, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(stringResource(R.string.courier_courier), style = MaterialTheme.typography.headlineSmall, color = colors.onHeader)
                    Text(stringResource(R.string.courier_login_hint), style = MaterialTheme.typography.bodyMedium, color = colors.onHeaderMuted)
                }
            }
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .navigationBarsPadding(),
        ) {
            NyffCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    NyffTextField(
                        value = state.login,
                        onValueChange = vm::setLogin,
                        placeholder = stringResource(R.string.courier_login_field),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            autoCorrectEnabled = false,
                            keyboardType = KeyboardType.Ascii,
                            imeAction = ImeAction.Next,
                        ),
                    )
                    NyffTextField(
                        value = state.password,
                        onValueChange = vm::setPassword,
                        placeholder = stringResource(R.string.courier_password_field),
                        error = state.error?.let { stringResource(it) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { vm.login() }),
                        visualTransformation = PasswordVisualTransformation(),
                    )
                    PrimaryButton(
                        text = stringResource(R.string.courier_login),
                        onClick = vm::login,
                        enabled = state.login.isNotBlank() && state.password.isNotEmpty(),
                        loading = state.busy,
                    )
                }
            }
        }
    }
}
