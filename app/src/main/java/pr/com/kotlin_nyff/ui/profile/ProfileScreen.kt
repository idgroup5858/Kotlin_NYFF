package pr.com.kotlin_nyff.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pr.com.kotlin_nyff.R
import pr.com.kotlin_nyff.domain.Phone
import pr.com.kotlin_nyff.ui.common.NyffTextField
import pr.com.kotlin_nyff.ui.common.PhoneVisualTransformation
import pr.com.kotlin_nyff.ui.common.PrimaryButton
import pr.com.kotlin_nyff.ui.common.RoundIconButton
import pr.com.kotlin_nyff.ui.common.SectionLabel
import pr.com.kotlin_nyff.ui.common.SystemBarIcons
import pr.com.kotlin_nyff.ui.theme.NyffTheme

/** "⋮" → "Profil": ism va telefon — rasmiylashtirishda o'zi yoziladi. "Saqlash" dan keyin bosh sahifaga qaytadi. */
@Composable
fun ProfileScreen(onBack: () -> Unit, vm: ProfileViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val colors = NyffTheme.colors

    LaunchedEffect(Unit) { vm.saved.collect { onBack() } }
    SystemBarIcons(darkIcons = !colors.isDark)

    Column(
        Modifier
            .fillMaxSize()
            .background(colors.page)
            .imePadding(),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .statusBarsPadding()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoundIconButton(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), onBack, Color.Transparent, colors.text, size = 44.dp, iconSize = 22.dp)
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.profile), style = MaterialTheme.typography.titleLarge, color = colors.text)
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            SectionLabel(stringResource(R.string.contacts))
            NyffTextField(
                value = state.name,
                onValueChange = vm::setName,
                placeholder = stringResource(R.string.name),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            )
            NyffTextField(
                value = state.phone,
                onValueChange = vm::setPhone,
                placeholder = "90 123 45 67",
                prefix = Phone.PREFIX + " ",
                error = if (state.showProblems && state.phoneIncomplete) stringResource(R.string.error_phone_incomplete) else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { vm.save() }),
                visualTransformation = PhoneVisualTransformation,
            )
            Text(
                stringResource(R.string.profile_hint),
                modifier = Modifier.padding(top = 2.dp),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted,
            )
        }

        Column(
            Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .navigationBarsPadding()
                .padding(12.dp),
        ) {
            PrimaryButton(
                text = stringResource(R.string.save),
                onClick = vm::save,
                enabled = state.loaded,
                loading = state.saving,
            )
        }
    }
}
