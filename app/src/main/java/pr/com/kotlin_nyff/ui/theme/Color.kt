package pr.com.kotlin_nyff.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/* Web ilovaning tokenlari (ReactJS_NYFF/src/styles/global.css): New York taksisi — sariq + qora. */
val Taxi = Color(0xFFFFC20E)
val TaxiPressed = Color(0xFFF2B200)
val Select = Color(0xFFFE9A00)
val Coal = Color(0xFF121212)
val CoalSoft = Color(0xFF1C1C1C)
val CoalLine = Color(0xFF2E2E2E)

/** Ekran ranglari — yorug' va tungi mavzuda har xil. Sariq aksent va qora sarlavha ikkalasida bir xil. */
@Immutable
data class NyffColors(
    val isDark: Boolean,
    val page: Color,
    val surface: Color,
    /** Maydon, chip, rasm o'rni foni. */
    val surfaceMuted: Color,
    val line: Color,
    val text: Color,
    val textMuted: Color,
    val textFaint: Color,
    /** Bosh sahifa va buyurtma ekranining qora sarlavhasi. */
    val header: Color,
    val onHeader: Color,
    val onHeaderMuted: Color,
    val headerChip: Color,
    val accent: Color = Taxi,
    val onAccent: Color = Coal,
    val select: Color = Select,
    val success: Color,
    val successSoft: Color,
    val danger: Color,
    val dangerSoft: Color,
    val warning: Color,
    val warningSoft: Color,
    /** Pastdagi qora savat paneli. */
    val bar: Color,
    val onBar: Color,
)

val LightNyffColors = NyffColors(
    isDark = false,
    page = Color(0xFFF1F1ED),
    surface = Color(0xFFFFFFFF),
    surfaceMuted = Color(0xFFECECE7),
    line = Color(0xFFE5E5E0),
    text = Color(0xFF0A0A0A),
    textMuted = Color(0xFF737373),
    textFaint = Color(0xFFA3A3A3),
    header = Coal,
    onHeader = Color.White,
    onHeaderMuted = Color(0x99FFFFFF),
    headerChip = Color(0x1AFFFFFF),
    success = Color(0xFF15803D),
    successSoft = Color(0xFFDCFCE7),
    danger = Color(0xFFB91C1C),
    dangerSoft = Color(0xFFFEE2E2),
    warning = Color(0xFF92400E),
    warningSoft = Color(0xFFFEF3C7),
    bar = Coal,
    onBar = Color.White,
)

val DarkNyffColors = NyffColors(
    isDark = true,
    page = Coal,
    surface = CoalSoft,
    surfaceMuted = Color(0xFF262626),
    line = CoalLine,
    text = Color(0xFFF5F5F5),
    textMuted = Color(0xFFA3A3A3),
    textFaint = Color(0xFF737373),
    header = Color(0xFF000000),
    onHeader = Color.White,
    onHeaderMuted = Color(0x99FFFFFF),
    headerChip = Color(0x1FFFFFFF),
    success = Color(0xFF4ADE80),
    successSoft = Color(0xFF14321F),
    danger = Color(0xFFF87171),
    dangerSoft = Color(0xFF3B1414),
    warning = Color(0xFFFBBF24),
    warningSoft = Color(0xFF3A2A06),
    bar = Color(0xFF2A2A2A),
    onBar = Color.White,
)

val LocalNyffColors = staticCompositionLocalOf { LightNyffColors }
