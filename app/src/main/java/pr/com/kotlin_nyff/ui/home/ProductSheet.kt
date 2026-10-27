package pr.com.kotlin_nyff.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pr.com.kotlin_nyff.R
import pr.com.kotlin_nyff.data.model.MenuProduct
import pr.com.kotlin_nyff.ui.common.FoodImage
import pr.com.kotlin_nyff.ui.common.NyffTextField
import pr.com.kotlin_nyff.ui.common.PrimaryButton
import pr.com.kotlin_nyff.ui.common.QtyStepper
import pr.com.kotlin_nyff.ui.common.SectionLabel
import pr.com.kotlin_nyff.ui.common.money
import pr.com.kotlin_nyff.ui.common.text
import pr.com.kotlin_nyff.ui.theme.NyffShapes
import pr.com.kotlin_nyff.ui.theme.NyffTheme

/** Taom tafsiloti: rasm, narx, tarkibi (grammsiz), izoh ("piyozsiz") va soni → savatga. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductSheet(product: MenuProduct, onAdd: (qty: Int, note: String) -> Unit, onDismiss: () -> Unit) {
    val colors = NyffTheme.colors
    var qty by rememberSaveable(product.id) { mutableIntStateOf(1) }
    var note by rememberSaveable(product.id) { mutableStateOf("") }
    val price = product.price ?: 0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface,
        shape = NyffShapes.sheet,
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FoodImage(
                product.image,
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f),
                shape = NyffShapes.card,
                iconSize = 48.dp,
            )
            Text(product.name.text(), style = MaterialTheme.typography.titleLarge, color = colors.text)
            Text(
                money(price),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                color = colors.text,
            )
            if (product.composition.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SectionLabel(stringResource(R.string.composition))
                    Text(
                        product.composition.map { it.text() }.joinToString(", "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMuted,
                    )
                }
            }
            NyffTextField(
                value = note,
                onValueChange = { note = it.take(200) },
                placeholder = stringResource(R.string.item_note_placeholder),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                QtyStepper(qty, onChange = { delta -> qty = (qty + delta).coerceIn(1, 99) })
                Spacer(Modifier.width(12.dp))
                PrimaryButton(
                    text = stringResource(R.string.add_to_cart),
                    trailing = money(price * qty),
                    onClick = { onAdd(qty, note) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
