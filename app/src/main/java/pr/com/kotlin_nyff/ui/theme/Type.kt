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
 * o'zini Apple litsenziyasi Android ilovada ishlatishga ruxsat bermaydi. To'rt og'irlik: 400–700; `Black` / `ExtraBold`
 * so'ralsa — eng yaqini `Bold`.
 */
val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)

/** Faqat katta sarlavha uchun — Inter ExtraBold (Ultra Bold, 800); boshqa `Black` yozuvlar `Inter`da Bold bo'lib qoladi. */
private val InterDisplay = FontFamily(Font(R.font.inter_extrabold, FontWeight.ExtraBold))

/**
 * iOS uslubida: bitta juda katta, qalin sarlavha ("Large Title" — `headlineLarge`), qolgan matnlar kichik va yengil
 * (Regular / Medium); qalinroq faqat tugma yozuvi va ekran sarlavhalari (SemiBold).
 */
val Typography = Typography(
    displayMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 56.sp, lineHeight = 60.sp),
    headlineLarge = TextStyle(fontFamily = InterDisplay, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, lineHeight = 33.sp, letterSpacing = (-0.6).sp),
    headlineSmall = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 29.sp),
    titleLarge = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 23.sp),
    titleMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 20.sp),
    titleSmall = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 17.sp),
    bodyLarge = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 20.sp),
    bodyMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
    bodySmall = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 11.sp, lineHeight = 15.sp),
    labelLarge = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 19.sp),
    labelMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 10.sp, lineHeight = 13.sp, letterSpacing = 0.8.sp),
)
