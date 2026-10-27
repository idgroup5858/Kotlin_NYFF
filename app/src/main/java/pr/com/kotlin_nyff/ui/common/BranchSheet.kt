package pr.com.kotlin_nyff.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import pr.com.kotlin_nyff.R
import pr.com.kotlin_nyff.data.model.Branch
import pr.com.kotlin_nyff.ui.theme.NyffShapes
import pr.com.kotlin_nyff.ui.theme.NyffTheme

/** Olib ketish filialini tanlash: yopiq filial ko'rinadi, lekin tanlab bo'lmaydi (holati yonida). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BranchSheet(
    branches: List<Branch>,
    selectedId: String?,
    onSelect: (Branch) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = NyffTheme.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.page,
        shape = NyffShapes.sheet,
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(stringResource(R.string.choose_branch), style = MaterialTheme.typography.titleLarge, color = colors.text)
            Spacer(Modifier.height(2.dp))
            branches.forEach { branch ->
                val open = branch.isOpen
                val selected = branch.id == selectedId
                NyffCard(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier
                            .clickable(enabled = open, role = Role.RadioButton) { onSelect(branch) }
                            .padding(14.dp)
                            .alpha(if (open) 1f else 0.6f),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (open) colors.accent else colors.surfaceMuted),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Outlined.Storefront,
                                contentDescription = null,
                                tint = if (open) colors.onAccent else colors.textMuted,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(branch.name, style = MaterialTheme.typography.titleMedium, color = colors.text)
                            if (branch.address.isNotBlank()) {
                                Text(branch.address, style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                            }
                            Text(
                                branchStateText(branch),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (open) colors.success else colors.danger,
                            )
                        }
                        if (selected) {
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = colors.select)
                        }
                    }
                }
            }
        }
    }
}
