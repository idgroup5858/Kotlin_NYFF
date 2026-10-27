package pr.com.kotlin_nyff.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.dp

private fun materialColors(c: NyffColors) = if (c.isDark) {
    darkColorScheme(
        primary = c.accent, onPrimary = c.onAccent, secondary = c.select, onSecondary = Coal,
        background = c.page, onBackground = c.text, surface = c.surface, onSurface = c.text,
        surfaceVariant = c.surfaceMuted, onSurfaceVariant = c.textMuted, surfaceContainer = c.surface,
        surfaceContainerLow = c.surface, surfaceContainerHigh = c.surfaceMuted, outline = c.line,
        outlineVariant = c.line, error = c.danger,
    )
} else {
    lightColorScheme(
        primary = c.accent, onPrimary = c.onAccent, secondary = c.select, onSecondary = Coal,
        background = c.page, onBackground = c.text, surface = c.surface, onSurface = c.text,
        surfaceVariant = c.surfaceMuted, onSurfaceVariant = c.textMuted, surfaceContainer = c.surface,
        surfaceContainerLow = c.surface, surfaceContainerHigh = c.surfaceMuted, outline = c.line,
        outlineVariant = c.line, error = c.danger,
    )
}

/** Shakl tokenlari (web): tugma / maydon 8, kartochka 12, kartochka ichidagi rasm 8. */
object NyffShapes {
    val control = RoundedCornerShape(8.dp)
    val card = RoundedCornerShape(12.dp)
    val media = RoundedCornerShape(8.dp)
    val sheet = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    val header = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
}

/** Ilova mavzusi: telefon sozlamasiga qarab yorug' yoki tungi. */
@Composable
fun NyffTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkNyffColors else LightNyffColors
    CompositionLocalProvider(LocalNyffColors provides colors) {
        MaterialTheme(
            colorScheme = materialColors(colors),
            typography = Typography,
            shapes = Shapes(
                small = NyffShapes.control,
                medium = NyffShapes.card,
                large = NyffShapes.card,
            ),
            content = content,
        )
    }
}

object NyffTheme {
    val colors: NyffColors
        @Composable @ReadOnlyComposable get() = LocalNyffColors.current
}
