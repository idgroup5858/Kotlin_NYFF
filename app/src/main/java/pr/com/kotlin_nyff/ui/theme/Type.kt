package pr.com.kotlin_nyff.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import pr.com.kotlin_nyff.R

/**
 * Inter — iOS shrifti (SF Pro) ga eng yaqin ochiq shrift (SIL OFL 1.1, `third_party/inter/LICENSE.txt`); SF Pro'ning
 * o'zini Apple litsenziyasi Android ilovada ishlatishga ruxsat bermaydi. Besh og'irlik: 400–800; `Black` so'ralsa —
 * eng yaqini `ExtraBold` (Ultra Bold).
 */
val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
    Font(R.font.inter_extrabold, FontWeight.ExtraBold),
)

/** Faqat bosh sahifa sarlavhasi uchun — Inter Black (900); boshqa `Black` yozuvlar `Inter`da ExtraBold bo'lib qoladi. */
private val InterBlack = FontFamily(Font(R.font.inter_black, FontWeight.Black))

/**
 * iOS uslubida: bitta juda katta, Black (900) sarlavha ("Large Title" — `headlineLarge`) va Ultra Bold jami summalar
 * (`headlineMedium`); qolgan matnlar kichik va yengil (Regular / Medium); ekran sarlavhalari Bold.
 */
val Typography = Typography(
    // buyurtma raqami (kuzatish ekrani)
    displayMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.ExtraBold, fontSize = 44.sp, lineHeight = 48.sp, letterSpacing = (-1).sp),
    headlineLarge = TextStyle(fontFamily = InterBlack, fontWeight = FontWeight.Black, fontSize = 27.sp, lineHeight = 32.sp, letterSpacing = (-0.6).sp),
    // jami summa (rasmiylashtirish, kuryerning "Bugun" raqamlari)
    headlineMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, lineHeight = 26.sp, letterSpacing = (-0.4).sp),
    headlineSmall = TextStyle(fontFamily = Inter, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, lineHeight = 29.sp, letterSpacing = (-0.4).sp),
    titleLarge = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 23.sp, letterSpacing = (-0.2).sp),
    titleMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 20.sp),
    titleSmall = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 17.sp),
    bodyLarge = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 20.sp),
    bodyMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
    bodySmall = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 11.sp, lineHeight = 15.sp),
    labelLarge = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 19.sp),
    labelMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 10.sp, lineHeight = 13.sp, letterSpacing = 0.8.sp),
)
