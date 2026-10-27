package pr.com.kotlin_nyff.ui.common

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pr.com.kotlin_nyff.domain.Phone
import pr.com.kotlin_nyff.ui.theme.NyffShapes
import pr.com.kotlin_nyff.ui.theme.NyffTheme

/**
 * Oq fonli maydon (web `fieldClass`): fokusda to'q sariq chegara, xatoda qizil va pastida matn. Material
 * `OutlinedTextField`ning 56dp minimal balandligi o'rniga ixcham — ~30dp (qator 16sp, ichki bo'shliq 7dp).
 */
@Composable
fun NyffTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    prefix: String? = null,
    error: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    val colors = NyffTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.text, lineHeight = 16.sp)
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colors.select,
        unfocusedBorderColor = colors.line,
        errorBorderColor = colors.danger,
        focusedContainerColor = colors.surface,
        unfocusedContainerColor = colors.surface,
        errorContainerColor = colors.surface,
        cursorColor = colors.select,
        focusedTextColor = colors.text,
        unfocusedTextColor = colors.text,
        errorSupportingTextColor = colors.danger,
    )
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        minLines = minLines,
        visualTransformation = visualTransformation,
        interactionSource = interaction,
        cursorBrush = SolidColor(colors.select),
    ) { innerTextField ->
        OutlinedTextFieldDefaults.DecorationBox(
            value = value,
            innerTextField = innerTextField,
            enabled = true,
            singleLine = singleLine,
            visualTransformation = visualTransformation,
            interactionSource = interaction,
            isError = error != null,
            placeholder = {
                Text(
                    placeholder,
                    style = textStyle,
                    color = colors.textFaint,
                    maxLines = if (singleLine) 1 else Int.MAX_VALUE,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            prefix = prefix?.let { { Text(it, style = textStyle) } },
            supportingText = error?.let { { Text(it) } },
            colors = fieldColors,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp),
            container = {
                OutlinedTextFieldDefaults.Container(
                    enabled = true,
                    isError = error != null,
                    interactionSource = interaction,
                    colors = fieldColors,
                    shape = NyffShapes.control,
                )
            },
        )
    }
}

/** "901234567" → ekranda "90 123 45 67" (qiymatda faqat raqamlar qoladi). */
object PhoneVisualTransformation : VisualTransformation {

    /** Asl matndagi bo'shliq qo'yiladigan joylar: 2, 5, 7 raqamdan keyin. */
    private val breaks = intArrayOf(2, 5, 7)

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val length = raw.length
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                offset + breaks.count { it < offset && it < length }

            override fun transformedToOriginal(offset: Int): Int {
                // bo'shliqning o'zgargan matndagi o'rni: 2, 6, 9
                val spaces = breaks.withIndex().count { (index, at) -> at < length && at + index < offset }
                return (offset - spaces).coerceIn(0, length)
            }
        }
        return TransformedText(AnnotatedString(Phone.group(raw)), mapping)
    }
}
