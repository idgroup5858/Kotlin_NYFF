package pr.com.kotlin_nyff.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import pr.com.kotlin_nyff.domain.Phone
import pr.com.kotlin_nyff.ui.theme.NyffShapes
import pr.com.kotlin_nyff.ui.theme.NyffTheme

/** Oq fonli maydon (web `fieldClass`): fokusda to'q sariq chegara, xatoda qizil va pastida matn. */
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
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    val colors = NyffTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = colors.textFaint) },
        prefix = prefix?.let { { Text(it, color = colors.text) } },
        singleLine = singleLine,
        minLines = minLines,
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        textStyle = MaterialTheme.typography.bodyLarge,
        shape = NyffShapes.control,
        colors = OutlinedTextFieldDefaults.colors(
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
        ),
    )
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
